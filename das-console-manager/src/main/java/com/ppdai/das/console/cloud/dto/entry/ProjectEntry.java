package com.ppdai.das.console.cloud.dto.entry;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ppdai.das.console.common.validates.group.project.AddProject;
import com.ppdai.das.console.common.validates.group.project.UpdateProject;
import com.ppdai.das.console.dto.model.Item;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import javax.persistence.Column;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table
public class ProjectEntry {

    @NotBlank(message = "{project.app_id.notNull}", groups = {AddProject.class, UpdateProject.class})
    private String app_id;

    @NotBlank(message = "{project.name.notNull}", groups = {AddProject.class, UpdateProject.class})
    private String app_name_en;

    private String comment;

    /**
     * 项目组ID
     **/
    @NotNull(message = "{project.dal_group_id.notNull}", groups = {AddProject.class})
    private Long das_group_id;

    /**
     * 预计上线时间
     **/
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date pre_release_time;

    /**
     * 应用场景
     **/
    private String app_scene;

    /**
     * 首次上线时间
     **/
    @Column(name = "first_release_time")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date first_release_time;

    private List<Long> dbSetIds;

    private List<String> userNoes;

    private List<Long> userIds;

    private List<Item> items;

    private List<Item> users;

    public List<Item> getItems() {
        items = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(dbSetIds)) {
            for (Long dbsetid : dbSetIds) {
                items.add(new Item(dbsetid, StringUtils.EMPTY));
            }
        }
        return items;
    }

    public List<Item> getUsers() {
        users =  new ArrayList<>();
        if (CollectionUtils.isNotEmpty(dbSetIds)) {
            for (Long userid : userIds) {
                users.add(new Item(userid, StringUtils.EMPTY));
            }
        }
        return users;
    }
}