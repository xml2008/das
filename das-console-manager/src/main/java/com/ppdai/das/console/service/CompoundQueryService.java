package com.ppdai.das.console.service;

import com.ppdai.das.console.common.utils.StringUtil;
import com.ppdai.das.console.dao.CompoundQueryDao;
import com.ppdai.das.console.dto.model.Paging;
import com.ppdai.das.console.dto.model.page.ListResult;
import com.ppdai.das.console.dto.model.page.PagerUtil;
import com.ppdai.das.console.dto.view.compoundQuery.AppInfoView;
import com.ppdai.das.console.dto.view.compoundQuery.DataBaseInfoView;
import com.ppdai.das.console.dto.view.compoundQuery.DbSetInfoView;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

@Service
public class CompoundQueryService {

    @Autowired
    private CompoundQueryDao compoundQueryDao;

    public ListResult<DataBaseInfoView> findDbPageListByAppids(Paging<String> paging) throws SQLException {
        paging.setData(StringUtil.toString(paging.getData()));
        Long count = compoundQueryDao.getTotalCountPageListByAppids(paging);
        return PagerUtil.find(count, paging.getPage(), paging.getPageSize(), () -> {
            List<DataBaseInfoView> list = compoundQueryDao.findDbPageListByAppids(paging);
            if (CollectionUtils.isEmpty(list)) {
                return Collections.emptyList();
            }
            return list;
        });
    }

    public ListResult<AppInfoView> findDbPageListByDBNames(Paging<String> paging) throws SQLException {
        paging.setData(StringUtil.toString(paging.getData()));
        Long count = compoundQueryDao.getTotalCountPageListByDBNames(paging);
        return PagerUtil.find(count, paging.getPage(), paging.getPageSize(), () -> {
            List<AppInfoView> list = compoundQueryDao.findDbPageListByDBNames(paging);
            if (CollectionUtils.isEmpty(list)) {
                return Collections.emptyList();
            }
            return list;
        });
    }

    public ListResult<DataBaseInfoView> findDbPageListByGroupIds(Paging<String> paging) throws SQLException {
        paging.setData(StringUtil.toString(paging.getData()));
        Long count = compoundQueryDao.getTotalCountPageListByGroupIds(paging);
        return PagerUtil.find(count, paging.getPage(), paging.getPageSize(), () -> {
            List<DataBaseInfoView> list = compoundQueryDao.findDbPageListByGroupIds(paging);
            if (CollectionUtils.isEmpty(list)) {
                return Collections.emptyList();
            }
            return list;
        });
    }

    public ListResult<DbSetInfoView> findDbSetPageListByDbNames(Paging<String> paging) throws SQLException {
        paging.setData(StringUtil.toString(paging.getData()));
        Long count = compoundQueryDao.getTotalCountDbSetPageListByDbNames(paging);
        return PagerUtil.find(count, paging.getPage(), paging.getPageSize(), () -> {
            List<DbSetInfoView> list = compoundQueryDao.findDbSetPageListByDbNames(paging);
            if (CollectionUtils.isEmpty(list)) {
                return Collections.emptyList();
            }
            return list;
        });
    }

    public ListResult<AppInfoView> findDbSetPageListByDbSetNames(Paging<String> paging) throws SQLException {
        paging.setData(StringUtil.toString(paging.getData()));
        Long count = compoundQueryDao.getTotalCountPageListByDBSetNames(paging);
        return PagerUtil.find(count, paging.getPage(), paging.getPageSize(), () -> {
            List<AppInfoView> list = compoundQueryDao.findDbSetPageListByDbSetNames(paging);
            if (CollectionUtils.isEmpty(list)) {
                return Collections.emptyList();
            }
            return list;
        });
    }

    public ListResult<DataBaseInfoView> getTotalCountDataBasePageListByDBSetNames(Paging<String> paging) throws SQLException {
        paging.setData(StringUtil.toString(paging.getData()));
        Long count = compoundQueryDao.getTotalCountPageListByDBSetNames(paging);
        return PagerUtil.find(count, paging.getPage(), paging.getPageSize(), () -> {
            List<DataBaseInfoView> list = compoundQueryDao.findDataBasePageListByDbSetNames(paging);
            if (CollectionUtils.isEmpty(list)) {
                return Collections.emptyList();
            }
            return list;
        });
    }

}
