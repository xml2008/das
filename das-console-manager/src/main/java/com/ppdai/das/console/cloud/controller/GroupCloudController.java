package com.ppdai.das.console.cloud.controller;

import com.ppdai.das.console.cloud.dao.GroupCloudDao;
import com.ppdai.das.console.cloud.dto.model.ServiceResult;
import com.ppdai.das.console.cloud.dto.view.DasGroupItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 三、组管理
 */
@Slf4j
@RestController
@RequestMapping(value = "/das/group")
public class GroupCloudController {

    @Autowired
    private GroupCloudDao groupCloudDao;

    /**
     * 组列表
     */
    @RequestMapping(value = "/getGroupList")
    public ServiceResult<List<DasGroupItem>> getDasGroupsByWorkName(@RequestParam(value = "name", defaultValue = "") String name) throws Exception {
        return ServiceResult.success(groupCloudDao.getDasGroupsByWorkName(name));
    }

}
