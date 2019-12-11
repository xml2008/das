package com.ppdai.das.console.service;

import com.ppdai.das.console.common.codeGen.CodeGenConsts;
import com.ppdai.das.console.common.codeGen.generator.java.context.JavaCodeGenContext;
import com.ppdai.das.console.common.codeGen.generator.java.generator.JavaDasGenerator;
import com.ppdai.das.console.common.codeGen.resource.ProgressResource;
import com.ppdai.das.console.common.user.UserContext;
import com.ppdai.das.console.constant.Consts;
import com.ppdai.das.console.dao.DeleteCheckDao;
import com.ppdai.das.console.dto.entry.codeGen.Progress;
import com.ppdai.das.console.dto.entry.das.LoginUser;
import com.ppdai.das.console.dto.model.GenerateCodeModel;
import com.ppdai.das.console.dto.model.ServiceResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;

@Slf4j
@Service
public class CodeService {

    @Autowired
    private Consts consts;

    @Autowired
    private DeleteCheckDao deleteCheckDao;

    public boolean isTaskCountByProjectId(Long projectId) throws SQLException {
        return deleteCheckDao.isProjectIdInTaskTable(projectId) || deleteCheckDao.isProjectIdInTaskSQL(projectId);
    }

    public ServiceResult<String> getFileContent(String projectId, String name) throws Exception {
        if (StringUtils.isBlank(projectId) || StringUtils.isBlank(name)) {
            return ServiceResult.fail("projectId 或 name 为空!!!");
        }
        File f = new File(new File(new File(consts.codeConsoleilePath, projectId), CodeGenConsts.JAVA), name);
        StringBuilder sb = new StringBuilder();
        if (f.exists()) {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(f));

                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                    sb.append(System.getProperty("line.separator"));
                }
            } catch (Throwable e) {
                log.error(e.getMessage());
                throw e;
            } finally {
                if (null != reader) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return ServiceResult.success(sb.toString());
    }

    public ServiceResult generateProject(GenerateCodeModel generateCodeRequest, HttpServletRequest request) {
        Long project_id = generateCodeRequest.getProjectId();
        Progress progress = null;
        LoginUser user = UserContext.getUser(request);
        try {
            progress = ProgressResource.getProgress(user.getUserNo(), project_id, StringUtils.EMPTY);

            String code = CodeGenConsts.JAVA;
            JavaDasGenerator generator = new JavaDasGenerator();
            JavaCodeGenContext context = generator.createContext(project_id, true, progress);
            context.setGeneratePath(consts.codeConsoleilePath);

            log.info(String.format("Begin to generate java task for project %s", project_id));
            generateLanguageProject(generator, context);
            log.info(String.format("Java task for project %s generated.", project_id));

            return ServiceResult.success(code);
        } catch (NullPointerException ne) {
            ne.printStackTrace();
            return ServiceResult.fail("表结构发生变化，请删除变更表再新建表实体！");
        } catch (Throwable e) {
            e.printStackTrace();
            return ServiceResult.fail(e.getMessage());
        } finally {
            progress.setStatus(ProgressResource.FINISH);
        }
    }

    private void generateLanguageProject(JavaDasGenerator generator, JavaCodeGenContext context) throws Exception {
        if (generator == null || context == null) {
            return;
        }
        generator.prepareDirectory(context);
        generator.prepareData(context);
        generator.generateCode(context);
    }

    public ServiceResult generateTable(GenerateCodeModel generateCodeRequest, HttpServletRequest request, Long db_set_id) {
        Long project_id = generateCodeRequest.getProjectId();
        Progress progress = null;
        LoginUser user = UserContext.getUser(request);
        try {
            progress = ProgressResource.getProgress(user.getUserNo(), project_id, StringUtils.EMPTY);

            String code = CodeGenConsts.JAVA;
            JavaDasGenerator generator = new JavaDasGenerator();
            JavaCodeGenContext context = generator.createContext(project_id, true, progress);
            context.setGeneratePath(consts.codeConsoleilePath);

            log.info(String.format("Begin to generate java task for project %s", project_id));
            generateLanguageTable(generator, context, db_set_id);
            log.info(String.format("Java task for project %s generated.", project_id));

            return ServiceResult.success(code);
        } catch (NullPointerException ne) {
            ne.printStackTrace();
            return ServiceResult.fail("表结构发生变化，请删除变更表再新建表实体！");
        } catch (Throwable e) {
            e.printStackTrace();
            return ServiceResult.fail(e.getMessage());
        } finally {
            progress.setStatus(ProgressResource.FINISH);
        }
    }

    private void generateLanguageTable(JavaDasGenerator generator, JavaCodeGenContext context, Long db_set_id) throws Exception {
        if (generator == null || context == null) {
            return;
        }
        generator.prepareDirectory(context);
        generator.prepareTable(context, db_set_id);
        generator.generateCode(context);
    }

}