package com.ppdai.das.console.cloud.dao;

import com.ppdai.das.console.cloud.dto.view.UserGroupCloudView;
import com.ppdai.das.console.common.utils.StringUtil;
import com.ppdai.das.console.dao.base.BaseDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.List;

@Slf4j
@Component
public class PermissionCloudDao extends BaseDao {

    public List<UserGroupCloudView> loadAccountPermissionList(String user) throws SQLException {
        String sql = "select t1.id, t1.group_name, t2.role, t1.group_comment from dal_group t1\n" +
                "inner join user_group t2 on t1.id = t2.group_id\n" +
                "inner join login_users t3 on t3.id = t2.user_id\n" +
                "where t2.role = 1 and t1.id !=1 and t3.user_name = '" + user + "'";
        return this.queryBySql(sql, UserGroupCloudView.class);
    }

    public List<UserGroupCloudView> loadAppidsPermissionList(List<String> list) throws SQLException {
        String sql = "select distinct t1.id, t1.group_name, t1.group_comment from dal_group t1\n" +
                "inner join project t2 on t1.id = t2.dal_group_id\n" +
                "where t1.id !=1 and t2.app_id in  (" + StringUtil.joinCollectByComma(list, ",") + ") ";
        return this.queryBySql(sql, UserGroupCloudView.class);
    }

}

