package com.ppdai.das.console.dao;

import com.ppdai.das.console.common.utils.SelectCoditonBuilder;
import com.ppdai.das.console.dao.base.BaseDao;
import com.ppdai.das.console.dto.model.Paging;
import com.ppdai.das.console.dto.view.compoundQuery.AppInfoView;
import com.ppdai.das.console.dto.view.compoundQuery.DataBaseInfoView;
import com.ppdai.das.console.dto.view.compoundQuery.DbSetInfoView;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.List;

@Slf4j
@Component
public class CompoundQueryDao extends BaseDao {

    private String getPageListByAppidsSql(Paging<String> paging, boolean isCount) {
        StringBuffer sql = new StringBuffer("select t3.group_name, t2.app_id, t1.db_name,  t1.db_address, t1.db_port, t1.db_user, t1.db_catalog, t1.db_type,t2.user_name, t1.comment from alldbs t1\n" +
                "\tinner join (\n" +
                "        \t\t\tselect a1.db_Id, a4.app_id, group_concat(distinct a6.user_real_name) user_name from databasesetentry a1\n" +
                "                \tinner join databaseset a2 on a1.dbset_id = a2.id\n" +
                "                \tinner join project_dbset_relation a3 on a3.dbset_id = a2.id\n" +
                "                \tinner join project a4 on a4.id = project_id\n" +
                "                \tleft join user_project a5 on a5.project_id = a4.id\n" +
                "                \tleft join login_users a6 on a6.id = a5.user_id\n" +
                "                \twhere a4.app_id in (" + paging.getData() + ")\n" +
                "                \tgroup by a1.db_Id, a4.app_id\n" +
                "               ) t2 on t1.id= t2.db_Id \n" +
                "\tinner join dal_group t3 on t3.id = t1.dal_group_id\n" +
                "    order by t2.app_id");
        if (isCount) {
            return "select count(1) from (" + sql.toString() + ") t";
        }
        return sql.toString();
    }

    public Long getTotalCountPageListByAppids(Paging<String> paging) throws SQLException {
        return this.getCount(this.getPageListByAppidsSql(paging, true));
    }

    public List<DataBaseInfoView> findDbPageListByAppids(Paging<String> paging) throws SQLException {
        String sql = this.getPageListByAppidsSql(paging, false) + this.appenCondition(paging);
        System.out.println(sql);
        return this.queryBySql(sql, DataBaseInfoView.class);
    }

    private String getPageListByDBNamesSql(Paging<String> paging, boolean isCount) {
        StringBuffer sql = new StringBuffer("select distinct t0.group_name, t1.app_id, t1.name, t1.app_scene as comment,group_concat(distinct t5.db_catalog) db_catalog, group_concat(distinct t7.user_real_name) user_name from project t1 \n" +
                "\tinner join dal_group t0 on t0.id = t1.dal_group_id\n" +
                "\tinner join project_dbset_relation t2 on t2.project_id = t1.id\n" +
                "\tinner join databaseset t3 on t3.id = t2.dbset_id\n" +
                "\tinner join databasesetentry t4 on t4.dbset_id = t3.id\n" +
                "\tinner join alldbs t5 on t5.id = t4.db_Id \n" +
                "\tleft join user_project t6 on t6.project_id = t1.id\n" +
                "\tleft join login_users t7 on t7.id = t6.user_id\n" +
                "\twhere t5.db_catalog in (" + paging.getData() + ")\n" +
                "\tgroup by t1.app_id\n" +
                "order by t0.group_name, t1.app_id");
        if (isCount) {
            return "select count(1) from (" + sql.toString() + ") t";
        }
        return sql.toString();
    }

    public Long getTotalCountPageListByDBNames(Paging<String> paging) throws SQLException {
        return this.getCount(this.getPageListByDBNamesSql(paging, true));
    }

    public List<AppInfoView> findDbPageListByDBNames(Paging<String> paging) throws SQLException {
        String sql = this.getPageListByDBNamesSql(paging, false) + this.appenCondition(paging);
        return this.queryBySql(sql, AppInfoView.class);
    }

    private String getPageListByGroupIdsSql(Paging<String> paging, boolean isCount) {
        StringBuffer sql = new StringBuffer("select t3.group_name, t2.app_id, t1.id, t1.db_name,t1.dal_group_id, t1.db_address, t1.db_port, t1.db_user, t1.db_catalog, t1.db_type, t2.user_name, t1.comment from alldbs t1\n" +
                "\tinner join (\n" +
                "\t\t\tselect a1.db_Id, a4.app_id,group_concat(distinct a7.user_real_name) user_name from databasesetentry a1\n" +
                "\t\t\t\tinner join databaseset a2 on a1.dbset_id = a2.id\n" +
                "\t\t\t\tinner join project_dbset_relation a3 on a3.dbset_id = a2.id\n" +
                "\t\t\t\tinner join project a4 on a4.id = project_id\n" +
                "\t\t\t\tinner join dal_group a5 on a4.dal_group_id = a5.id\n" +
                "\t\t\t\tleft join user_project a6 on a6.project_id = a4.id\n" +
                "\t\t\t\tleft join login_users a7 on a7.id = a6.user_id\n" +
                "\t\t\twhere a5.id in (" + paging.getData() + ")\n" +
                "\t\t\tgroup by a1.db_Id, a4.app_id\n" +
                "\t) t2 on t1.id= t2.db_Id  \n" +
                "\tinner join dal_group t3 on t3.id = t1.dal_group_id\n" +
                "order by t3.group_name, t2.app_id");
        if (isCount) {
            return "select count(1) from (" + sql.toString() + ") t";
        }
        return sql.toString();
    }

