package com.ppdai.das.console.cloud.controller;

import com.ppdai.das.console.cloud.dto.model.permission.PermissionUpdateModel;
import com.ppdai.das.console.cloud.dto.view.permission.ResourcePermissionView;
import com.ppdai.das.console.cloud.service.PermissionCloudService;
import com.ppdai.das.console.dto.model.ServiceResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/cloud/permission")
public class PermissionCloudController {

    @Autowired
    private PermissionCloudService permissionCloudService;

    /**
     * 1、根据域账号查询权限列表
     */
    @RequestMapping(value = "/account/resources", method = RequestMethod.GET)
    public ServiceResult<List<ResourcePermissionView>> loadAccountPermissionList(@RequestParam("user") String user) throws Exception {
        if (StringUtils.isBlank(user)) {
            return ServiceResult.fail("user不能为空!!!");
        }
        return permissionCloudService.loadAccountPermissionList(user);
    }

    /**
     * 2、根据组内appid集合查询权限列表
     */
    @RequestMapping(value = "/appids/resources", method = RequestMethod.POST)
    public ServiceResult<List<ResourcePermissionView>> loadAppidsPermissionList(@RequestBody List<String> list) throws Exception {
        if (CollectionUtils.isEmpty(list)) {
            return ServiceResult.fail("appid 不能为空!!!");
        }
        return permissionCloudService.loadAppidsPermissionList(list);
    }

    /**
     * 3、权限更新
     */
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public ServiceResult<List<ResourcePermissionView>> update(@RequestBody PermissionUpdateModel permissionUpdateModel) throws Exception {
        if (CollectionUtils.isEmpty(permissionUpdateModel.getList())) {
            return ServiceResult.fail("list 不能为空!!!");
        }
        return permissionCloudService.update(permissionUpdateModel);
    }

}
