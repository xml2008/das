package com.ppdai.das.console.cloud.service;

import com.ppdai.das.console.cloud.dto.view.DatabaseSetItem;
import com.ppdai.das.console.dao.DatabaseSetDao;
import com.ppdai.das.console.dto.entry.das.DatabaseSet;
import com.ppdai.das.console.dto.view.DatabaseSetView;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DatabaseSetCloudService {

    @Autowired
    private DatabaseSetDao databaseSetDao;

    /**
     * 1、根据groupId查询逻辑数据库列表 dbset
     */
    public List<DatabaseSetItem> getdbSetListByGroupId(Long groupId) throws Exception {
        if (groupId == null) {
            return ListUtils.EMPTY_LIST;
        }
        List<DatabaseSet> databaseSets = databaseSetDao.getAllDatabaseSetByGroupId(groupId);
        if (CollectionUtils.isNotEmpty(databaseSets)) {
            return databaseSets.stream().map(item -> new DatabaseSetItem(item.getId(), item.getName())).collect(Collectors.toList());
        }
        return ListUtils.EMPTY_LIST;
    }

    public List<DatabaseSetItem> getdbSetListByProjectId(Long project_id) throws Exception {
        if (project_id == null) {
            return ListUtils.EMPTY_LIST;
        }
        List<DatabaseSetView> databaseSets = databaseSetDao.getAllDatabaseSetByProjectId(project_id);
        if (CollectionUtils.isNotEmpty(databaseSets)) {
            return databaseSets.stream().map(item -> new DatabaseSetItem(item.getId(), item.getName())).collect(Collectors.toList());
        }
        return ListUtils.EMPTY_LIST;
    }
}
