package com.ppdai.das.console.cloud.dao;

import com.ppdai.das.console.dao.base.BaseDao;
import com.ppdai.das.console.dto.entry.das.TaskTable;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.List;

@Slf4j
@Component
public class TableEntityCloudDao extends BaseDao {

    public TaskTable getTaskTableByConditon(TaskTable taskTable) throws SQLException {
        List<TaskTable> list = this.getDasClient().queryBySample(taskTable);
        return CollectionUtils.isNotEmpty(list) ? list.get(0) : null;
    }
}
