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
public class DbSetInfoView {

    @Column(name="group_name")
    private String group_name;

    @Column(name="name")
    private String name;

    @Column(name="strategy_source")
    private String strategy_source;

    @Column(name="db_catalog")
    private String db_catalog;

}

