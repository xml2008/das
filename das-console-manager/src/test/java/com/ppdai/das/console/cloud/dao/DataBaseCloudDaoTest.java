package com.ppdai.das.console.cloud.dao;

import com.ppdai.das.console.api.ConfigLoader;
import com.ppdai.das.console.cloud.dto.view.DataBaseView;
import com.ppdai.das.console.common.utils.ResourceUtil;
import com.ppdai.das.console.dto.entry.das.DataBaseInfo;
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
@SpringBootTest(classes = {DataBaseCloudDao.class})
public class DataBaseCloudDaoTest {

    @Autowired
    DataBaseCloudDao dataBaseCloudDao;

    @MockBean
    ConfigLoader configLoader;

    @MockBean
    ClientConfigureLoader clientConfigureLoader;

    @Before
    public void setUp() {
        ResourceUtil.setClasspath("/Users/ppd-03020210/work/java/github/das/das-console-manager/target");
    }

    @Test
    public void getAllDbByAppId() throws Exception {
        List<DataBaseView> list = dataBaseCloudDao.getAllDbByAppId("1000003330");
        System.out.println("list :-------> " + list);
        Assert.assertTrue(list.size() > 0);
    }

    @Test
    public void getDataBaseInfoByConditon() throws Exception {
        DataBaseInfo dataBaseInfo = DataBaseInfo.builder().db_catalog("ppdai_ac_risk").build();
        DataBaseInfo dataBaseInfo1 = dataBaseCloudDao.getDataBaseInfoByConditon(dataBaseInfo);
        System.out.println("getDataBaseInfoByConditon :-------> " + dataBaseInfo1);
        Assert.assertTrue(dataBaseInfo1 != null);
    }
}
