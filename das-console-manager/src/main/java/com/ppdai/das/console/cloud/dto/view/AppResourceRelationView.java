package com.ppdai.das.console.cloud.dto.view;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppResourceRelationView {

    @Column(name="id")
    private Long id;

    @Column(name="name")
    private String name;

    @Column(name="state")
    private Integer state;

    @Column(name="res_state")
    private Integer resState;

    private String description;

}

