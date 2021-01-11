package com.ppdai.das.console.cloud.dto.model.permission;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleModel {

    private String id;

    private String type;

    private Boolean hasPermission;

}
