package com.ppdai.das.console.cloud.service;

import com.ppdai.das.console.cloud.dao.TableEntityCloudDao;
import com.ppdai.das.console.cloud.dto.model.ServiceResult;
import com.ppdai.das.console.cloud.dto.model.TablCodeModel;
import com.ppdai.das.console.common.codeGen.CodeGenConsts;
import com.ppdai.das.console.common.codeGen.generator.processor.prepareDirectory.DirectoryPreparerProcessor;
import com.ppdai.das.console.common.validates.chain.ValidatorChain;
import com.ppdai.das.console.dao.LoginUserDao;
import com.ppdai.das.console.dao.TableEntityDao;
import com.ppdai.das.console.dto.entry.das.TaskTable;
import com.ppdai.das.console.dto.model.GenerateCodeModel;
import com.ppdai.das.console.service.CodeService;
import com.ppdai.das.console.service.TableEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;

import javax.servlet.http.HttpServletRequest;

@Service
public class CodeCloudService {

    @Autowired
    private TableEntityDao tableEntityDao;

    @Autowired
    private CodeService codeService;

    @Autowired
    private TableEntityService tableEntityService;

    @Autowired
    private TableEntityCloudDao tableEntityCloudDao;

    @Autowired
    private LoginUserDao loginUserDao;

    public ServiceResult<String> getFileContent(TablCodeModel tablCodeModel, HttpServletRequest request, Errors errors) throws Exception {
        ValidatorChain validatorChain = ValidatorChain.newInstance().controllerValidate(errors);
        if (!validatorChain.validate().isValid()) {
            return ServiceResult.fail(validatorChain.validate().getSummarize());
        }
        TaskTable taskTable = getTaskTable(tablCodeModel);
        if (taskTable == null) {
            taskTable = TaskTable.builder()
                    .project_id(tablCodeModel.getProject_id())
                    .dbset_id(tablCodeModel.getDb_set_id())
                    .table_names(tablCodeModel.getCatalog_table_name())
                    .custom_table_name(tablCodeModel.getCustom_table_name())
                    .view_names(tablCodeModel.getCustom_entry_name())
                    .field_type(tablCodeModel.getDate_type())
                    .update_user_no(loginUserDao.getUserByUserName(tablCodeModel.getWork_name()).getUserNo())
                    .build();
            tableEntityService.insertTask(taskTable);
        } else if (taskTable != null && !isNeedUpateTaskTable(tablCodeModel, taskTable)) {
            taskTable.setCustom_table_name(tablCodeModel.getCustom_table_name());
            taskTable.setView_names(tablCodeModel.getCustom_entry_name());
            taskTable.setField_type(tablCodeModel.getDate_type());
            taskTable.setUpdate_user_no(loginUserDao.getUserByUserName(tablCodeModel.getWork_name()).getUserNo());
            tableEntityDao.updateTask(taskTable);
        }
        codeService.generateTable(GenerateCodeModel.builder().projectId(taskTable.getProject_id()).build(), request, taskTable.getId());
        return ServiceResult.toServiceResult(codeService.getFileContent(String.valueOf(tablCodeModel.getProject_id()), DirectoryPreparerProcessor.TABLE_FILE_NAME + "/" + tablCodeModel.getCustom_entry_name() + "." + CodeGenConsts.JAVA));
    }


    public TaskTable getTaskTable(TablCodeModel tablCodeModel) throws Exception {
        return tableEntityCloudDao.getTaskTableByConditon(TaskTable.builder().project_id(tablCodeModel.getProject_id()).dbset_id(tablCodeModel.getDb_set_id()).table_names(tablCodeModel.getCatalog_table_name()).build());
    }

    private boolean isNeedUpateTaskTable(TablCodeModel tablCodeModel, TaskTable taskTable) {
        return tablCodeModel.getCustom_table_name().equals(taskTable.getCustom_table_name())
                && tablCodeModel.getCustom_entry_name().equals(taskTable.getView_names())
                && tablCodeModel.getDate_type().equals(taskTable.getField_type());
    }

}