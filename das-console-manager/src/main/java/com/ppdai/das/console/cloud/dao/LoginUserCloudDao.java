package com.ppdai.das.console.cloud.dao;

import com.ppdai.das.client.Parameter;
import com.ppdai.das.client.SqlBuilder;
import com.ppdai.das.console.cloud.dto.view.LoginUserItem;
import com.ppdai.das.console.dao.base.BaseDao;
import com.ppdai.das.console.dto.entry.das.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.List;

@Slf4j
@Component
public class LoginUserCloudDao extends BaseDao {

    public List<LoginUserItem> getUserByGroupId(Long groupId) throws SQLException {
        String sql = "SELECT t2.user_no, t2.user_real_name FROM user_group t1 inner JOIN login_users t2 ON t1.user_id = t2.id WHERE t1.group_id = ?";
        return this.queryBySql(sql, LoginUserItem.class, Parameter.integerOf(StringUtils.EMPTY, groupId));
    }

    public List<LoginUser> getUserByNoes(List<String> userNos) throws SQLException {
        LoginUser.LoginUsersDefinition user = LoginUser.LOGINUSER;
        SqlBuilder builder = SqlBuilder.selectAllFrom(user).where().allOf(user.userNo.in(userNos)).into(LoginUser.class);
        return this.getDasClient().query(builder);
    }
}
