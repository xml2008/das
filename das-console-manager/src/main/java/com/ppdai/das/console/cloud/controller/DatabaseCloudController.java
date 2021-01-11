package com.ppdai.das.console.cloud.controller;

import com.google.common.collect.Lists;
import com.ppdai.das.console.cloud.dto.entry.DataBaseEntry;
import com.ppdai.das.console.cloud.dto.model.ServiceResult;
import com.ppdai.das.console.cloud.dto.view.DataBaseView;
import com.ppdai.das.console.cloud.dto.view.relation.LogicView;
import com.ppdai.das.console.cloud.service.DatabaseCloudService;
import com.ppdai.das.console.common.validates.group.db.AddDataBase;
import com.ppdai.das.console.dto.model.Item;
import com.ppdai.das.console.enums.DataBaseEnum;
import com.ppdai.das.console.enums.DataFieldTypeEnum;
import com.ppdai.das.console.service.DatabaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/das/db")
public class DatabaseCloudController {

    @Autowired
    private DatabaseService databaseService;

    @Autowired
    private DatabaseCloudService databaseCloudService;

    @RequestMapping(value = "/getdbList")
    public ServiceResult<List<DataBaseView>> getdbnames(@RequestParam("appid") String appid) throws SQLException {
        List<DataBaseView> list = databaseCloudService.getAllDbByAppId(appid);
        return ServiceResult.success(list);
    }

    @RequestMapping(value = "/getdbTypeList")
    public ServiceResult<List<DataBaseView>> getdbTypeList() {
        List<Item> list = Lists.newArrayList(new Item(Long.valueOf(DataBaseEnum.MYSQL.getType()), DataBaseEnum.MYSQL.getName()), new Item(Long.valueOf(DataBaseEnum.SQLSERVER.getType()), DataBaseEnum.SQLSERVER.getName()));
        return ServiceResult.success(list);
    }

    @RequestMapping(value = "/getDateTypeList")
    public ServiceResult<List<DataBaseView>> getDateTypeList() {
        List<Item> list = Lists.newArrayList(new Item(Long.valueOf(DataFieldTypeEnum.SQL_TIMESTAMP.getType()), DataFieldTypeEnum.SQL_TIMESTAMP.getDetail()), new Item(Long.valueOf(DataFieldTypeEnum.UTIL_DATE.getType()), DataFieldTypeEnum.UTIL_DATE.getDetail()));
        return ServiceResult.success(list);
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public ServiceResult<String> add(@Validated(AddDataBase.class) @RequestBody DataBaseEntry dataBaseEntry, Errors errors) throws Exception {
        return databaseCloudService.addDataBase(dataBaseEntry, dataBaseEntry.getWork_name(), errors);
    }

    @RequestMapping(value = "/catalogs")
    public ServiceResult<List<String>> getDBCatalogs(@RequestParam("dbset_id") Long dbsetId) throws Exception {
        return ServiceResult.toServiceResult(databaseService.getDBCatalogs(dbsetId));
    }

    @RequestMapping(value = "/logic/catalogs")
    public ServiceResult<List<LogicView>> getDBCatalogs() {
        try {
            return ServiceResult.success(databaseCloudService.getAllSetDbHosts());
        } catch (Exception e) {
            return ServiceResult.fail("接口异常!!!" + e.getMessage());
        }
    }
}
