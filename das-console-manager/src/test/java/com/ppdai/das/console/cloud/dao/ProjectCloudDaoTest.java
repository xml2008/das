package com.ppdai.das.console.cloud.dao;

import com.ppdai.das.console.api.ConfigLoader;
import com.ppdai.das.console.cloud.dto.view.ProjectCloudView;
import com.ppdai.das.console.common.utils.ResourceUtil;
import com.ppdai.das.console.dao.ProjectDao;
import com.ppdai.das.console.dto.view.ProjectView;
import com.ppdai.das.core.ClientConfigureLoader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ProjectCloudDao.class, ProjectDao.class})
public class ProjectCloudDaoTest {

    @Autowired
    ProjectCloudDao projectCloudDao;

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
    public void getProjectsByWorkName() throws Exception {
        List<ProjectCloudView> list = projectCloudDao.getProjectsByWorkName("wangliang");
        System.out.println("list :-------> " + list);
        Assert.assertTrue(list.size() > 0);
    }

    @Test
    public void getProjectByAppId() throws Exception {
        ProjectView projectView = projectCloudDao.getProjectByAppId("1000002707");
        System.out.println("list :-------> " + projectView);
        Assert.assertTrue(projectView != null);
    }
}
