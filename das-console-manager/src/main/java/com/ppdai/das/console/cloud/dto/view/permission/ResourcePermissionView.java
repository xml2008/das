package com.ppdai.das.console.cloud.dto.view.permission;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourcePermissionView {

    private String id;

    private String type;

    private String name;

    private String description;

    private List<RoleView> roleList;

    private List<ResourcePermissionView> children;

}
