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
public class DataBaseInfoView {

    @Column(name="group_name")
    private String group_name;

    @Column(name="app_id")
    private String app_id;

    @Column(name="db_name")
    private String db_name;

    @Column(name="db_catalog")
    private String db_catalog;

    @Column(name="db_address")
    private String db_address;

    @Column(name="db_port")
    private String db_port;

    @Column(name="db_user")
    private String db_user;

    @Column(name="db_type")
    private Integer db_type;

    @Column(name="comment")
    private String comment;

    @Column(name="user_name")
    private String user_name;

}

