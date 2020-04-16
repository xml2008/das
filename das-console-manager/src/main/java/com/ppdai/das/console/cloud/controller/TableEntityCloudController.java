package com.ppdai.das.console.cloud.controller;

import com.ppdai.das.console.cloud.dto.model.ServiceResult;
import com.ppdai.das.console.cloud.dto.model.TablCodeModel;
import com.ppdai.das.console.cloud.dto.view.TableEntryView;
import com.ppdai.das.console.cloud.service.CodeCloudService;
import com.ppdai.das.console.common.validates.chain.ValidatorChain;
import com.ppdai.das.console.common.validates.group.tableEntity.SelectTableEntity;
import com.ppdai.das.console.dto.entry.das.TaskTable;
import com.ppdai.das.console.dto.model.Paging;
import com.ppdai.das.console.dto.model.page.ListResult;
import com.ppdai.das.console.dto.view.TaskTableView;
import com.ppdai.das.console.service.TableEntityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;

@Slf4j
@RestController
@RequestMapping(value = "/das/tableEntity")
public class TableEntityCloudController {

    @Autowired
    private CodeCloudService codeCloudService;

    @Autowired
    private TableEntityService tableEntityService;

    @RequestMapping(value = "/list", method = RequestMethod.POST)
    public ServiceResult<ListResult<TaskTableView>> list(@RequestBody Paging<TaskTable> paging) throws SQLException {
        return ServiceResult.success(tableEntityService.findTableEntityPageList(paging));
    }

    @RequestMapping(value = "/getTableEntry", method = RequestMethod.POST)
    public ServiceResult<TableEntryView> getTableEntryView(@Validated(SelectTableEntity.class) @RequestBody TablCodeModel tablCodeModel, Errors errors) throws Exception {
        ValidatorChain validatorChain = ValidatorChain.newInstance().controllerValidate(errors);
        if (!validatorChain.validate().isValid()) {
            return ServiceResult.fail(validatorChain.validate().getSummarize());
        }
        TaskTable taskTable = codeCloudService.getTaskTable(tablCodeModel);
        return ServiceResult.success(TableEntryView.builder().date_type(taskTable.getField_type()).custom_table_name(taskTable.getCustom_table_name()).custom_entry_name(taskTable.getView_names()).build());
    }
}
