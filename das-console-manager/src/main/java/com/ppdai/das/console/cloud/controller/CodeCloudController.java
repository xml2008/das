package com.ppdai.das.console.cloud.controller;

import com.ppdai.das.console.cloud.dto.model.ServiceResult;
import com.ppdai.das.console.cloud.dto.model.TablCodeModel;
import com.ppdai.das.console.cloud.service.CodeCloudService;
import com.ppdai.das.console.common.validates.group.tableEntity.ContentTableEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping(value = "/das/code")
public class CodeCloudController {

    @Autowired
    private CodeCloudService codeCloudService;

    @RequestMapping(value = "/content", method = RequestMethod.POST)
    public ServiceResult<String> getFileContent(@Validated(ContentTableEntity.class) @RequestBody TablCodeModel tablCodeModel, HttpServletRequest request, Errors errors) throws Exception {
        return codeCloudService.getFileContent(tablCodeModel, request, errors);
    }
}
