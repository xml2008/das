package com.ppdai.das.console.cloud.dto.view.relation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhysicalHostView {

    private String host;
    private String port;
    private List<String> catalog;
}
