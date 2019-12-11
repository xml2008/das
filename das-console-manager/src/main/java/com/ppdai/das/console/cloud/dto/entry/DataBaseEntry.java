package com.ppdai.das.console.cloud.dto.entry;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ppdai.das.console.common.validates.group.db.AddDataBase;
import com.ppdai.das.console.common.validates.group.db.UpdateDataBase;
import com.ppdai.das.console.common.validates.group.groupdb.TransferGroupDB;
import com.ppdai.das.console.dto.view.search.CheckTypes;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.StringUtils;

import javax.persistence.Column;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

/**
 * create by das-console
 * 请勿修改此文件
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table
public class DataBaseEntry {

    @NotBlank(message = "{dalgroupdb.dbname.notNull}", groups = {AddDataBase.class, UpdateDataBase.class})
    private String db_name;

    @NotNull(message = "{dalgroupdb.dal_group_id.notNull}", groups = {AddDataBase.class})
    private Long das_group_id;

    @NotBlank(message = "{dalgroupdb.db_address.notNull}", groups = {AddDataBase.class, UpdateDataBase.class})
    private String db_address;

    @NotBlank(message = "{dalgroupdb.db_port.notNull}", groups = {AddDataBase.class, UpdateDataBase.class})
    private String db_port;

    @NotBlank(message = "{dalgroupdb.db_user.notNull}", groups = {AddDataBase.class, UpdateDataBase.class})
    private String db_user;

    @NotBlank(message = "{dalgroupdb.db_password.notNull}", groups = {AddDataBase.class, UpdateDataBase.class})
    private String db_password;

    @NotBlank(message = "{dalgroupdb.db_catalog.notNull}", groups = {AddDataBase.class, UpdateDataBase.class})
    private String db_catalog;

    /**
     * 数据库类型：1、mysql 2、SqlServer
     **/
    @NotNull(message = "{dalgroupdb.db_type.notNull}", groups = {AddDataBase.class})
    private Integer db_type;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date create_time;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date update_time;

    /**
     * 操作人域账号
     **/
    @NotBlank(message = "{project.work_name.notNull}", groups = {AddDataBase.class})
    private String work_name;

    /**
     * 最后操作人
     **/
    @Column(name = "update_user_no")
    private String updateUserNo;

    private boolean addToGroup;

    private boolean isGenDefault;

    private String group_name;

    @NotNull(message = "{dalgroupdb.dal_group_id.notNull}", groups = {TransferGroupDB.class})
    private Long target_dal_group_id;

    private CheckTypes db_types;

    private List<String> insert_times;

    public String getDb_name() {
        if (StringUtils.isNotBlank(db_name)) {
            return db_name.trim();
        }
        return db_name;
    }

    public String getDb_catalog() {
        if (StringUtils.isNotBlank(db_catalog)) {
            return db_catalog.trim();
        }
        return db_catalog;
    }

    public void setDb_name(String db_name) {
        if (StringUtils.isNotBlank(db_name)) {
            this.db_name = db_name.trim();
            return;
        }
        this.db_name = db_name;
    }

    public void setDb_catalog(String db_catalog) {
        if (StringUtils.isNotBlank(db_catalog)) {
            this.db_catalog = db_catalog.trim();
            return;
        }
        this.db_catalog = db_catalog;
    }

}