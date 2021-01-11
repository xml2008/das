package com.ppdai.das.console.cloud.dao;

import com.ppdai.das.console.api.ConfigLoader;
import com.ppdai.das.console.cloud.dto.view.LoginUserItem;
import com.ppdai.das.console.common.utils.ResourceUtil;
import com.ppdai.das.console.dto.entry.das.LoginUser;
import com.ppdai.das.core.ClientConfigureLoader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {LoginUserCloudDao.class})
public class LoginUserCloudDaoTest {

    @Autowired
    LoginUserCloudDao loginUserCloudDao;

    @MockBean
    ConfigLoader configLoader;

    @MockBean
    ClientConfigureLoader clientConfigureLoader;

    @Before
    public void setUp() {
        ResourceUtil.setClasspath("/Users/ppd-03020210/work/java/github/das/das-console-manager/target");
    }

    @Test
    public void getUserByGroupId() throws Exception {
        List<LoginUserItem> list = loginUserCloudDao.getUserByGroupId(1L);
        System.out.println("list :-------> " + list);
        Assert.assertTrue(list.size() > 0);
    }

    @Test
    public void getUserByNoes() throws Exception {
        List<String> userNos = new ArrayList<>();
        userNos.add("010892");
        List<LoginUser> list = loginUserCloudDao.getUserByNoes(userNos);
        System.out.println("list :-------> " + list);
        Assert.assertTrue(list.size() > 0);
    }
}
