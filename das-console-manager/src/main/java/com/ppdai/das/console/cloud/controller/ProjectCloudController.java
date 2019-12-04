package com.ppdai.das.console.cloud.controller;

import com.ppdai.das.console.cloud.dto.entry.ProjectEntry;
import com.ppdai.das.console.cloud.dto.model.ServiceResult;
import com.ppdai.das.console.cloud.service.ProjectCloudService;
import com.ppdai.das.console.common.validates.group.project.AddProject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/das/project")
public class ProjectCloudController {

    @Autowired
    private ProjectCloudService projectCloudService;

    @RequestMapping(value = "/getProject")
    public ServiceResult<List<ProjectEntry>> getProjectByAppid(@RequestParam(value = "appid", defaultValue = "") String appid) throws SQLException {
        return ServiceResult.success(projectCloudService.getProjectByAppid(appid));
    }

    @RequestMapping(value = "/getAppidList")
    public ServiceResult<List<String>> getAppidListByWorkName(@RequestParam(value = "name", defaultValue = "") String name) throws SQLException {
        return ServiceResult.success(projectCloudService.getAppidListByWorkName(name));
    }

    /**
     * 1、新建project
     */
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public ServiceResult<String> add(@Validated(AddProject.class) @RequestBody ProjectEntry project, Errors errors) throws Exception {
        return projectCloudService.addProject(project, "wangliang", errors);
    }

}
