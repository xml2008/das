package com.ppdai.das.console.cloud.dto.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Table;
import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table
public class ProjectModel {

    private String app_id;

    private String app_name_en;

    private String comment;

    /**
     * 项目组ID
     **/
    private Long das_group_id;

    private String das_group_name;

    /**
     * 应用场景
     **/
    private String app_scene;

    /**
     * 预计上线时间
     **/
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date pre_release_time;

    /**
     * 首次上线时间
     **/
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date first_release_time;

    private List<Long> db_set_ids;

    private List<String> user_noes;
}