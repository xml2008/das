package com.ppdai.das.console.cloud.service;

import com.ppdai.das.console.cloud.dao.LoginUserCloudDao;
import com.ppdai.das.console.cloud.dao.ProjectCloudDao;
import com.ppdai.das.console.cloud.dto.entry.ProjectEntry;
import com.ppdai.das.console.cloud.dto.model.ProjectModel;
import com.ppdai.das.console.cloud.dto.model.ServiceResult;
import com.ppdai.das.console.cloud.dto.view.ProjectCloudView;
import com.ppdai.das.console.cloud.dto.view.ProjectItem;
import com.ppdai.das.console.common.utils.StringUtil;
import com.ppdai.das.console.common.validates.chain.ValidateResult;
import com.ppdai.das.console.common.validates.chain.ValidatorChain;
import com.ppdai.das.console.dao.LoginUserDao;
import com.ppdai.das.console.dao.ProjectDao;
import com.ppdai.das.console.dto.entry.das.LoginUser;
import com.ppdai.das.console.dto.entry.das.Project;
import com.ppdai.das.console.dto.view.ProjectView;
import com.ppdai.das.console.service.GroupService;
import com.ppdai.das.console.service.PermissionService;
import com.ppdai.das.console.service.ProjectService;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectCloudService {

    @Autowired
    private ProjectDao projectDao;

    @Autowired
    private GroupService groupService;

    @Autowired
    private LoginUserDao loginUserDao;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectCloudDao projectCloudDao;

    @Autowired
    private LoginUserCloudDao loginUserCloudDao;

    @Autowired
    private GroupCloudService groupCloudService;

    @Autowired
    private PermissionService permissionService;

    public List<String> getAppidListByWorkName(String workname) throws SQLException {
        if (StringUtils.isBlank(workname)) {
            return ListUtils.EMPTY_LIST;
        }
        List<ProjectCloudView> list = projectCloudDao.getProjectsByWorkName(workname);
        return list.stream().map(i -> i.getApp_id()).collect(Collectors.toList());
    }

    public ServiceResult<String> addProject(ProjectEntry projectEntry, String workName, Errors errors) throws Exception {
        ValidatorChain validatorChain = ValidatorChain.newInstance().controllerValidate(errors);
        if (!validatorChain.validate().isValid()) {
            return ServiceResult.fail(validatorChain.validate().getSummarize());
        }
        Project project = toProject(projectEntry);
        LoginUser loginUser = loginUserDao.getUserByUserName(workName);
        if (loginUser == null) {
            return ServiceResult.fail("当前用户" + workName + "未注册das，请联系管理员添加");
        }
        project.setUpdate_user_no(loginUser.getUserNo());
        ValidateResult validateRes = validatorChain
                .addAssert(() -> groupCloudService.isWorkNameInGrroup(projectEntry.getDas_group_id(), workName) || permissionService.isManagerById(loginUser.getId()), "当前用户" + workName + "不在组内,或没有权限")
                .addAssert(() -> groupCloudService.isWorkNoesInGrroup(projectEntry.getDas_group_id(), projectEntry.getUser_noes()))
                .addAssert(() -> groupCloudService.isDbSetIdsInGrroup(projectEntry.getDas_group_id(), projectEntry.getDb_set_ids()))
                .addAssert(() -> groupService.isNotExistInProjectAndGroup(project.getName()), project.getName() + " 已存在！且组名和项目名不能重复！")
                .addAssert(() -> projectDao.getCountByAppId(project.getApp_id()) == 0, "APPID:" + project.getApp_id() + " 已存在！")
                .addAssert(() -> projectService.insertProject(project)).validate();
        if (!validateRes.isValid()) {
            return ServiceResult.fail(validateRes.getSummarize());
        }
        return ServiceResult.toServiceResult(projectService.addDataCenter(loginUser, project));
    }

    public List<Long> toOserIds(List<String> userNos) throws SQLException {
        List<LoginUser> loginUsers = loginUserCloudDao.getUserByNoes(userNos);
        return loginUsers.stream().map(i -> i.getId()).collect(Collectors.toList());
    }


    public ProjectModel getProjectByAppid(String appid) throws SQLException {
        ProjectView projectView = projectCloudDao.getProjectByAppId(appid);
        return toProjectModel(projectView);
    }

    private Project toProject(ProjectEntry projectEntry) throws SQLException {
        List<Long> userIds = toOserIds(projectEntry.getUser_noes());
        projectEntry.setUserIds(userIds);
        return Project.builder()
                .app_id(projectEntry.getApp_id())
                .dal_group_id(projectEntry.getDas_group_id())
                .name(projectEntry.getApp_name_en())
                .app_scene(projectEntry.getApp_scene())
                .comment(projectEntry.getComment())
                .first_release_time(projectEntry.getFirst_release_time())
                .pre_release_time(projectEntry.getPre_release_time())
                .items(projectEntry.getItems())
                .users(projectEntry.getUsers())
                .build();
    }

    private ProjectModel toProjectModel(ProjectView projectView) throws SQLException {
        return ProjectModel.builder()
                .app_id(projectView.getApp_id())
                .app_name_en(projectView.getName())
                .comment(projectView.getComment())
                .app_scene(projectView.getApp_scene())
                .first_release_time(projectView.getFirst_release_time())
                .pre_release_time(projectView.getPre_release_time())
                .das_group_id(projectView.getDal_group_id())
                .db_set_ids(StringUtil.toLongList(projectView.getDbsetIds()))
                .user_noes(projectView.getUserNoes())
                .build();
    }

    public List<ProjectItem> getProjectList(Long group_id) throws SQLException {
        List<Project> list = projectDao.getProjectByGroupId(group_id);
        return list.stream().map(i -> new ProjectItem(i.getId(), i.getName())).collect(Collectors.toList());
    }
}
