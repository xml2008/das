package com.ppdai.das.core.datasource;

import java.lang.management.ManagementFactory;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.sql.DataSource;

import com.ppdai.das.core.datasource.tomcat.DasTomcatDataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ppdai.das.core.configure.DataSourceConfigure;
import com.ppdai.das.core.configure.DataSourceConfigureConstants;
import com.ppdai.das.core.datasource.tomcat.DalTomcatDataSource;
import com.ppdai.das.core.helper.ConnectionPhantomReferenceCleaner;
import com.ppdai.das.core.helper.DefaultConnectionPhantomReferenceCleaner;
import com.ppdai.das.core.helper.PoolPropertiesHelper;
import com.ppdai.das.core.helper.ServiceLoaderHelper;
import com.ppdai.das.core.log.Callback;
import com.ppdai.das.core.log.DefaultLoggerImpl;
import com.ppdai.das.core.log.ILogger;

public class SingleDataSource implements DataSourceConfigureConstants {
    private static final Logger LOGGER = LoggerFactory.getLogger(SingleDataSource.class);
    private PoolPropertiesHelper poolPropertiesHelper = PoolPropertiesHelper.getInstance();
    private String name;
    private DataSourceConfigure dataSourceConfigure;
    private DataSource dataSource;

    private static final String DAL = "DAL";
    private static final String DATASOURCE_CREATE_DATASOURCE = "DataSource::createDataSource:%s";
    private static ILogger ilogger = ServiceLoaderHelper.getInstance(ILogger.class, DefaultLoggerImpl.class);

    private static ConnectionPhantomReferenceCleaner connectionPhantomReferenceCleaner = new DefaultConnectionPhantomReferenceCleaner();
    private static AtomicBoolean containsMySQL=new AtomicBoolean(false);
    private static final String MYSQL_URL_PREFIX = "jdbc:mysql://";
    public static final String JMX_TOMCAT_DATASOURCE = "TomcatDataSource";

    public String getName() {
        return name;
    }

    public DataSourceConfigure getDataSourceConfigure() {
        return dataSourceConfigure;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public SingleDataSource(String name, DataSourceConfigure dataSourceConfigure) throws SQLException {
        if (dataSourceConfigure == null) {
            throw new SQLException("Can not find any connection configure for " + name);
        }

        try {
            this.name = name;
            this.dataSourceConfigure = dataSourceConfigure;

            PoolProperties p = poolPropertiesHelper.convert(dataSourceConfigure);
            PoolPropertiesHolder.getInstance().setPoolProperties(p);
            final org.apache.tomcat.jdbc.pool.DataSource dataSource = new DalTomcatDataSource(p);
            this.dataSource = dataSource;

            String message = String.format("Datasource[name=%s, Driver=%s] created,connection url:%s", name,
                    p.getDriverClassName(), dataSourceConfigure.getConnectionUrl());
            ilogger.logTransaction(DAL, String.format(DATASOURCE_CREATE_DATASOURCE, name), message, new Callback() {
                @Override
                public void execute() throws Exception {
                    dataSource.createPool();
                }
            });
            registerDataSource();

            LOGGER.info(message);
        } catch (Throwable e) {
            LOGGER.error(String.format("Error creating pool for data source %s", name), e);
        }

        try {
            if (!containsMySQL.get()) {
                if (dataSourceConfigure.getConnectionUrl().startsWith(MYSQL_URL_PREFIX)){
                    connectionPhantomReferenceCleaner.start();
                    containsMySQL.set(true);
                }
            }
        } catch (Throwable e) {
            LOGGER.error(String.format("Error starting pool connectionPhantomReferenceCleaner"), e);
        }
    }

    private void registerDataSource() throws MalformedObjectNameException, NotCompliantMBeanException, InstanceAlreadyExistsException, MBeanRegistrationException, InstanceNotFoundException {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName objectName = new ObjectName(JMX_TOMCAT_DATASOURCE, "type", name);
        if(mbs.isRegistered(objectName)) {
            mbs.unregisterMBean(objectName);
        }
        DasTomcatDataSource dasTomcatDataSource = new DasTomcatDataSource((DalTomcatDataSource)dataSource);
        mbs.registerMBean(dasTomcatDataSource, objectName) ;
    }

    private void testConnection(org.apache.tomcat.jdbc.pool.DataSource dataSource) throws SQLException {
        if (dataSource == null) {
            return;
        }

        Connection con = null;
        try {
            con = dataSource.getConnection();
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (Throwable e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
    }

}
