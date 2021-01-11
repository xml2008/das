package com.ppdai.das.console.cloud.service;

import com.ppdai.das.console.cloud.dao.OfflineCloudDao;
import com.ppdai.das.console.cloud.dto.model.AppResourceBatchModel;
import com.ppdai.das.console.cloud.dto.model.component.AppResourceModel;
import com.ppdai.das.console.cloud.dto.view.AppResourceRelationView;
import com.ppdai.das.console.cloud.enums.AppAccessEnum;
import com.ppdai.das.console.cloud.enums.MiddlewareTypeEnum;
import com.ppdai.das.console.common.exceptions.TransactionException;
import com.ppdai.das.console.dao.ProjectDao;
import com.ppdai.das.console.dto.entry.das.Project;
import com.ppdai.das.console.dto.model.ServiceResult;
import org.apache.commons.collections.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;

@Service
public class OfflineleCloudService {

    @Autowired
    private ProjectDao projectDao;

    @Autowired
    private OfflineCloudDao offlineCloudDao;

    public boolean isNotExist(String appId) throws SQLException {
        return null == projectDao.getProjectByAppId(appId);
    }

    /**
     * 更新资源下线状态:批量下线
     */
    public ServiceResult updateAppResourceRelation(AppResourceBatchModel appResourceBatchModel) {
        try {
            boolean isSussess = offlineCloudDao.getDasClient().execute(() -> {
                Project project = projectDao.getProjectByAppId(String.valueOf(appResourceBatchModel.getAppId()));
                int id = offlineCloudDao.offlineApp(appResourceBatchModel.getAppId());
                if (id <= 0) {
                    throw new TransactionException("项目下线失败!!");
                }
                id = offlineCloudDao.updateAppResourceRelation(project.getId(), appResourceBatchModel.getIds(), appResourceBatchModel.getSelectAll());
                if (id <= 0) {
                    throw new TransactionException("关联资源下线失败!!");
                }
                return true;
            });
            if (isSussess) {
                return ServiceResult.success();
            }
        } catch (SQLException e) {
            return ServiceResult.fail(e.getMessage());
        }
        return ServiceResult.fail("项目资源关联下线失败!!!");
    }

    public ServiceResult findAppResourceRelation(String appId) {
        try {
            AppResourceModel appResourceModel = AppResourceModel.builder().appId(appId).type(MiddlewareTypeEnum.MIDDLEWARE_DAS.getType()).build();
            Project project = projectDao.getProjectByAppId(appId);
            if (null == project) {
                appResourceModel.setDetail(ListUtils.EMPTY_LIST);
                appResourceModel.setState(AppAccessEnum.APP_NO_ACCESS.getType());
                return ServiceResult.success(appResourceModel);
            }
            List<AppResourceRelationView> list = offlineCloudDao.getAllProjectDbsetRelation(project.getId());
            appResourceModel.setDetail(list);
            return ServiceResult.success(appResourceModel);
        } catch (SQLException e) {
            return ServiceResult.fail(e.getMessage());
        }
    }

    private void updateResSateByType(List<AppResourceRelationView> list) {

    }

}
