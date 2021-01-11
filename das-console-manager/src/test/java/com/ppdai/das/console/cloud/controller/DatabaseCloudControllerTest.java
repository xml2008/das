package com.ppdai.das.console.cloud.controller;

import com.alibaba.fastjson.JSONObject;
import com.ppdai.das.console.api.UserConfiguration;
import com.ppdai.das.console.cloud.dto.entry.DataBaseEntry;
import com.ppdai.das.console.cloud.service.DatabaseCloudService;
import com.ppdai.das.console.common.interceptor.CommStatusInterceptor;
import com.ppdai.das.console.common.interceptor.permissions.UserLoginInterceptor;
import com.ppdai.das.console.constant.Message;
import com.ppdai.das.console.dao.*;
import com.ppdai.das.console.dto.entry.das.DasGroup;
import com.ppdai.das.console.service.DatabaseService;
import com.ppdai.das.console.service.PermissionService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringRunner.class)
@WebMvcTest(DatabaseCloudController.class)
public class DatabaseCloudControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    private UserLoginInterceptor userLoginInterceptor;

    @MockBean
    private CommStatusInterceptor commStatusInterceptor;

    @MockBean
    private DatabaseService databaseService;

    @MockBean
    private DatabaseCloudService databaseCloudService;

    @MockBean
    private GroupDao groupDao;

    @MockBean
    private Message message;

    @MockBean
    private ProjectDao projectDao;

    @MockBean
    private DataBaseDao dataBaseDao;

    @MockBean
    private LoginUserDao lLoginUserDao;

    @MockBean
    private DatabaseSetDao databaseSetDao;

    @MockBean
    private UserGroupDao userGroupDao;

    @MockBean
    private PermissionService permissionService;

    @MockBean
    private UserConfiguration userConfiguration;

    private MockMvc mockMvc;
    private String requestJson;

    @Before
    public void setupMockMvc() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build(); //初始化MockMvc对象
        requestJson = JSONObject.toJSONString(DasGroup.builder().id(1L).group_name("name").build());
    }

    @Test
    public void getdbnames() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/das/db/getdbList")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .param("appid", "1")
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void getdbTypeList() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/das/db/getdbTypeList")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }


    @Test
    public void getDateTypeList() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/das/db/getDateTypeList")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void add() throws Exception {
        DataBaseEntry dataBaseEntry = DataBaseEntry.builder().db_address("127.0.0.1").db_catalog("test").db_port("3306").db_user("root").db_password("123456").build();
        String requestJson = JSONObject.toJSONString(dataBaseEntry);
        mockMvc.perform(MockMvcRequestBuilders.post("/das/db/add")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(requestJson)
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void getDBCatalogs() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/das/db/catalogs")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .param("dbset_id", "1")
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }

}
