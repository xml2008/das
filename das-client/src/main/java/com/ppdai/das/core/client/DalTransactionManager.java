package com.ppdai.das.core.client;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

import com.ppdai.das.client.Hints;
import com.ppdai.das.client.transaction.DasTransactionInterceptor;
import com.ppdai.das.client.transaction.TransactionalIntercepted;
import com.ppdai.das.client.annotation.DasTransactional;
import com.ppdai.das.core.DasException;
import com.ppdai.das.core.ErrorCode;
import com.ppdai.das.core.EventEnum;
import com.ppdai.das.core.HaContext;
import com.ppdai.das.core.HintEnum;
import com.ppdai.das.core.markdown.MarkdownManager;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.NoOp;

public class DalTransactionManager {
	private DalConnectionManager connManager;

	private static final ThreadLocal<DalTransaction> transactionHolder = new ThreadLocal<DalTransaction>();

	public DalTransactionManager(DalConnectionManager connManager) {
		this.connManager = connManager;
	}

    public static void setCurrentTransaction(DalTransaction transaction) {
        transactionHolder.set(transaction);
    }

	private <T> int startTransaction(Hints hints, ConnectionAction<T> action) throws SQLException {
		DalTransaction transaction = transactionHolder.get();

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
		DalTransaction transaction = transactionHolder.get();
		
		if(transaction == null) {
            throw new SQLException("calling endTransaction with empty ConnectionCache");
        }

		transaction.endTransaction(startLevel);
	}

	public static boolean isInTransaction() {
		return transactionHolder.get() != null;
	}
	
	private static void reqiresTransaction() throws DasException {
		if(!isInTransaction()) {
            throw new DasException(ErrorCode.TransactionNoFound);
        }
	}
	
	public static List<DalTransactionListener> getCurrentListeners() throws DasException {
		reqiresTransaction();		
		return transactionHolder.get().getListeners();
	}
	
	public static void register(DalTransactionListener listener) throws DasException {
		reqiresTransaction();
		Objects.requireNonNull(listener, "The listener should not be null");
		
		transactionHolder.get().register(listener);
	}
	
	private void rollbackTransaction() throws SQLException {
		DalTransaction transaction = transactionHolder.get();
		
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
		return isInTransaction() ?
				transactionHolder.get().getLogicDbName() :
					null;
	}
	
	public static String getCurrentShardId() {
        return isInTransaction() ?
                transactionHolder.get().getConnection().getShardId() :
                    null;
	}

	public static boolean isDefaultShardApplied() {
		return isInTransaction() ?
				transactionHolder.get().isDefaultShardApplied():
				false;
	}

	public static DbMeta getCurrentDbMeta() {
		return isInTransaction() ?
				transactionHolder.get().getConnection().getMeta() :
					null;
	}
	
	private DalConnection getConnection(Hints hints, boolean useMaster, EventEnum operation, HaContext ha) throws SQLException {
		DalTransaction transaction = transactionHolder.get();
		
		if(transaction == null) {
			return connManager.getNewConnection(hints, useMaster, operation, ha);
		} else {
			transaction.validate(connManager.getLogicDbName(), connManager.evaluateShard(hints));
			return transaction.getConnection();
		}
	}
	
	public static void clearCurrentTransaction() {
		transactionHolder.set(null);
	}

    @SuppressWarnings("unchecked")
    public static <T> T create(Class<T> targetClass) throws InstantiationException, IllegalAccessException {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(targetClass);
        enhancer.setClassLoader(targetClass.getClassLoader());
        enhancer.setCallbackFilter(new TransactionalCallbackFilter());
        Callback[] callbacks = new Callback[] {new DasTransactionInterceptor(), NoOp.INSTANCE};
        enhancer.setCallbacks(callbacks);
        enhancer.setInterfaces(new Class[] {TransactionalIntercepted.class});
        return (T) enhancer.create();
    }

    private static class TransactionalCallbackFilter implements CallbackFilter {
        @Override
        public int accept(Method method) {
            return method.isAnnotationPresent(DasTransactional.class) ? 0 : 1;
        }
    }
    
	public <T> T doInTransaction(ConnectionAction<T> action, Hints hints)throws SQLException{
	    action.config = connManager.getConfig();
		action.initLogEntry(connManager.getLogicDbName(), hints);
		action.start();

		Throwable ex = null;
		T result = null;
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
