package com.ppdai.das.console.cloud.dao;

import com.ppdai.das.console.api.ConfigLoader;
import com.ppdai.das.console.cloud.dto.view.DasGroupItem;
import com.ppdai.das.console.common.utils.ResourceUtil;
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
@SpringBootTest(classes = {GroupCloudDao.class})
public class GroupCloudDaoTest {

    @Autowired
    GroupCloudDao groupCloudDao;

    @MockBean
    ConfigLoader configLoader;

    @MockBean
    ClientConfigureLoader clientConfigureLoader;

    @Before
    public void setUp() {
        ResourceUtil.setClasspath("/Users/ppd-03020210/work/java/github/das/das-console-manager/target");
    }

    @Test
    public void getDasGroupsByWorkName() throws Exception {
        List<DasGroupItem> list = groupCloudDao.getDasGroupsByWorkName("wangliang");
        System.out.println("list :-------> " + list);
        Assert.assertTrue(list.size() > 0);
    }
}
