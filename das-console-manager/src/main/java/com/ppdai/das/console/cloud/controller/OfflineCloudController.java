package com.ppdai.das.console.cloud.controller;

import com.ppdai.das.console.cloud.dto.model.AppResourceBatchModel;
import com.ppdai.das.console.cloud.dto.model.component.AppResourceModel;
import com.ppdai.das.console.cloud.dto.validates.UpdateAppResourceBath;
import com.ppdai.das.console.cloud.enums.AppAccessEnum;
import com.ppdai.das.console.cloud.enums.MiddlewareTypeEnum;
import com.ppdai.das.console.cloud.service.OfflineleCloudService;
import com.ppdai.das.console.common.validates.chain.ValidateResult;
import com.ppdai.das.console.common.validates.chain.ValidatorChain;
import com.ppdai.das.console.dto.model.ServiceResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(value = "/cloud/offline")
public class OfflineCloudController {

    @Autowired
    private OfflineleCloudService offlineleCloudService;

    /**
     * 下线通知
     */
    @RequestMapping(value = "/app", method = RequestMethod.GET)
    public ServiceResult<String> offlineApp(@RequestParam("appId") String appId) throws Exception {
        if (appId == null) {
            return ServiceResult.fail("操作成功！！appId不能为空");
        }
        if (offlineleCloudService.isNotExist(appId)) {
            return ServiceResult.success(AppResourceModel.builder().appId(appId).type(MiddlewareTypeEnum.MIDDLEWARE_DAS.getType()).state(AppAccessEnum.APP_NO_ACCESS.getType()).detail(ListUtils.EMPTY_LIST).build());
        }
        offlineleCloudService.updateAppResourceRelation(AppResourceBatchModel.builder().appId(appId).selectAll(true).build());
        return offlineleCloudService.findAppResourceRelation(appId);
    }

    @RequestMapping(value = "/app/batch", method = RequestMethod.POST)
    public ServiceResult<String> updateAppResourceRelation(@Validated(UpdateAppResourceBath.class) @RequestBody AppResourceBatchModel appResourceBatchModel, Errors errors) throws Exception {
        ValidateResult validateRes = ValidatorChain.newInstance().controllerValidate(errors).validate();
        if (!validateRes.isValid()) {
            return ServiceResult.fail(validateRes.getSummarize());
        }
        return offlineleCloudService.updateAppResourceRelation(appResourceBatchModel);
    }

}
