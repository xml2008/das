package com.ppdai.das.console.cloud.controller;

import com.ppdai.das.console.cloud.dao.LoginUserCloudDao;
import com.ppdai.das.console.cloud.dto.model.ServiceResult;
import com.ppdai.das.console.cloud.dto.view.LoginUserItem;
import com.ppdai.das.console.cloud.service.ProjectCloudService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/das/user")
public class UserCloudController {

    @Autowired
    private LoginUserCloudDao loginUserCloudDao;

    @Autowired
    private ProjectCloudService projectCloudService;
    /**
     * 全部用户列表 （添加组员等用）
     */
    @RequestMapping(value = "/group/userList")
    public ServiceResult<List<LoginUserItem>> findGroupUserList(@RequestParam(value = "groupId", defaultValue = "0") Long groupId) throws Exception {
        return ServiceResult.success(loginUserCloudDao.getUserByGroupId(groupId));
    }

    /**
     * 全部用户列表 （添加组员等用）
     */
    @RequestMapping(value = "/userNos/userList")
    public ServiceResult<List<LoginUserItem>> findUserListByUserNos(@RequestBody List<String> userNos) throws Exception {
        return projectCloudService.findUserListByUserNos(userNos);
    }
}
