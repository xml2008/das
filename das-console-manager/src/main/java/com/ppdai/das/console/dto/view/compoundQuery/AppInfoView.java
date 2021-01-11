package com.ppdai.das.console.dto.view.compoundQuery;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppInfoView {

    @Column(name="group_name")
    private String group_name;

    @Column(name="app_id")
    private String app_id;

    @Column(name="name")
    private String name;

    @Column(name="db_catalog")
    private String db_catalog;

    @Column(name="user_name")
    private String user_name;

    @Column(name="comment")
    private String comment;

}

