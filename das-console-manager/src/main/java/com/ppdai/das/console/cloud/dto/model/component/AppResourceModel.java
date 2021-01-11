package com.ppdai.das.console.cloud.dto.model.component;

import com.ppdai.das.console.cloud.dto.view.AppResourceRelationView;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppResourceModel {

    private String appId;

    private Integer type;

    private Integer state = 1;

    private List<AppResourceRelationView> detail;
}
