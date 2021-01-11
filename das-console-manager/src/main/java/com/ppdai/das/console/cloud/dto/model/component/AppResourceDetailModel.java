package com.ppdai.das.console.cloud.dto.model.component;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppResourceDetailModel {

    private Long id;

    private Integer state;

    private String name;

    private String operator;

    private String description;
}
