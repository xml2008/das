package com.ppdai.das.console.cloud.controller;

import com.ppdai.das.console.cloud.dto.entry.ProjectEntry;
import com.ppdai.das.console.cloud.dto.model.ProjectModel;
import com.ppdai.das.console.cloud.dto.model.ServiceResult;
import com.ppdai.das.console.cloud.dto.view.ProjectItem;
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
    public ServiceResult<ProjectModel> getProjectByAppid(@RequestParam(value = "appid", defaultValue = "") String appid) {
        try {
            return projectCloudService.getProjectByAppid(appid);
        } catch (Exception e) {
            return ServiceResult.fail("AppId:" + appid + "未接入DAS");
        }
    }

    @RequestMapping(value = "/getAppidList")
    public ServiceResult<List<String>> getAppidListByWorkName(@RequestParam(value = "name", defaultValue = "") String name) throws SQLException {
        return ServiceResult.success(projectCloudService.getAppidListByWorkName(name));
    }

    @RequestMapping(value = "/getProjectList")
    public ServiceResult<List<ProjectItem>> getProjectList(@RequestParam(value = "group_id", defaultValue = "") Long group_id) throws SQLException {
        return ServiceResult.success(projectCloudService.getProjectList(group_id));
    }

    /**
     * 1、新建project
     */
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public ServiceResult<String> add(@Validated(AddProject.class) @RequestBody ProjectEntry project, Errors errors) throws Exception {
        return projectCloudService.addProject(project, project.getWork_name(), errors);
    }

}
