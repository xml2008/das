package com.ppdai.das.console.controller;

import com.ppdai.das.console.config.annotation.CurrentUser;
import com.ppdai.das.console.dao.GroupDao;
import com.ppdai.das.console.dto.entry.das.DasGroup;
import com.ppdai.das.console.dto.entry.das.LoginUser;
import com.ppdai.das.console.dto.model.Paging;
import com.ppdai.das.console.dto.model.ServiceResult;
import com.ppdai.das.console.dto.model.page.ListResult;
import com.ppdai.das.console.dto.view.compoundQuery.AppInfoView;
import com.ppdai.das.console.dto.view.compoundQuery.DataBaseInfoView;
import com.ppdai.das.console.dto.view.compoundQuery.DbSetInfoView;
import com.ppdai.das.console.service.CompoundQueryService;
import com.ppdai.das.console.service.PermissionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/compound/query")
public class CompoundQueryController {

    @Autowired
    private GroupDao groupDao;

    @Autowired
    private CompoundQueryService compoundQueryService;

    @Autowired
    private PermissionService permissionService;

    /**
     * 1、根据 appid 查询物理库信息
     */
    @RequestMapping(value = "/db/page/by/appids", method = RequestMethod.POST)
    public ServiceResult<ListResult<DataBaseInfoView>> findDbPageListByAppids(@RequestBody Paging<String> paging) throws SQLException {
        if (StringUtils.isBlank(paging.getData())) {
            return ServiceResult.success(ListResult.builder().list(ListUtils.EMPTY_LIST).build());
        }
        return ServiceResult.success(compoundQueryService.findDbPageListByAppids(paging));
    }


    /**
     * 2、根据物理库名查询所有项目
     */
    @RequestMapping(value = "/app/page/by/db/names", method = RequestMethod.POST)
    public ServiceResult<ListResult<AppInfoView>> findDbPageListByDBNames(@RequestBody Paging<String> paging) throws SQLException {
        if (StringUtils.isBlank(paging.getData())) {
            return ServiceResult.success(ListResult.builder().list(ListUtils.EMPTY_LIST).build());
        }
        return ServiceResult.success(compoundQueryService.findDbPageListByDBNames(paging));
    }

    /**
     * 3、根据项目组查询物理库信息
     */
    @RequestMapping(value = "/db/page/by/group/ids", method = RequestMethod.POST)
    public ServiceResult<ListResult<DataBaseInfoView>> findDbPageListByGroupIds(@RequestBody Paging<String> paging) throws SQLException {
        if (StringUtils.isBlank(paging.getData())) {
            return ServiceResult.success(ListResult.builder().list(ListUtils.EMPTY_LIST).build());
        }
        return ServiceResult.success(compoundQueryService.findDbPageListByGroupIds(paging));
    }

    /**
     * 4、根据物理库名查询逻辑库
     */
    @RequestMapping(value = "/dbset/page/by/db/names", method = RequestMethod.POST)
    public ServiceResult<ListResult<DbSetInfoView>> findDbSetPageListByDbNames(@RequestBody Paging<String> paging) throws SQLException {
        if (StringUtils.isBlank(paging.getData())) {
            return ServiceResult.success(ListResult.builder().list(ListUtils.EMPTY_LIST).build());
        }
        return ServiceResult.success(compoundQueryService.findDbSetPageListByDbNames(paging));
    }

    /**
     * 5、根据逻辑库名查询所有项目
     */
    @RequestMapping(value = "/app/page/by/dbset/names", method = RequestMethod.POST)
    public ServiceResult<ListResult<AppInfoView>> findDbSetPageListByDbSetNames(@RequestBody Paging<String> paging) throws SQLException {
        if (StringUtils.isBlank(paging.getData())) {
            return ServiceResult.success(ListResult.builder().list(ListUtils.EMPTY_LIST).build());
        }
        return ServiceResult.success(compoundQueryService.findDbSetPageListByDbSetNames(paging));
    }


    /**
     * 6、根据逻辑库名查询物理库
     */
    @RequestMapping(value = "/db/page/by/dbset/names", method = RequestMethod.POST)
    public ServiceResult<ListResult<AppInfoView>> getTotalCountDataBasePageListByDBSetNames(@RequestBody Paging<String> paging) throws SQLException {
        if (StringUtils.isBlank(paging.getData())) {
            return ServiceResult.success(ListResult.builder().list(ListUtils.EMPTY_LIST).build());
        }
        return ServiceResult.success(compoundQueryService.getTotalCountDataBasePageListByDBSetNames(paging));
    }

    /**
     * 查询所有组信息
     *
     * @throws Exception
     */
    @RequestMapping(value = "/group/tree")
    public ServiceResult<List<DasGroup>> getAllGroups(@CurrentUser LoginUser loginUser) throws Exception {
        if (permissionService.isManagerById(loginUser.getId())) {
            return ServiceResult.success(groupDao.getAllGroupsWithNotAdmin());
        }
        return ServiceResult.success(groupDao.getGroupsByUserId(loginUser.getId()));
    }

}
