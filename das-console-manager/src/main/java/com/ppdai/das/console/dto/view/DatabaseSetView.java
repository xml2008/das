package com.ppdai.das.console.dto.view;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ppdai.das.console.common.configCenter.ConfigCenterCons;
import com.ppdai.das.console.dto.model.PatternModel;
import com.ppdai.das.console.enums.DataBaseEnum;
import com.ppdai.das.console.enums.PatternDescriptionEnum;
import com.ppdai.das.console.enums.PatternTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.StringUtils;

import javax.persistence.Column;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatabaseSetView {

    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "db_type")
    private Integer dbType;

    @Column(name = "class_name")
    private String className;

    @Column(name = "group_id")
    private Long groupId;

    @Column(name = "strategy_type")
    private Integer strategyType;

    @Column(name = "strategy_source")
    private String strategySource;

    @Column(name = "dynamic_strategy_id")
    private Long dynamicStrategyId;

    @Column(name = "update_user_no")
    private String updateUserNo;

    @Column(name = "user_real_name")
    private String userRealName;

    @Column(name = "insert_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date create_time;

    @Column(name = "update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date update_time;

    @Column(name = "group_name")
    private String groupName;

    @Column(name = "pattern")
    private String pattern;

    public PatternModel getPattern() {
        if (StringUtils.isNotBlank(pattern) && !StringUtils.isNumeric(pattern)) {
            return JSON.parseObject(pattern, PatternModel.class);
        }
        return PatternModel.builder().type(PatternTypeEnum.NORMAL.getType()).description(PatternDescriptionEnum.NORMAL.getType()).build();
    }

    List<DatabaseSetEntryView> databaseSetEntryList;

    public boolean hasShardingStrategy() throws SQLException {
        String shardingStrategy = ConfigCenterCons.getShardingStrategy(this);
        if (StringUtils.isNotBlank(shardingStrategy)) {
            return true;
        }
        return false;
    }

    public String getProvider() {
        return DataBaseEnum.getDataBaseEnumByType(dbType).getProvider();
    }

    public String getShardingStrategy() throws SQLException {
        return ConfigCenterCons.getShardingStrategy(this);
    }

    public boolean getMgrEnabled() {
        return PatternTypeEnum.MGR.getType().equals(this.getPattern().getType());
    }

    public boolean getMgrReadWriteSplittingEnabled() {
        return PatternDescriptionEnum.MGRREADWRITESPLITTING.getType().equals(this.getPattern().getDescription());
    }
}
