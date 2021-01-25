package com.ppdai.das.console.cloud.service;

import com.ppdai.das.console.api.EncdecConfiguration;
import com.ppdai.das.console.api.UserConfiguration;
import com.ppdai.das.console.cloud.dao.PermissionCloudDao;
import com.ppdai.das.console.cloud.dto.model.permission.PermissionUpdateModel;
import com.ppdai.das.console.cloud.dto.model.permission.ResourcePermissionsModel;
import com.ppdai.das.console.cloud.dto.model.permission.RoleModel;
import com.ppdai.das.console.cloud.dto.view.UserGroupCloudView;
import com.ppdai.das.console.cloud.dto.view.permission.ResourcePermissionView;
import com.ppdai.das.console.cloud.dto.view.permission.RoleView;
import com.ppdai.das.console.common.exceptions.TransactionException;
import com.ppdai.das.console.dao.LoginUserDao;
import com.ppdai.das.console.dao.UserGroupDao;
import com.ppdai.das.console.dto.entry.das.LoginUser;
import com.ppdai.das.console.dto.entry.das.UserGroup;
import com.ppdai.das.console.dto.model.ServiceResult;
import com.ppdai.das.console.dto.model.UserIdentityModel;
import com.ppdai.das.console.enums.RoleTypeEnum;
import com.ppdai.das.console.service.PermissionService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PermissionCloudService {

    @Autowired
    private LoginUserDao loginUserDao;

    @Autowired
    private UserGroupDao userGroupDao;

    @Autowired
    private PermissionCloudDao permissionCloudDao;

    @Autowired
    private UserConfiguration userConfiguration;

    @Autowired
    private EncdecConfiguration encdecConfiguration;

    /**
     * 根据域账号查询权限列表
     */
    public ServiceResult loadAccountPermissionList(String user) {
        try {
            List<ResourcePermissionView> rs = new ArrayList<>();
            List<UserGroupCloudView> list = permissionCloudDao.loadAccountPermissionList(user);
            for (UserGroupCloudView dto : list) {
                rs.add(ResourcePermissionView.builder()
                        .id(String.valueOf(dto.getId()))
                        .type("项目组")
                        .name(dto.getGroup_name())
                        .description(dto.getGroup_comment())
                        .roleList(create(dto))
                        .build());
            }
            return ServiceResult.success(rs);
        } catch (SQLException e) {
            return ServiceResult.fail("根据域账号查询权限列表失败!!! " + e.getMessage());
        }
    }

    private List<RoleView> create(UserGroupCloudView dto) {
        List<RoleView> list = new ArrayList<>();
        if (dto.getRole() == RoleTypeEnum.Admin.getType()) {
            list.add(RoleView.builder().id(String.valueOf(RoleTypeEnum.Admin.getType())).type("组管理员").hasPermission(true).build());
        }
        /*if (dto.getRole() == RoleTypeEnum.Admin.getType()) {
            list.add(RoleView.builder().id(String.valueOf(RoleTypeEnum.Admin.getType())).type("管理员").hasPermission(true).build());
            list.add(RoleView.builder().id(String.valueOf(RoleTypeEnum.Limited.getType())).type("组员").hasPermission(false).build());
        } else {
            list.add(RoleView.builder().id(String.valueOf(RoleTypeEnum.Admin.getType())).type("管理员").hasPermission(false).build());
            list.add(RoleView.builder().id(String.valueOf(RoleTypeEnum.Limited.getType())).type("组员").hasPermission(true).build());
        }*/
        return list;
    }

    public ServiceResult loadAppidsPermissionList(List<String> appids) {
        try {
            List<ResourcePermissionView> rs = new ArrayList<>();
            List<UserGroupCloudView> list = permissionCloudDao.loadAppidsPermissionList(appids);
            for (UserGroupCloudView dto : list) {
                rs.add(ResourcePermissionView.builder()
                        .id(String.valueOf(dto.getId()))
                        .type("项目组")
                        .name(dto.getGroup_name())
                        .description(dto.getGroup_comment())
                        .roleList(new ArrayList<RoleView>() {
                            {
                                add(RoleView.builder().id(String.valueOf(RoleTypeEnum.Admin.getType())).type("组管理员").hasPermission(false).build());
                            }
                        })
                        .build());
            }
            return ServiceResult.success(rs);
        } catch (SQLException e) {
            return ServiceResult.fail("根据域账号查询权限列表失败!!! " + e.getMessage());
        }
    }

    public ServiceResult update(PermissionUpdateModel permissionUpdateModel) {
        try {
            Long userId = createUserId(permissionUpdateModel.getUser());
            for (ResourcePermissionsModel dto : permissionUpdateModel.getList()) {
                ServiceResult st = updateRole(userId, Long.valueOf(dto.getId()), dto.getRoleList());
                if (st.getCode() == ServiceResult.ERROR) {
                    return st;
                }
            }
            return ServiceResult.success();
        } catch (Exception e) {
            return ServiceResult.fail("权限更新失败!!! " + e.getMessage());
        }
    }

    private ServiceResult updateRole(Long userId, Long groupId, List<RoleModel> roleList) {
        RoleModel admin = roleList.stream().filter(i -> Integer.parseInt(i.getId()) == RoleTypeEnum.Admin.getType()).collect(Collectors.toList()).get(0);
        ServiceResult st = ServiceResult.success();
        if (admin.getHasPermission()) {
            st = replace(userId, groupId, RoleTypeEnum.Admin.getType());
        } else {
            st = delete(userId, groupId);
        }
        return st;
    }

    private ServiceResult updateRoleback(Long userId, Long groupId, List<RoleModel> roleList) {
        RoleModel admin = roleList.stream().filter(i -> Integer.parseInt(i.getId()) == RoleTypeEnum.Admin.getType()).collect(Collectors.toList()).get(0);
        RoleModel limited = roleList.stream().filter(i -> Integer.parseInt(i.getId()) == RoleTypeEnum.Limited.getType()).collect(Collectors.toList()).get(0);
        ServiceResult st = ServiceResult.success();
        if (admin.getHasPermission()) {
            st = replace(userId, groupId, RoleTypeEnum.Admin.getType());
        } else if (!admin.getHasPermission() && limited.getHasPermission()) {
            st = replace(userId, groupId, RoleTypeEnum.Limited.getType());
        } else if (!admin.getHasPermission() && !limited.getHasPermission()) {
            st = delete(userId, groupId);
        }
        return st;
    }

    private ServiceResult replace(Long userId, Long groupId, Integer role) {
        try {
            boolean isSussess = permissionCloudDao.getDasClient().execute(() -> {
                List<UserGroup> list = userGroupDao.getUserGroupByGroupIdAndUserId(groupId, userId);
                if (CollectionUtils.isNotEmpty(list)) {
                    for (UserGroup userGroup : list) {
                        if (!role.equals(userGroup.getRole())) {
                            int flag = userGroupDao.updateUserPersimion(userGroup.getUser_id(), userGroup.getGroup_id(), role, 1);
                            if (flag <= 0) {
                                throw new TransactionException("更新权限失败!!");
                            }
                        }
                    }
                } else {
                    Long id = userGroupDao.insertUserGroup(userId, groupId, role, 1);
                    if (id <= 0) {
                        throw new TransactionException("新建权限失败!!");
                    }
                }
                return true;
            });
            if (isSussess) {
                return ServiceResult.success();
            }
        } catch (SQLException e) {
            return ServiceResult.fail(e.getMessage());
        }
        return ServiceResult.fail("更新权限失败!!!");
    }

    private ServiceResult delete(Long userId, Long groupId) {
        try {
            boolean isSussess = permissionCloudDao.getDasClient().execute(() -> {
                List<UserGroup> list = userGroupDao.getUserGroupByGroupIdAndUserId(groupId, userId);
                if (CollectionUtils.isNotEmpty(list)) {
                    int id = userGroupDao.deleteUserFromGroup(userId, groupId);
                    if (id <= 0) {
                        throw new TransactionException("删除权限失败!!");
                    }
                }
                return true;
            });
            if (isSussess) {
                return ServiceResult.success();
            }
        } catch (SQLException e) {
            return ServiceResult.fail(e.getMessage());
        }
        return ServiceResult.fail("删除权限失败!!!");
    }

    private Long createUserId(String userName) throws Exception {
        LoginUser loginUser = loginUserDao.getUserByUserName(userName);
        if (loginUser == null) {
            UserIdentityModel userIdentity = (UserIdentityModel) userConfiguration.getUserIdentityByWorkName(LoginUser.builder().build(), userName);
            LoginUser user = LoginUser.builder()
                    .userRealName(userIdentity.getUserRealName())
                    .userEmail(userIdentity.getUserEmail())
                    .userName(userIdentity.getUserName())
                    .password(encdecConfiguration.parseUnidirection("111111"))
                    .userNo(userIdentity.getWorkNumber())
                    .update_user_no(String.valueOf(PermissionService.getSUPERID()))
                    .build();
            return loginUserDao.insertUser(user);
        } else {
            return loginUser.getId();
        }
    }
}
