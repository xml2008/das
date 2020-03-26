package com.ppdai.das.core.datasource.tomcat;

public class DasTomcatDataSource implements DasTomcatDataSourceMBean {
    private DalTomcatDataSource dalTomcatDataSource;

    public DasTomcatDataSource(DalTomcatDataSource dataSource) {
        this.dalTomcatDataSource = dataSource;
    }

    @Override
    public int getActive() {
        return dalTomcatDataSource.getActive();
    }
}
