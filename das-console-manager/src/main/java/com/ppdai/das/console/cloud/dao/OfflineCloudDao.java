package com.ppdai.das.console.cloud.dao;

import com.ppdai.das.client.Parameter;
import com.ppdai.das.console.cloud.dto.view.AppResourceRelationView;
import com.ppdai.das.console.common.utils.StringUtil;
import com.ppdai.das.console.dao.base.BaseDao;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
public class OfflineCloudDao extends BaseDao {

    public int offlineApp(String appId) throws SQLException {
        String sql = "update project set state = 0 where app_id = ?";
        return this.updataBysql(sql, Parameter.varcharOf(StringUtils.EMPTY, appId));
    }

    public int updateAppResourceRelation(Long id, Set<Long> resIds, boolean isAll) throws SQLException {
        if (null == id) {
            return 1;
        }
        String sql = "update project_dbset_relation set state = 0 where project_id = ?";
        if (!isAll) {
            if (CollectionUtils.isEmpty(resIds)) {
                return 1;
            }
            sql = "update project_dbset_relation set state = 0 where project_id = ? and dbset_id in (" + StringUtil.joinCollectByComma(resIds) + ") ";
        }
        return this.updataBysql(sql, Parameter.varcharOf(StringUtils.EMPTY, id));
    }

    public List<AppResourceRelationView> getAllProjectDbsetRelation(Long projectId) throws SQLException {
        String sql = "select t1.dbset_id as id, t2.name, t1.state, if(t3.res_state is null, 1, t3.res_state) as res_state from project_dbset_relation t1 \n" +
                "inner join databaseset t2 on t2.id = t1.dbset_id\n" +
                "inner join (\n" +
                "\tselect t1.dbset_id, sum(t1.state), if(sum(t1.state) > 0, 1, 0) as res_state, t1.state from project_dbset_relation t1 \n" +
                "\twhere t1.dbset_id in (select dbset_id from project_dbset_relation where project_id = " + projectId + ")\n" +
                "\tgroup by t1.dbset_id\t\n" +
                ") t3 on t3.dbset_id = t1.dbset_id\n" +
                "where t1.project_id = " + projectId;
        return this.queryBySql(sql, AppResourceRelationView.class);
    }

}
