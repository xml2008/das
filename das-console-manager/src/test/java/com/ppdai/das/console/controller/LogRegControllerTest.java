package com.ppdai.das.console.controller;

import com.alibaba.fastjson.JSONObject;
import com.ppdai.das.console.api.EncdecConfiguration;
import com.ppdai.das.console.api.UserConfiguration;
import com.ppdai.das.console.common.interceptor.CommStatusInterceptor;
import com.ppdai.das.console.common.interceptor.permissions.UserLoginInterceptor;
import com.ppdai.das.console.constant.Message;
import com.ppdai.das.console.dao.LoginUserDao;
import com.ppdai.das.console.dto.entry.das.LoginUser;
import com.ppdai.das.console.service.PermissionService;
import com.ppdai.das.console.service.UserService;
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
@WebMvcTest(LogRegController.class)
public class LogRegControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    private UserLoginInterceptor userLoginInterceptor;

    @MockBean
    private CommStatusInterceptor commStatusInterceptor;

    @MockBean
    private PermissionService permissionService;

    @MockBean
    private Message message;

    @MockBean
    private UserService userService;

    @MockBean
    private LoginUserDao loginUserDao;

    @MockBean
    private UserConfiguration userConfiguration;

    @MockBean
    private EncdecConfiguration encdecConfiguration;

    private MockMvc mockMvc;
    private String requestJson;
    private LoginUser loginUser;

    @Before
    public void setupMockMvc() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build(); //初始化MockMvc对象
        requestJson = JSONObject.toJSONString(LoginUser.builder().id(1L).userName("tom").build());
        loginUser = LoginUser.builder().userName("tom").userEmail("tom@12.com").build();
    }

    @Test
    public void signup() throws Exception {
        String requestJson = JSONObject.toJSONString(loginUser);
        mockMvc.perform(MockMvcRequestBuilders.post("/logReg/initAdminInfo")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(requestJson)
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void register() throws Exception {
        String requestJson = JSONObject.toJSONString(loginUser);
        mockMvc.perform(MockMvcRequestBuilders.post("/logReg/register")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(requestJson)
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void login() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/logReg/login")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(requestJson)
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void simulateLogin() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/logReg/simulateLogin")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(requestJson)
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }
}
