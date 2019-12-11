package com.ppdai.das.console.cloud.dao;

import com.ppdai.das.console.cloud.dto.view.DasGroupItem;
import com.ppdai.das.console.dao.base.BaseDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.List;

@Slf4j
@Component
public class GroupCloudDao extends BaseDao {

    public List<DasGroupItem> getDasGroupsByWorkName(String workname) throws SQLException {
        String sql = "select t1.id, t1.group_name from dal_group t1 " +
                "inner join user_group t2 on t1.id = t2.group_id " +
                "inner join login_users t3 on t3.id = t2.user_id " +
                "where t3.user_name = '" + workname + "' order by t1.group_name";
        return this.queryBySql(sql, DasGroupItem.class);
    }

}
