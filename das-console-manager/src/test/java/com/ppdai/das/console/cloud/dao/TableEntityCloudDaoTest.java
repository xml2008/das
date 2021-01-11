package com.ppdai.das.console.cloud.dao;

import com.ppdai.das.console.api.ConfigLoader;
import com.ppdai.das.console.common.utils.ResourceUtil;
import com.ppdai.das.console.dao.ProjectDao;
import com.ppdai.das.console.dto.entry.das.TaskTable;
import com.ppdai.das.core.ClientConfigureLoader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {TableEntityCloudDao.class, ProjectDao.class})
public class TableEntityCloudDaoTest {

    @Autowired
    TableEntityCloudDao tableEntityCloudDao;

    @Autowired
    ProjectDao projectDao;

    @MockBean
    ConfigLoader configLoader;

    @MockBean
    ClientConfigureLoader clientConfigureLoader;

    @Before
    public void setUp() {
        ResourceUtil.setClasspath("/Users/ppd-03020210/work/java/github/das/das-console-manager/target");
    }

    @Test
    public void getTaskTableByConditon() throws Exception {
        TaskTable taskTable = TaskTable.builder().project_id(1L).build();
        TaskTable taskTable1 = tableEntityCloudDao.getTaskTableByConditon(taskTable);
        System.out.println("taskTable1 :-------> " + taskTable1);
        Assert.assertTrue(taskTable1 != null);
    }

}
