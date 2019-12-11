package com.ppdai.das.console.cloud.service;

import com.ppdai.das.console.cloud.dao.GroupCloudDao;
import com.ppdai.das.console.cloud.dao.LoginUserCloudDao;
import com.ppdai.das.console.cloud.dto.view.DasGroupItem;
import com.ppdai.das.console.cloud.dto.view.LoginUserItem;
import com.ppdai.das.console.dao.DatabaseSetDao;
import com.ppdai.das.console.dao.LoginUserDao;
import com.ppdai.das.console.dto.entry.das.DatabaseSet;
import com.ppdai.das.console.dto.entry.das.LoginUser;
import com.ppdai.das.console.dto.model.ServiceResult;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GroupCloudService {

    @Autowired
    private GroupCloudDao groupCloudDao;

    @Autowired
    private LoginUserDao loginUserDao;

    @Autowired
    private DatabaseSetDao databaseSetDao;

    @Autowired
    private LoginUserCloudDao loginUserCloudDao;

    /**
     * 用户是否在组
     */
    public boolean isWorkNameInGrroup(Long groud_id, String work_name) throws SQLException {
        if (null == groud_id || StringUtils.isBlank(work_name)) {
            return false;
        }
        List<DasGroupItem> list = groupCloudDao.getDasGroupsByWorkName(work_name);
        if (CollectionUtils.isNotEmpty(list)) {
            list = list.stream().filter(item -> item.getId().equals(groud_id)).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(list)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 用户是否在组
     */
    public ServiceResult isWorkNoesInGrroup(Long groud_id, List<String> user_noes) throws SQLException {
        if (null == groud_id || CollectionUtils.isEmpty(user_noes)) {
            return ServiceResult.fail("组id或工号为空！！");
        }
        List<LoginUserItem> list = loginUserCloudDao.getUserByGroupId(groud_id);
        if (CollectionUtils.isNotEmpty(list)) {
            for (String userNo : user_noes) {
                boolean bool = false;
                for (LoginUserItem item : list) {
                    if (userNo.equals(item.getUserNo())) {
                        bool = true;
                    }
                }
                if (!bool) {
                    LoginUser loginUser = loginUserDao.getUserByNo(userNo);
                    if(loginUser == null){
                        return ServiceResult.fail("工号: " + userNo + " 不存在！！");
                    }
                    return ServiceResult.fail(loginUser.getUserRealName() + "不在当前组内！！");
                }
            }
        }
        return ServiceResult.success();
    }

    /**
     * 逻辑库是否在组
     */
    public ServiceResult isDbSetIdsInGrroup(Long groud_id, List<Long> db_set_ids) throws SQLException {
        if (null == groud_id) {
            return ServiceResult.fail("组id为空！！");
        }
        if (CollectionUtils.isNotEmpty(db_set_ids)) {
            List<DatabaseSet> list = databaseSetDao.getAllDatabaseSetByGroupId(groud_id);
            for (Long db_set_id : db_set_ids) {
                List<DatabaseSet> collect = list.stream().filter(item -> db_set_id.equals(item.getId())).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(collect)) {
                    DatabaseSet databaseSet = databaseSetDao.getDatabaseSetById(db_set_id);
                    if(databaseSet == null){
                        return ServiceResult.fail("逻辑库id: " + db_set_id+ " 不存在！！");
                    }
                    return ServiceResult.fail("逻辑库" + databaseSet.getName() + "不在此组内！！");
                }
            }
        }
        return ServiceResult.success();
    }
}
