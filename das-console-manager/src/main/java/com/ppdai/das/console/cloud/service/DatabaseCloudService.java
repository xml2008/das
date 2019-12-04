package com.ppdai.das.console.cloud.service;

import com.google.common.collect.Lists;
import com.ppdai.das.console.cloud.dao.DataBaseCloudDao;
import com.ppdai.das.console.cloud.dto.model.ServiceResult;
import com.ppdai.das.console.common.validates.chain.ValidateResult;
import com.ppdai.das.console.common.validates.chain.ValidatorChain;
import com.ppdai.das.console.common.validates.group.db.AddDataBase;
import com.ppdai.das.console.dao.DataBaseDao;
import com.ppdai.das.console.dao.GroupDao;
import com.ppdai.das.console.dao.LoginUserDao;
import com.ppdai.das.console.dto.entry.das.DasGroup;
import com.ppdai.das.console.dto.entry.das.DataBaseInfo;
import com.ppdai.das.console.dto.entry.das.LoginUser;
import com.ppdai.das.console.service.DatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

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

    /**
     * 添加物理库
     */
    public ServiceResult<String> addDataBase(@Validated(AddDataBase.class) @RequestBody DataBaseInfo dataBaseInfo, String workName, Errors errors) throws Exception {
        LoginUser loginUser = loginUserDao.getUserByUserName(workName);
        DasGroup dasGroup = groupDao.getDalGroupById(dataBaseInfo.getDal_group_id());
        if (loginUser == null) {
            return ServiceResult.fail("当前用户" + workName + "未注册das，请联系管理员添加");
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
        ValidateResult validateRes = ValidatorChain.newInstance().controllerValidate(errors)
                .addAssert(() -> dataBaseDao.getCountByName(dataBaseInfo.getDbname()) == 0, "物理库标识符" + dataBaseInfo.getDbname() + "已经存在!")
                .addAssert(() -> dataBaseCloudDao.getDataBaseInfoByConditon(condtion) == null, dataBaseInfo.getDbname() + ", 在组" + dasGroup.getGroup_name() + "已经存在 !")
                .addAssert(() -> databaseService.addDataBaseInfo(loginUser, dataBaseInfo)).validate();
        if (!validateRes.isValid()) {
            return ServiceResult.fail(validateRes.getSummarize());
        }
        return ServiceResult.toServiceResult(databaseService.addDataCenter(loginUser, Lists.newArrayList(dataBaseInfo)));
    }

}


