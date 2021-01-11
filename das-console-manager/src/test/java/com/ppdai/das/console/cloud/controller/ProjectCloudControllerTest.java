package com.ppdai.das.console.cloud.controller;

import com.alibaba.fastjson.JSONObject;
import com.ppdai.das.console.api.UserConfiguration;
import com.ppdai.das.console.cloud.dao.GroupCloudDao;
import com.ppdai.das.console.cloud.dto.entry.ProjectEntry;
import com.ppdai.das.console.cloud.service.DatabaseSetCloudService;
import com.ppdai.das.console.cloud.service.ProjectCloudService;
import com.ppdai.das.console.common.interceptor.CommStatusInterceptor;
import com.ppdai.das.console.common.interceptor.permissions.UserLoginInterceptor;
import com.ppdai.das.console.constant.Message;
import com.ppdai.das.console.dao.*;
import com.ppdai.das.console.dto.entry.das.DasGroup;
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
@WebMvcTest(ProjectCloudController.class)
public class ProjectCloudControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    private UserLoginInterceptor userLoginInterceptor;

    @MockBean
    private CommStatusInterceptor commStatusInterceptor;

    @MockBean
    private ProjectCloudService projectCloudService;

    @MockBean
    private DatabaseSetCloudService databaseSetCloudService;

    @MockBean
    private GroupDao groupDao;

    @MockBean
    private Message message;

    @MockBean
    private ProjectDao projectDao;

    @MockBean
    private DataBaseDao dataBaseDao;

    @MockBean
    private GroupCloudDao groupCloudDao;

    @MockBean
    private DatabaseSetDao databaseSetDao;

    @MockBean
    private UserGroupDao userGroupDao;

    @MockBean
    private PermissionService permissionService;

    @MockBean
    private UserConfiguration userConfiguration;

    @MockBean
    private LoginUserDao loginUserDao;

    private MockMvc mockMvc;
    private String requestJson;

    @Before
    public void setupMockMvc() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build(); //初始化MockMvc对象
        requestJson = JSONObject.toJSONString(DasGroup.builder().id(1L).group_name("name").build());
    }

    @Test
    public void getProjectByAppid() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/das/project/getProject")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .param("appid", "10000721")
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void getAppidList() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/das/project/getAppidList")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .param("name", "tom")
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void getProjectList() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/das/project/getProjectList")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .param("group_id", "1")
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void add() throws Exception {
        ProjectEntry project = ProjectEntry.builder().app_id("123456").app_name_en("test0001").das_group_id(1L).build();
        String requestJson = JSONObject.toJSONString(project);
        mockMvc.perform(MockMvcRequestBuilders.post("/das/project/add")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(requestJson)
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }
}