    public Long getTotalCountPageListByGroupIds(Paging<String> paging) throws SQLException {
        return this.getCount(this.getPageListByGroupIdsSql(paging, true));
    }

    public List<DataBaseInfoView> findDbPageListByGroupIds(Paging<String> paging) throws SQLException {
        String sql = this.getPageListByGroupIdsSql(paging, false) + this.appenCondition(paging);
        return this.queryBySql(sql, DataBaseInfoView.class);
    }

    private String getDbSetPageListByDbNamesSql(Paging<String> paging, boolean isCount) {
        StringBuffer sql = new StringBuffer("select distinct t4.group_name, group_concat(distinct t6.app_id) app_id, t1.name, t1.strategy_source, t3.db_catalog from databaseset t1 \n" +
                "inner join databasesetentry t2 on t2.dbset_id = t1.id \n" +
                "inner join alldbs t3 on t3.id = t2.db_Id \n" +
                "inner join dal_group t4 on t1.group_id = t4.id \n" +
                "inner join project_dbset_relation t5 on t5.dbset_id = t1.id\n" +
                "inner join project t6 on t6.id = t5.project_id\n" +
                "where t3.db_catalog in  (" + paging.getData() + ")\n" +
                "group by t1.name \n" +
                "order by t4.group_name, t3.db_catalog ");
        if (isCount) {
            return "select count(1) from (" + sql.toString() + ") t";
        }
        return sql.toString();
    }

    public Long getTotalCountDbSetPageListByDbNames(Paging<String> paging) throws SQLException {
        return this.getCount(this.getDbSetPageListByDbNamesSql(paging, true));
    }

    public List<DbSetInfoView> findDbSetPageListByDbNames(Paging<String> paging) throws SQLException {
        String sql = this.getDbSetPageListByDbNamesSql(paging, false) + this.appenCondition(paging);
        return this.queryBySql(sql, DbSetInfoView.class);
    }

    private String getPageListByDbSetNamesSql(Paging<String> paging, boolean isCount) {
        StringBuffer sql = new StringBuffer("select distinct t0.group_name, t1.app_id, t1.name, t1.app_scene as comment, group_concat(distinct t5.user_real_name) user_name from project t1 \n" +
                "inner join dal_group t0 on t0.id = t1.dal_group_id\n" +
                "inner join project_dbset_relation t2 on t2.project_id = t1.id\n" +
                "inner join databaseset t3 on t3.id = t2.dbset_id\n" +
                "left join user_project t4 on t4.project_id = t1.id\n" +
                "left join login_users t5 on t5.id = t4.user_id\n" +
                "where t3.name in (" + paging.getData() + ")\n" +
                "group by t1.app_id\n" +
                "order by t1.app_id");
        if (isCount) {
          return "select count(1) from (" + sql.toString() + ") t";
        }
        return sql.toString();
    }

    public Long getTotalCountPageListByDBSetNames(Paging<String> paging) throws SQLException {
        return this.getCount(this.getPageListByDbSetNamesSql(paging, true));
    }

    public List<AppInfoView> findDbSetPageListByDbSetNames(Paging<String> paging) throws SQLException {
        String sql = this.getPageListByDbSetNamesSql(paging, false) + this.appenCondition(paging);
        return this.queryBySql(sql, AppInfoView.class);
    }

    private String getDataBasePageListByDbSetNamesSql(Paging<String> paging, boolean isCount) {
        StringBuffer sql = new StringBuffer("select distinct t0.group_name, group_concat(distinct t5.app_id) app_id, t1.db_name, t1.dal_group_id, t1.db_address, t1.db_port, t1.db_user, t1.db_catalog, t1.db_type  from alldbs t1\n" +
                "inner join dal_group t0 on t0.id = t1.dal_group_id\n" +
                "inner join databasesetentry t2 on t2.db_Id = t1.id\n" +
                "inner join databaseset t3 on t3.id = t2.dbset_id\n" +
                "inner join project_dbset_relation t4 on t4.dbset_id = t3.id\n" +
                "inner join project t5 on t5.id = t4.project_id\n" +
                "where t3.name in (" + paging.getData() + ")\n" +
                "group by t1.id\n" +
                "order by t0.group_name, t1.db_catalog");
        if (isCount) {
            return "select count(1) from (" + sql.toString() + ") t";
        }
        return sql.toString();
    }

    public Long getTotalCountDataBasePageListByDBSetNames(Paging<String> paging) throws SQLException {
        return this.getCount(this.getDataBasePageListByDbSetNamesSql(paging, true));
    }

    public List<DataBaseInfoView> findDataBasePageListByDbSetNames(Paging<String> paging) throws SQLException {
        String sql = this.getDataBasePageListByDbSetNamesSql(paging, false) + this.appenCondition(paging);
        return this.queryBySql(sql, DataBaseInfoView.class);
    }

    private String appenCondition(Paging<String> paging) {
        return SelectCoditonBuilder.getInstance()
                .orderBy(paging)
                .limit(paging)
                .builer();
    }
}
