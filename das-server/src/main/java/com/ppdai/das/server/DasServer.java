package com.ppdai.das.server;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;


import java.io.IOException;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static com.ppdai.das.core.DasConfigureFactory.configContextRef;
import static com.ppdai.das.util.ConvertUtils.*;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Ints;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ppdai.das.client.*;
import com.ppdai.das.client.delegate.remote.ServerSelector;
import com.ppdai.das.client.sqlbuilder.ColumnOrder;
import com.ppdai.das.client.sqlbuilder.SqlBuilderSerializer;

import com.ppdai.das.core.DasLogger;

import com.ppdai.das.core.LogContext;
import com.ppdai.das.service.*;
import com.ppdai.das.tx.Txlog;
import com.ppdai.das.util.ConvertUtils;
import com.sun.net.httpserver.HttpServer;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TThreadedSelectorServer;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransportException;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.ppdai.das.client.delegate.DasDelegate;
import com.ppdai.das.client.delegate.remote.BuilderUtils;
import com.ppdai.das.core.DasConfigureFactory;
import com.ppdai.das.core.DasDiagnose;
import com.ppdai.das.core.TransactionId;
import com.ppdai.das.core.TransactionServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DasServer implements DasService.Iface {
    private static final Logger logger = LoggerFactory.getLogger(DasServer.class);

    public final static int DEFAULT_PORT = 9090;
    public static final String SELECTING_NUMBER = "selectingNumber";
    public static final String WORKING_NUMBER   = "workingNumber";
    public static final String PORT              = "port";

    private final TransactionServer transServer;
    private final String address;
    private final String workerId;
    private final DasServerStatusMonitor monitor;
    
    private int port = DEFAULT_PORT;

    public DasServer(String address, String workerId) throws UnknownHostException, SQLException {
        this.address = address;
        this.workerId = workerId;
        createPIDFile();

        transServer = new TransactionServer(address, workerId);
        monitor = new DasServerStatusMonitor(transServer);

        DasConfigureFactory.warmUpAllConnections();

        launchCommand();
    }

    private void launchCommand() {
        Executors.newScheduledThreadPool(3).scheduleAtFixedRate(()->{
            try {
                DasClient dasClient = DasClientFactory.getClient("das_tx");

                /**
                 * select max(id),XID
                 * from txlog
                 * #where status like 'rollback' or status like 'commit'
                 * group by XID
                 */
                List<Map> maps = dasClient.query(new SqlBuilder().appendTemplate(
                                "select max(id) as mid, XID, status, nodeID " +
                                "from txlog " +
                                "where status like 'rollback' or status like 'commit' " +
                                "group by XID").intoMap());
                maps.size();
                maps.forEach(map -> {

                    String status = ((String) map.get("status"));
                    String xid = ((String) map.get("XID"));
                    String node = ((String) map.get("nodeID"));
                    final String host = Splitter.on(":").split((String) map.get("XID")).iterator().next();
                    if (status.equals("commit")) {
                        try {
                            sendConfirm(host, xid, node);
                        } catch (TException e) {
                            e.printStackTrace();
                        }
                    } else if (status.equals("rollback")) {
                        sendCancel(host, xid, node);
                    }

                });
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }, 0, 5, TimeUnit.SECONDS);
    }

    private void sendCancel(String host, String xid, String node) {
    }


    private void sendConfirm(String host, String xid, String node) throws TException {
        TSocket transport = new TSocket(host, 9091);
        TFramedTransport ft = new TFramedTransport(transport);
        TBinaryProtocol protocol = new TBinaryProtocol(ft);
        transport.setTimeout(3000);//TODO
        transport.open();
        TxGeneralRequest request = new TxGeneralRequest();
        request.setXid(xid);
        request.setAppId("na");
        request.setIp(host);
        request.setTxType(TxType.TCC);
        request.setNode(node);

        new CommandService.Client(protocol).confirmCommand(request);
    }

    @Override
    public DasResult execute(DasRequest request) throws DasException, TException {
        monitor.receive(request);
        DasLogger dalLogger = configContextRef.get().getLogger();
        Throwable ex = null;
        LogContext logContext = dalLogger.receiveRemoteRequest(request);
        try {
            return transServer.doInTransaction(getTransactionId(request.transactionId), () -> {
                switch (request.getOperation()) {
                    case QueryBySample:
                        return queryBySample(request);
                    case QueryByPK:
                        return queryByPK(request);
                    case CountBySample:
                        return countBySample(request);
                    case Insert:
                        return insert(request);
                    case InsertList:
                        return insertList(request);
                    case DeleteByPk:
                        return deleteByPk(request);
                    case DeleteBySample:
                        return deleteBySample(request);
                    case Update:
                        return update(request);
                    case BatchUpdate:
                        return batchUpdate(request);
                    case BatchDelete:
                        return batchDelete(request);
                    case BatchInsert:
                        return batchInsert(request);
                    case UpdateWithSqlBuilder:
                        return updateWithSqlBuilder(request);
                    case BatchUpdateWithSqlBuilder:
                        return batchUpdateWithSqlBuilder(request);
                    case Query:
                        return query(request);
                    case QueryObject:
                        return queryObject(request);
                    case Call:
                        return call(request);
                    case BatchCall:
                        return batchCall(request);
                    case BatchQuery:
                        return batchQuery(request);
                    case QueryBySampleWithRange:
                        return queryBySampleWithRange(request);
                    default:
                        throw new IllegalArgumentException("Unknown Operation:" + request.getOperation());
                }
            });
        } catch (Throwable e) {
            ex = e;
            logger.error(e.getMessage(), e);
            SQLException sqlException = com.ppdai.das.core.DasException.wrap(e);
            throw new DasException(sqlException.getErrorCode() + "", sqlException.getMessage());
        } finally {
            dalLogger.finishRemoteRequest(logContext, ex);
            monitor.complete(request, ex);
        }
    }

    private String getTransactionId(DasTransactionId transactionId) {
        if(transactionId == null)
            return null;
        
        return buildTransactionId(transactionId);
    }
    
    private DasDelegate getDelegate(String appId, String logicDbName, String customerClientVersion) {
        return new ServerDasDelegate(appId, logicDbName, customerClientVersion);
    }
    
    private DasDelegate getDelegate(DasRequest request) {
        return getDelegate(request.appId, request.logicDbName, request.ppdaiClientVersion);
    }

    private DasResult intResult(int result){
        return new DasResult()
                .setRows(Arrays.asList(new Entity().setValue(new Gson().toJson(result))))
                .setRowCount(1);
    }

    private DasResult intsResult(int[] results){
        return new DasResult()
                .setRowCount(results.length)
                .setRows(pojo2Entities(Ints.asList(results), null));
    }

    private SqlBuilder toSqlBuilder(DasRequest request) {
        List<SqlBuilder> builders = BuilderUtils.fromSqlBuilders(request.getSqlBuilders());
        if(builders.isEmpty()) {
            return null;
        } else {
            SqlBuilder builder = Iterables.getFirst(builders, null);
            String entityTypeInStr = Iterables.getFirst(request.getSqlBuilders(), null).getEntityType();
            if(entityTypeInStr != null) {
                Class clz = entityTypes.getOrDefault(entityTypeInStr, Entity.class);
                builder.into(clz);
            }

            builder.setHints(translate(request.getHints()));

            return builder;
        }
    }

    final static ImmutableMap<String, Class> entityTypes = ImmutableMap.of(
            Object.class.getName(), Object.class,
            Entity.class.getName(), Entity.class,
            String.class.getName(), String.class,
            Map.class.getName(), Map.class
    );

    private BatchUpdateBuilder toBatchUpdateBuilder(DasRequest request) {
        BatchUpdateBuilder result = new BatchUpdateBuilder(toSqlBuilder(request));
        List<Object[]> values = BuilderUtils.toList(request.getBatchUpdateBuilder().getValuesList(), v-> {
           List l = SqlBuilderSerializer.deserializePrimitive(v);
           return l.toArray(new Object[l.size()]);
        });
        result.getValuesList().addAll(values);

        List<String> statementList = request.getBatchUpdateBuilder().getStatements();
        if(!statementList.isEmpty()) {
            //BatchUpdateBuilder checks statements null
            String[] statements = statementList.toArray(new String[statementList.size()]);
            result.setStatements(statements);
        }
        result.setHints(translate(request.getHints()));
        return result;
    }

    private DasResult updateWithSqlBuilder(DasRequest request) throws SQLException {
        SqlBuilder sqlBuilder = toSqlBuilder(request);

        int result = getDelegate(request).update(sqlBuilder);
        return intResult(result).setDiagInfo(diagInfo2Hints(sqlBuilder.hints().getDiagnose()));
    }

    private DasResult batchUpdateWithSqlBuilder(DasRequest request) throws SQLException {
        BatchUpdateBuilder batchUpdateBuilder = toBatchUpdateBuilder(request);

        int[] results = getDelegate(request).batchUpdate(batchUpdateBuilder);
        return intsResult(results).setDiagInfo(diagInfo2Hints(batchUpdateBuilder.hints().getDiagnose()));
    }

    private DasResult query(DasRequest request) throws SQLException {
        SqlBuilder sqlBuilder = toSqlBuilder(request);

        List list = getDelegate(request).query(sqlBuilder);
        List<Entity> entities = ConvertUtils.pojo2Entities(list, sqlBuilder.getEntityMeta());
        return new DasResult()
                .setRowCount(list.size())
                .setRows(entities)
                .setEntityMeta(sqlBuilder.getEntityMeta())
                .setDiagInfo(diagInfo2Hints(sqlBuilder.hints().getDiagnose()));
    }

    private DasResult queryObject(DasRequest request) throws SQLException {
        EntityMeta meta = request.getSqlBuilders().get(0).getEntityMeta();
        SqlBuilder sqlBuilder = toSqlBuilder(request).setEntityMeta(meta);

        Object r = null;
        if(request.getSqlBuilders().get(0).isNullable()){
            r = getDelegate(request).queryObjectNullable(sqlBuilder);
        } else {
            r = getDelegate(request).queryObject(sqlBuilder);
        }

        DasResult dasResult = new DasResult();
        List<Entity> entities = ConvertUtils.pojo2Entities(r == null ? Arrays.asList() : Arrays.asList(r), sqlBuilder.getEntityMeta());
        return dasResult.setRowCount(entities.size())
                 .setRows(entities)
                 .setDiagInfo(diagInfo2Hints(sqlBuilder.hints().getDiagnose()));
    }

    private DasResult call(DasRequest request) throws SQLException {
        CallBuilder callBuilder = BuilderUtils.fromCallBuilder(request.getCallBuilder());
        callBuilder.setHints(translate(request.getHints()));//Copy Hints from DasRequest to CallBuilder

        getDelegate(request).call(callBuilder);
        return new DasResult().setParameters(new DasParameters().setParameters(
                BuilderUtils.buildParameters(callBuilder.getParameters())))
                .setDiagInfo(diagInfo2Hints(callBuilder.hints().getDiagnose()));
    }

    private DasResult batchCall(DasRequest request) throws SQLException {
        BatchCallBuilder batchCallBuilder = BuilderUtils.fromBatchCallBuilder(request.getBatchCallBuilder());
        batchCallBuilder.setHints(translate(request.getHints()));//Copy Hints from DasRequest to BatchCallBuilder

        int[] results = getDelegate(request).batchCall(batchCallBuilder);
        return intsResult(results).setDiagInfo(diagInfo2Hints(batchCallBuilder.hints().getDiagnose()));
    }

    private DasResult batchQuery(DasRequest request) throws SQLException {
        List<SqlBuilder> sqlBuilders = BuilderUtils.fromSqlBuilders(request.getSqlBuilders());

        BatchQueryBuilder batchQueryBuilder = new BatchQueryBuilder();
        for (SqlBuilder builder : sqlBuilders) {
            batchQueryBuilder.addBatch(builder.into(Entity.class));
        }

        batchQueryBuilder.setHints(translate(request.getHints()));//Copy Hints from DasRequest to BatchQueryBuilder

        List<List<Entity>> results = (List<List<Entity>>) getDelegate(request).batchQuery(batchQueryBuilder);
        Object[] flatResult = flatResult(results);

        return new DasResult()
                .setRowCount(results.size())
                .setRows((List<Entity>)flatResult[0])
                .setBatchRowsIndex((List<Integer>)flatResult[1])
                .setDiagInfo(diagInfo2Hints(batchQueryBuilder.hints().getDiagnose()));
    }

    private Object[] flatResult(List<List<Entity>> results) {
        List<Entity> list = new ArrayList<>();
        List<Integer> index = new ArrayList<>();

        results.stream().forEach(l -> {
            index.add(l.size());
            list.addAll(l);
        });
        return new Object[]{list, index};
     }

    private DasResult deleteByPk(DasRequest request) throws SQLException {
        Entity entity = fillMeta(request);
        Hints hints = translate(request.getHints());
        int result = getDelegate(request).deleteByPk(entity, hints);
        return intResult(result).setDiagInfo(diagInfo2Hints(hints.getDiagnose()));
    }

    private DasResult deleteBySample(DasRequest request) throws SQLException {
        Entity entity = fillMeta(request);
        Hints hints = translate(request.getHints());
        int result = getDelegate(request).deleteBySample(entity, hints);
        return intResult(result).setDiagInfo(diagInfo2Hints(hints.getDiagnose()));
    }

    private DasResult insertList(DasRequest request) throws SQLException {
        List<Entity> entities = fillMetas(request);
        Hints hints = translate(request.getHints());
        int result = getDelegate(request).insert(entities, hints);

        return idBackResult(result, entities, hints.isSetIdBack())
                .setDiagInfo(diagInfo2Hints(hints.getDiagnose()));
    }

    private DasResult insert(DasRequest request) throws SQLException {
        Entity entity = fillMeta(request);
        Hints hints = translate(request.getHints());
        int result = getDelegate(request).insert(entity, hints);

        return idBackResult(result, newArrayList(entity), hints.isSetIdBack())
                .setDiagInfo(diagInfo2Hints(hints.getDiagnose()));
    }

    private DasResult idBackResult(int insertResult, List<Entity> entities, boolean isIdBack) {
       List<Entity> insertResults = newArrayList(new Entity().setValue(new Gson().toJson(insertResult)));

        DasResult dasResult = new DasResult().setRows(insertResults);
        if(isIdBack) {
            insertResults.addAll(entities);
            return dasResult.setRowCount(1 + entities.size());
        } else {
            return dasResult.setRowCount(entities.size());
        }
    }


    private DasResult update(DasRequest request) throws SQLException {
        Entity entity = fillMeta(request);
        Hints hints = translate(request.getHints());
        int result = getDelegate(request).update(entity, hints);
        return intResult(result).setDiagInfo(diagInfo2Hints(hints.getDiagnose()));
    }

    private DasResult batchUpdate(DasRequest request) throws SQLException {
        List<Entity> entities = fillMetas(request);
        Hints hints = translate(request.getHints());
        int[] result = getDelegate(request).batchUpdate(entities, hints);
        return intsResult(result).setDiagInfo(diagInfo2Hints(hints.getDiagnose()));
    }

    private DasResult batchDelete(DasRequest request) throws SQLException {
        List<Entity> entities = fillMetas(request);
        Hints hints = translate(request.getHints());
        int[] result = getDelegate(request).batchDelete(entities, hints);
        return intsResult(result).setDiagInfo(diagInfo2Hints(hints.getDiagnose()));
    }

    private DasResult queryBySample(DasRequest request) throws SQLException {
        EntityList entities = request.getEntityList();
        checkArgument(!entities.getRows().isEmpty());

        Hints hints = translate(request.getHints());
        List<Entity> list = getDelegate(request).queryBySample(fillMeta(request), hints);
        return new DasResult()
                .setRowCount(list.size())
                .setRows(list)
                .setDiagInfo(diagInfo2Hints(hints.getDiagnose()));
    }

    private DasResult queryBySampleWithRange(DasRequest request) throws SQLException {
        return query(request);
    }

    DasDiagInfo diagInfo2Hints(DasDiagnose dasDiagnose) {
        if (dasDiagnose == null) {
            return null;
        }
        Map<String, String> diagnoseInfoMap = Maps.transformEntries(dasDiagnose.getDiagnoseInfoMap(), (key, value) -> Objects.toString(value, ""));
        List<DasDiagInfo> subs = dasDiagnose.getChildDiagnoses().stream().map(d -> diagInfo2Hints(d)).collect(Collectors.toList());
        return new DasDiagInfo()
                .setName(dasDiagnose.getName())
                .setDiagnoseInfoMap(diagnoseInfoMap)
                .setSpaceLevel(dasDiagnose.getSpaceLevel())
                .setEntries(subs);
    }

    private Hints translate(DasHints dasHints) {
        if (dasHints == null) {
            return null;
        }

        Map<DasHintEnum, String> map = dasHints.getHints();
        Hints result = new Hints();
        String dbShard = map.get(DasHintEnum.dbShard);
        if(!isNullOrEmpty(dbShard)){
            result.inShard(dbShard);
        }
        String tableShard = map.get(DasHintEnum.tableShard);
        if(!isNullOrEmpty(tableShard)){
            result.inTableShard(tableShard);
        }
        String dbShardValue = map.get(DasHintEnum.dbShardValue);
        if(!isNullOrEmpty(dbShardValue)){
            result.shardValue(dbShardValue);
        }
        String tableShardValue = map.get(DasHintEnum.tableShardValue);
        if(!isNullOrEmpty(tableShardValue)){
            result.tableShardValue(tableShardValue);
        }
        if(Boolean.valueOf(map.get(DasHintEnum.setIdentityBack))) {
            result.setIdBack();
        }
        if(Boolean.valueOf(map.get(DasHintEnum.enableIdentityInsert))) {
            result.insertWithId();
        }
        if(Boolean.valueOf(map.get(DasHintEnum.diagnoseMode))) {
            result.diagnose();
        }
        if(Boolean.valueOf(map.get(DasHintEnum.updateNullField))) {
            result.updateNullField();
        }
        if(Boolean.valueOf(map.get(DasHintEnum.crossShardsPageRoughly))) {
            result.crossShardsPageRoughly();
        }
        if(Boolean.valueOf(map.get(DasHintEnum.applyDefaultShard))) {
            result.applyDefaultShard();
        }
        String sortJson = map.get(DasHintEnum.sortColumns);
        List<ColumnOrder> columnOrders = SqlBuilderSerializer.deserializeColumnOrders(sortJson);
        if(!columnOrders.isEmpty()){
            result.sortBy(columnOrders.toArray(new ColumnOrder[columnOrders.size()]));
        }

        String excludedColumnsString = map.get(DasHintEnum.excludedColumns);
        Type jsonType = new TypeToken<Set<String>>() {}.getType();
        Set<String> excludedColumns = new Gson().fromJson(excludedColumnsString, jsonType);
        if(!excludedColumns.isEmpty()){
            result.setExcluded(excludedColumns);
        }

        return result;
    }

    private DasResult queryByPK(DasRequest request) throws SQLException {
        EntityList entities = request.getEntityList();
        checkArgument(!entities.getRows().isEmpty());

        Entity pk = fillMeta(request);
        Hints hints = translate(request.getHints());
        Entity entity = getDelegate(request).queryByPk(pk, hints);
        if (entity == null) {
            return new DasResult()
                    .setRowCount(0)
                    .setRows(Arrays.asList())
                    .setDiagInfo(diagInfo2Hints(hints.getDiagnose()));
        } else {
            return new DasResult()
                    .setRowCount(1)
                    .setRows(Arrays.asList(entity))
                    .setDiagInfo(diagInfo2Hints(hints.getDiagnose()));
        }
    }

    private DasResult countBySample(DasRequest request) throws SQLException {
        EntityList entities = request.getEntityList();
        checkArgument(!entities.getRows().isEmpty());

        Entity sample = fillMeta(request);
        Hints hints = translate(request.getHints());
        Number count = getDelegate(request).countBySample(sample, hints);
        return new DasResult()
                .setRowCount(1)
                .setRows(Arrays.asList(new Entity().setValue(new Gson().toJson(count))))
                .setDiagInfo(diagInfo2Hints(hints.getDiagnose()));
    }

    private DasResult batchInsert(DasRequest request) throws SQLException {
        EntityList entities = request.getEntityList();
        checkArgument(!entities.getRows().isEmpty());

        Hints hints = translate(request.getHints());
        int[] results = getDelegate(request).batchInsert(fillMetas(request), hints);
        return new DasResult()
                .setRowCount(results.length)
                .setRows(pojo2Entities(Ints.asList(results), null))
                .setDiagInfo(diagInfo2Hints(hints.getDiagnose()));
    }

    @Override
    public void commit(DasTransactionId tranId) throws DasException, TException {
        try {
            transServer.commit(buildTransactionId(tranId));
        } catch (SQLException e) {
            throw toDasException(e);
        }
    }

    @Override
    public void rollback(DasTransactionId tranId) throws DasException, TException {
        try {
            transServer.rollback(buildTransactionId(tranId));
        } catch (SQLException e) {
            throw toDasException(e);
        }
    }

    private String buildTransactionId(DasTransactionId tranId) {
        return TransactionId.buildUniqueId(tranId.logicDbName, tranId.serverAddress, workerId, tranId.createTime, tranId.sequenceNumber);
    }

    @Override
    public DasTransactionId start(String appId, String logicDbName, DasHints dasHints) throws DasException, TException {
        TransactionId id;
        try {
            id = transServer.start(appId, logicDbName,  translate(dasHints));
        } catch (SQLException e) {
            throw toDasException(e);
        }

        String clientAddress = "";

        DasTransactionId tranId = new DasTransactionId().
                setClientAddress(clientAddress).
                setCreateTime(id.getLast()).
                setCompleted(false).
                setLogicDbName(logicDbName).
                setPhysicalDbName(id.getPhysicalDbName()).
                setSequenceNumber(id.getIndex()).
                setRolledBack(false).
                setServerAddress(address).
                setShardId(id.getShardId());
        
        return tranId;
    }

    @Override
    public DasServerStatus check(DasCheckRequest request) throws DasException, TException {
        return monitor.getStatues();
    }

    @Override
    public TxGeneralResponse registerApplication(TxGeneralRequest req) throws TException {
        TxGeneralResponse response = new TxGeneralResponse();
        response.setXid("na");
        try {
            DasClient dasClient = DasClientFactory.getClient("das_tx");
            Txlog toInsert = new Txlog();
            toInsert.setApplicationID(req.getAppId());
            toInsert.setNodeID(req.getNode());
            toInsert.setType(req.getTxType().name());
            toInsert.setXID(req.getXid());
            toInsert.setIp(req.getIp());
            toInsert.setStatus("register");
            int result = dasClient.insert(toInsert );
           // List<Txlog> list = dasDelegate.query(SqlBuilder.selectAllFrom(Txlog.TXLOG).into(Txlog.class));
            //list.size();

            if(result == 1){
                response.setResult("success");
            } else {
                response.setResult("error");
            }
            return response;
        } catch (Exception throwables) {
            throwables.printStackTrace();
            response.setResult("error");
            response.setErrorMessage(throwables.getMessage());
            return response;
        }

    }

    @Override
    public TxGeneralResponse txBegin(TxGeneralRequest req) throws TException {
        TxGeneralResponse response = new TxGeneralResponse();

        Txlog toInsert = new Txlog();
        toInsert.setApplicationID(req.getAppId());
        toInsert.setNodeID(req.getNode());
        toInsert.setType(req.getTxType().name());

        toInsert.setIp(req.getIp());
        toInsert.setStatus("begin");

        final String xid = generateXID(req.getIp());
        toInsert.setXID(xid);
        try {
            DasClient dasClient = DasClientFactory.getClient("das_tx");
            int result = dasClient.insert(toInsert);

            if(result == 1){
                response.setResult("success");
            } else {
                response.setResult("error");
            }
            response.setXid(xid);
            return response;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            response.setResult("error");
            response.setErrorMessage(throwables.getMessage());
            return response;
        }

    }

    String generateXID(String ip) {
        return ip +":" + Math.abs(new Random().nextInt());
    }

    @Override
    public TxNodeStartResponse nodeStart(TxNodeStartRequest req) throws TException {
        return null;
    }

    @Override
    public TxGeneralResponse txCommit(TxGeneralRequest req) throws TException {
        TxGeneralResponse response = new TxGeneralResponse();

        Txlog toInsert = new Txlog();
        toInsert.setApplicationID(req.getAppId());
        toInsert.setNodeID(req.getNode());
        toInsert.setType(req.getTxType().name());

        toInsert.setIp(req.getIp());
        toInsert.setStatus("commit");

        toInsert.setXID(req.getXid());
        try {
            DasClient dasClient = DasClientFactory.getClient("das_tx");
            int result = dasClient.insert(toInsert);

            if(result == 1){
                response.setResult("success");
            } else {
                response.setResult("error");
            }
            response.setXid(req.getXid());
            return response;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            response.setResult("error");
            response.setErrorMessage(throwables.getMessage());
            return response;
        }

    }

    @Override
    public TxGeneralResponse txRollback(TxGeneralRequest req) throws TException {
        TxGeneralResponse response = new TxGeneralResponse();

        Txlog toInsert = new Txlog();
        toInsert.setApplicationID(req.getAppId());
        toInsert.setNodeID(req.getNode());
        toInsert.setType(req.getTxType().name());

        toInsert.setIp(req.getIp());
        toInsert.setStatus("rollback");

        toInsert.setXID(req.getXid());
        try {
            DasClient dasClient = DasClientFactory.getClient("das_tx");
            int result = dasClient.insert(toInsert);

            if(result == 1){
                response.setResult("success");
            } else {
                response.setResult("error");
            }
            response.setXid(req.getXid());
            return response;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            response.setResult("error");
            response.setErrorMessage(throwables.getMessage());
            return response;
        }
    }

    @Override
    public TxGeneralResponse txConfirm(TxGeneralRequest req) throws TException {
        TxGeneralResponse response = new TxGeneralResponse();

        Txlog toInsert = new Txlog();
        toInsert.setApplicationID(req.getAppId());
        toInsert.setNodeID(req.getNode());
        toInsert.setType(req.getTxType().name());

        toInsert.setIp(req.getIp());
        toInsert.setStatus("confirm");

        toInsert.setXID(req.getXid());
        try {
            DasClient dasClient = DasClientFactory.getClient("das_tx");
            int result = dasClient.insert(toInsert);

            if(result == 1){
                response.setResult("success");
            } else {
                response.setResult("error");
            }
            response.setXid(req.getXid());
            return response;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            response.setResult("error");
            response.setErrorMessage(throwables.getMessage());
            return response;
        }
    }

    @Override
    public TxGeneralResponse txCancel(TxGeneralRequest req) throws TException {
        TxGeneralResponse response = new TxGeneralResponse();

        Txlog toInsert = new Txlog();
        toInsert.setApplicationID(req.getAppId());
        toInsert.setNodeID(req.getNode());
        toInsert.setType(req.getTxType().name());

        toInsert.setIp(req.getIp());
        toInsert.setStatus("cancel");

        toInsert.setXID(req.getXid());
        try {
            DasClient dasClient = DasClientFactory.getClient("das_tx");
            int result = dasClient.insert(toInsert);

            if(result == 1){
                response.setResult("success");
            } else {
                response.setResult("error");
            }
            response.setXid(req.getXid());
            return response;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            response.setResult("error");
            response.setErrorMessage(throwables.getMessage());
            return response;
        }
    }

    @Override
    public TxGeneralResponse txConfirmFail(TxGeneralRequest req) throws TException {
        TxGeneralResponse response = new TxGeneralResponse();

        Txlog toInsert = new Txlog();
        toInsert.setApplicationID(req.getAppId());
        toInsert.setNodeID(req.getNode());
        toInsert.setType(req.getTxType().name());

        toInsert.setIp(req.getIp());
        toInsert.setStatus("confirmFail");

        toInsert.setXID(req.getXid());
        try {
            DasClient dasClient = DasClientFactory.getClient("das_tx");
            int result = dasClient.insert(toInsert);

            if(result == 1){
                response.setResult("success");
            } else {
                response.setResult("error");
            }
            response.setXid(req.getXid());
            return response;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            response.setResult("error");
            response.setErrorMessage(throwables.getMessage());
            return response;
        }
    }

    @Override
    public TxGeneralResponse txCancelFail(TxGeneralRequest req) throws TException {
        TxGeneralResponse response = new TxGeneralResponse();

        Txlog toInsert = new Txlog();
        toInsert.setApplicationID(req.getAppId());
        toInsert.setNodeID(req.getNode());
        toInsert.setType(req.getTxType().name());

        toInsert.setIp(req.getIp());
        toInsert.setStatus("cancelFail");

        toInsert.setXID(req.getXid());
        try {
            DasClient dasClient = DasClientFactory.getClient("das_tx");
            int result = dasClient.insert(toInsert);

            if(result == 1){
                response.setResult("success");
            } else {
                response.setResult("error");
            }
            response.setXid(req.getXid());
            return response;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            response.setResult("error");
            response.setErrorMessage(throwables.getMessage());
            return response;
        }
    }

    private DasException toDasException(SQLException e) {
        return new DasException().setCode(String.valueOf(e.getErrorCode())).setMessage(e.getMessage());
    }

    public static void startServer(int port) throws TTransportException, UnknownHostException, SQLException {
        final long start = System.currentTimeMillis();
        
        String address = InetAddress.getLocalHost().getHostAddress();
        
        DasServerContext serverContext = new DasServerContext(address, port);

        DasServer server = new DasServer(address, serverContext.getWorkerId());

        TBinaryProtocol.Factory protocolFactory = new TBinaryProtocol.Factory();

        DasService.Processor<DasService.Iface> processor = new DasService.Processor<>(
                server);

        int selector = Integer.parseInt(serverContext.getServerConfigure().get(SELECTING_NUMBER));
        int worker   = Integer.parseInt(serverContext.getServerConfigure().get(WORKING_NUMBER));
        TThreadedSelectorServer ttServer = new TThreadedSelectorServer(
                new TThreadedSelectorServer.Args(new TNonblockingServerSocket(port))
                        .selectorThreads(selector)
                        .processor(processor)
                        .workerThreads(worker)
                        .protocolFactory(protocolFactory));

        startupHSServer();
        logger.info("Start server duration on port [" + port + "]: [" +
                TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - start) + "] seconds.");
        ttServer.serve();
    }

    //This server is called by Ops team to check the application aliveness
    private static void startupHSServer() {
        HttpServer server;
        try {
            server = HttpServer.create(new InetSocketAddress(8085), 0);
        } catch (IOException e) {
            logger.error("Fail to startup HSServer", e);
            throw new RuntimeException(e);
        }
        server.createContext("/hs", t -> {
            String response = "OK";
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        });
        server.setExecutor(null);
        server.start();
    }

    //Create a pid file when the process is running
    private void createPIDFile(){
        try {
            final String jvmName = ManagementFactory.getRuntimeMXBean().getName();
            final int index = jvmName.indexOf('@');
            String pid = "-1";
            try {
                if (index > 0) {
                    pid = jvmName.substring(0, index);
                }
            } catch (NumberFormatException e) {
                // ignore
            }

            Path pidFile = Files.createFile(Paths.get(System.getProperty("user.dir"), pid +".pid"));
            Files.write(pidFile, (port + "").getBytes());
            pidFile.toFile().deleteOnExit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws TTransportException, UnknownHostException, SQLException {
        startServer(DEFAULT_PORT);
    }
}
