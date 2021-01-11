package com.ppdai.das.console.cloud.dto.model;

import com.ppdai.das.console.cloud.dto.validates.UpdateAppResourceBath;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppResourceBatchModel {

    @NotNull(message = "{appResourceBatchModel.appId.notNull}", groups = {UpdateAppResourceBath.class})
    private String appId;

    private Boolean selectAll;

    @NotNull(message = "{appResourceBatchModel.ids.notNull}", groups = {UpdateAppResourceBath.class})
    private Set<Long> ids;

}

