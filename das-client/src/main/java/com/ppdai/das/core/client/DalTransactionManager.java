package com.ppdai.das.core.client;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

import com.ppdai.das.client.Hints;
import com.ppdai.das.core.DasException;
import com.ppdai.das.core.ErrorCode;
import com.ppdai.das.core.EventEnum;
import com.ppdai.das.core.HaContext;
import com.ppdai.das.core.HintEnum;
import com.ppdai.das.core.markdown.MarkdownManager;


public class DalTransactionManager extends TransactionManager<ConnectionAction>{
	private DalConnectionManager connManager;

	public DalTransactionManager(DalConnectionManager connManager) {
		this.connManager = connManager;
	}

    public static void setCurrentTransaction(DalTransaction transaction) {
        transactionHolder.set(transaction);
    }


	private <T> int startTransaction(Hints hints, ConnectionAction<T> action) throws SQLException {
		DalTransaction transaction = getCurrentTransaction();

		if(transaction == null) {
			transaction = new DalTransaction( 
					getConnection(hints, true, action.operation, action.highAvalible), 
					connManager.getLogicDbName(), hints.is(HintEnum.applyDefaultShard));
			
			transactionHolder.set(transaction);
		}else{
		    transaction.validate(connManager.getLogicDbName(), connManager.evaluateShard(hints));
		}
		
        action.connHolder = transaction.getConnection();
		return transaction.startTransaction();
	}

	private void endTransaction(int startLevel) throws SQLException {
		DalTransaction transaction = getCurrentTransaction();

		if(transaction == null) {
            throw new SQLException("calling endTransaction with empty ConnectionCache");
        }

		transaction.endTransaction(startLevel);
	}
	
	private static void reqiresTransaction() throws DasException {
		if(!isInTransaction()) {
            throw new DasException(ErrorCode.TransactionNoFound);
        }
	}
	
	public static List<DalTransactionListener> getCurrentListeners() throws DasException {
		reqiresTransaction();
		DalTransaction transaction = getCurrentTransaction();
		return transaction.getListeners();
	}
	
	public static void register(DalTransactionListener listener) throws DasException {
		reqiresTransaction();
		Objects.requireNonNull(listener, "The listener should not be null");
		DalTransaction transaction = getCurrentTransaction();
		transaction.register(listener);
	}
	
	private void rollbackTransaction() throws SQLException {
		DalTransaction transaction = getCurrentTransaction();
		
		// Already handled in deeper level
		if(transaction == null) {
            return;
        }

		transaction.rollbackTransaction();
	}
	
	public DalConnection getConnection(Hints hints, EventEnum operation, HaContext ha) throws SQLException {
		return getConnection(hints, false, operation, ha);
	}
	
	public static String getLogicDbName() {
		DalTransaction dalTransaction = getCurrentTransaction();
		return isInTransaction() ?
				dalTransaction.getLogicDbName() : null;
	}

	public static String getCurrentShardId() {
		DalTransaction dalTransaction = getCurrentTransaction();
        return isInTransaction() ?
				dalTransaction.getConnection().getShardId() : null;
	}

	public static boolean isDefaultShardApplied() {
		DalTransaction dalTransaction = getCurrentTransaction();
		return isInTransaction() ?
				dalTransaction.isDefaultShardApplied(): false;
	}

	public static DbMeta getCurrentDbMeta() {
		DalTransaction dalTransaction = getCurrentTransaction();
		return isInTransaction() ?
				dalTransaction.getConnection().getMeta() :
					null;
	}
	
	private DalConnection getConnection(Hints hints, boolean useMaster, EventEnum operation, HaContext ha) throws SQLException {
		DalTransaction transaction = getCurrentTransaction();
		
		if(transaction == null) {
			return connManager.getNewConnection(hints, useMaster, operation, ha);
		} else {
			transaction.validate(connManager.getLogicDbName(), connManager.evaluateShard(hints));
			return transaction.getConnection();
		}
	}

	@Override
	public Object doInTransaction(ConnectionAction action, Hints hints)throws SQLException{
		action.config = connManager.getConfig();
		action.initLogEntry(connManager.getLogicDbName(), hints);
		action.start();

		Throwable ex = null;
		Object result = null;
		int level;
		try {
			level = startTransaction(hints, action);
			action.populateDbMeta();

			result = action.execute();

			if(hints.isRollbackOnly()) {
				rollbackTransaction();
			} else {
				endTransaction(level);
			}
		} catch (Throwable e) {
		    action.error(e);
			rollbackTransaction();
			MarkdownManager.detect(action.connHolder, action.start, e);
		}finally{
			action.cleanup();
		}

		action.end(result);

		return result;
	}


}
