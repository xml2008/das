package com.ppdai.das.console.cloud.service;

import com.google.common.collect.Lists;
import com.ppdai.das.console.cloud.dao.DataBaseCloudDao;
import com.ppdai.das.console.cloud.dto.entry.DataBaseEntry;
import com.ppdai.das.console.cloud.dto.model.ServiceResult;
import com.ppdai.das.console.cloud.dto.view.DataBaseView;
import com.ppdai.das.console.common.validates.chain.ValidateResult;
import com.ppdai.das.console.common.validates.chain.ValidatorChain;
import com.ppdai.das.console.dao.DataBaseDao;
import com.ppdai.das.console.dao.GroupDao;
import com.ppdai.das.console.dao.LoginUserDao;
import com.ppdai.das.console.dto.entry.das.DasGroup;
import com.ppdai.das.console.dto.entry.das.DataBaseInfo;
import com.ppdai.das.console.dto.entry.das.LoginUser;
import com.ppdai.das.console.service.DatabaseService;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;

import java.sql.SQLException;
import java.util.List;

@Service
public class DatabaseCloudService {

    @Autowired
    private GroupDao groupDao;

    @Autowired
    private DataBaseDao dataBaseDao;

    @Autowired
    private LoginUserDao loginUserDao;

    @Autowired
    private DatabaseService databaseService;

    @Autowired
    private DataBaseCloudDao dataBaseCloudDao;

    public List<DataBaseView> getAllDbByAppId(String appid) throws SQLException {
        if (StringUtils.isBlank(appid)) {
            return ListUtils.EMPTY_LIST;
        }
        return dataBaseCloudDao.getAllDbByAppId(appid);
    }

    /**
     * 添加物理库
     */
    public ServiceResult<String> addDataBase(DataBaseEntry dataBaseEntry, String workName, Errors errors) throws Exception {
        ValidatorChain validatorChain = ValidatorChain.newInstance().controllerValidate(errors);
        if (!validatorChain.validate().isValid()) {
            return ServiceResult.fail(validatorChain.validate().getSummarize());
        }
        DataBaseInfo dataBaseInfo = toDataBaseInfo(dataBaseEntry);
        LoginUser loginUser = loginUserDao.getUserByUserName(workName);
        if (loginUser == null) {
            return ServiceResult.fail("当前用户" + workName + "未注册das，请联系管理员添加");
        }
        DasGroup dasGroup = groupDao.getDalGroupById(dataBaseInfo.getDal_group_id());
        if (dasGroup == null) {
            return ServiceResult.fail("当前组id不存在！！！");
        }
        DataBaseInfo condtion = DataBaseInfo.builder()
                .db_type(dataBaseInfo.getDb_type())
                .db_address(dataBaseInfo.getDb_address())
                .db_port(dataBaseInfo.getDb_port())
                .db_catalog(dataBaseInfo.getDb_catalog())
                .db_user(dataBaseInfo.getDb_user())
                .db_password(dataBaseInfo.getDb_password())
                .dal_group_id(dataBaseInfo.getDal_group_id())
                .build();

        dataBaseInfo.setUpdateUserNo(loginUser.getUserNo());
        ValidateResult validateRes = validatorChain
                .addAssert(() -> dataBaseDao.getCountByName(dataBaseInfo.getDbname()) == 0, "物理库标识符" + dataBaseInfo.getDbname() + "已经存在!")
                .addAssert(() -> dataBaseCloudDao.getDataBaseInfoByConditon(condtion) == null, dataBaseInfo.getDbname() + ", 在组" + dasGroup.getGroup_name() + "已经存在 !")
                .addAssert(() -> databaseService.addDataBaseInfo(loginUser, dataBaseInfo)).validate();
        if (!validateRes.isValid()) {
            return ServiceResult.fail(validateRes.getSummarize());
        }
        return ServiceResult.toServiceResult(databaseService.addDataCenter(loginUser, Lists.newArrayList(dataBaseInfo)));
    }

    public DataBaseInfo toDataBaseInfo(DataBaseEntry dataBaseEntry) {
        return DataBaseInfo.builder()
                .db_type(dataBaseEntry.getDb_type())
                .dbname(dataBaseEntry.getDb_name())
                .db_address(dataBaseEntry.getDb_address())
                .db_port(dataBaseEntry.getDb_port())
                .db_user(dataBaseEntry.getDb_user())
                .db_password(dataBaseEntry.getDb_password())
                .db_catalog(dataBaseEntry.getDb_catalog())
                .dal_group_id(dataBaseEntry.getDas_group_id())
                .build();
    }
}


