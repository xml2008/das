package com.ppdai.das.server;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Preconditions;
import com.ppdai.das.core.DataBase;
import com.ppdai.das.core.DatabaseSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ppdai.das.core.DasConfigureContext;
import com.ppdai.das.core.DasConfigureFactory;
import com.ppdai.das.core.DasConfigure;
import com.ppdai.das.core.ServerConfigureLoader;
import com.ppdai.das.core.helper.ServiceLoaderHelper;
import com.ppdai.das.core.status.StatusManager;
import com.ppdai.das.core.task.SqlRequestExecutor;

/**
 * @author hejiehui
 */
public class DasServerContext {
    private static Logger logger = LoggerFactory.getLogger(DasServerVersion.getLoggerName());

    private static final String NEW_LINE = System.lineSeparator();
    private String workerId;

    private String serverGroup;
    private Map<String, String> serverConfigure;
    private ServerConfigureLoader serverLoader;
    
    public DasServerContext(String address, int port) {
        workerId = String.format("Server-%s-%d", address, port);
        Map<String, DasConfigure> configureMap = new HashMap<>();

        try {
            logger.info("Load Das Server configure");
            serverLoader = ServiceLoaderHelper.getInstance(ServerConfigureLoader.class);
            if(serverLoader == null)
                return;

            serverGroup = serverLoader.getServerGroupId(address, port);
            logger.info("Das Server Group: [" + serverGroup + "]");

            serverConfigure = serverLoader.getServerConfigure();
            logger.info("Das Server Configures: " + serverConfigure);
            
            String poolSize = serverConfigure.get(SqlRequestExecutor.MAX_POOL_SIZE);
            String keepAliveTime = serverConfigure.get(SqlRequestExecutor.KEEP_ALIVE_TIME);

            SqlRequestExecutor.init(poolSize, keepAliveTime);
            StatusManager.initializeGlobal();
            Set<String> appIds = serverLoader.getAppIds(serverGroup);
            DasConfigureContext configContext = createDasConfigureContext(appIds, address, port);
            DasConfigureFactory.initialize(configContext);
            refreshAppIds(address, port);
        } catch (Throwable e) {
            throw new IllegalStateException("Das Server Context initilization fail", e);
        }
    }

    DasConfigureContext createDasConfigureContext(Set<String> appIds, String address, int port) throws Exception {
        Map<String, DasConfigure> configureMap = new HashMap<>();

        for (String appId : appIds) {//load app ids from multiple app groups
            DasConfigure config = serverLoader.load(appId);
            Preconditions.checkNotNull(config, "Can not load dal confiure for app: " + appId);
            configureMap.put(appId, config);
        }
        logDalConfigure(configureMap, address, port);
        return new DasConfigureContext(configureMap, serverLoader.getDasLogger(), serverLoader.getTaskFactory(), serverLoader.getConnectionLocator(), serverLoader.getDatabaseSelector());
    }

    void refreshAppIds(String address, int port) {
        Executors.newScheduledThreadPool(2).scheduleAtFixedRate(() -> {
            try{
                Set<String> appIds = serverLoader.getAppIds(serverGroup);
                if(!appIds.equals(DasConfigureFactory.getAppIds())){
                    DasConfigureContext configContext = createDasConfigureContext(appIds, address, port);
                    DasConfigureFactory.refresh(configContext);
                    logger.info("application ids changed, and reloaded");
                }
            } catch (Exception e) {
                logger.error("Exception occurs in refreshAppIds", e);
            }
        }, 1, 5, TimeUnit.MINUTES);
    }

    void logDalConfigure( Map<String, DasConfigure> configureMap, String address, int port){
        StringBuilder sb = new StringBuilder();
        sb.append("This server address: [" + address + "], port: [" + port + "]" ).append(NEW_LINE);
        sb.append("Server group: [" + serverGroup + "]").append(NEW_LINE);
        sb.append("Server configuration: [" + serverConfigure + "]").append(NEW_LINE);
        for(Map.Entry<String, DasConfigure> en : configureMap.entrySet()) {
            DasConfigure dalConfigure = en.getValue();
            sb.append("DalConfigure for appId [" + en.getKey() + "], its logicDBs: " + dalConfigure.getDatabaseSetNames()).append(NEW_LINE);
            for(String logicDB: dalConfigure.getDatabaseSetNames()){
                sb.append("    logicDB: [" + logicDB + "]").append(NEW_LINE);
                DatabaseSet dbSet = dalConfigure.getDatabaseSet(logicDB);
                for(Map.Entry<String, DataBase> db: dbSet.getDatabases().entrySet()){
                    sb.append("        |- DataBase: [" + db.getKey() + "] -> ConnectionString: [" + db.getValue().getConnectionString() + "], Sharding: [" + db.getValue().getSharding() + "]").append(NEW_LINE);
                }
            }
        }
        logger.info(sb.toString());
    }

    /**
     * Return NULL if not found
     * @param ip
     * @param port
     * @return 
     */
    public String getWorkerId(){
        return workerId;
    }
    
    public Set<String> getAppIds() {
        return new HashSet<>(serverConfigure.keySet());
    }
    
    public Map<String, String> getServerConfigure() {
        return new HashMap<String, String>(serverConfigure);
    }
    
    public String getServerGroup() {
        return serverGroup;
    }
}
