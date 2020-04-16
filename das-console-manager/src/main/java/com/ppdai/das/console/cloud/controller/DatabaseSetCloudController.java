package com.ppdai.das.console.cloud.controller;

import com.ppdai.das.console.cloud.dto.model.ServiceResult;
import com.ppdai.das.console.cloud.dto.view.DatabaseSetItem;
import com.ppdai.das.console.cloud.service.DatabaseSetCloudService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/das/dbset")
public class DatabaseSetCloudController {

    @Autowired
    private DatabaseSetCloudService databaseSetCloudService;

    /**
     * 1、根据groupId查询逻辑数据库列表 dbset
     */
    @RequestMapping(value = "/getdbSetListByGroupId")
    public ServiceResult<List<DatabaseSetItem>> getdbSetListByGroupId(@RequestParam("group_id") Long group_id) throws Exception {
        return ServiceResult.success(databaseSetCloudService.getdbSetListByGroupId(group_id));
    }

    @RequestMapping(value = "/getdbSetListByProjectId")
    public ServiceResult<List<DatabaseSetItem>> getdbSetListByProjectId(@RequestParam("project_id") Long project_id) throws Exception {
        return ServiceResult.success(databaseSetCloudService.getdbSetListByProjectId(project_id));
    }
}