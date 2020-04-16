package com.ppdai.das.console.cloud.dto.model;

import com.ppdai.das.console.common.validates.group.tableEntity.ContentTableEntity;
import com.ppdai.das.console.common.validates.group.tableEntity.SelectTableEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table
public class TablCodeModel {

    @NotNull(message = "{tablCodeModel.project_id.notNull}", groups = {ContentTableEntity.class, SelectTableEntity.class})
    private Long project_id;

    @NotNull(message = "{tablCodeModel.db_set_id.notNull}", groups = {ContentTableEntity.class, SelectTableEntity.class})
    private Long db_set_id;

    @NotNull(message = "{tablCodeModel.date_type.notNull}", groups = {ContentTableEntity.class})
    private Integer date_type;

    @NotBlank(message = "{tablCodeModel.catalog_table_name.notNull}", groups = {ContentTableEntity.class, SelectTableEntity.class})
    private String catalog_table_name;

    @NotBlank(message = "{tablCodeModel.custom_table_name.notNull}", groups = {ContentTableEntity.class})
    private String custom_table_name;

    @NotBlank(message = "{tablCodeModel.custom_entry_name.notNull}", groups = {ContentTableEntity.class})
    private String custom_entry_name;

    @NotBlank(message = "{project.work_name.notNull}", groups = {ContentTableEntity.class})
    private String work_name;

}
