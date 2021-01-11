package com.ppdai.das.console.cloud.dto.model.permission;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionUpdateModel {

    private String user;

    private List<ResourcePermissionsModel> list;

}
