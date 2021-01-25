package com.ppdai.das.console.controller;

import com.ppdai.das.console.common.codeGen.CodeGenConsts;
import com.ppdai.das.console.common.utils.DateUtil;
import com.ppdai.das.console.common.utils.FileUtils;
import com.ppdai.das.console.constant.Consts;
import com.ppdai.das.console.dao.ProjectDao;
import com.ppdai.das.console.dto.entry.codeGen.W2uiElement;
import com.ppdai.das.console.dto.entry.das.Project;
import com.ppdai.das.console.dto.model.GenerateCodeModel;
import com.ppdai.das.console.dto.model.ServiceResult;
import com.ppdai.das.console.service.CodeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/code")
public class CodeController {

    @Autowired
    private ProjectDao projectDao;

    @Autowired
    private Consts consts;

    @Autowired
    private CodeService codeService;


    @RequestMapping(value = "/count")
    public ServiceResult countProject(@RequestParam(value = "projectId", defaultValue = "0") Long projectId) throws SQLException {
        Boolean bool = codeService.isTaskCountByProjectId(projectId);
        return ServiceResult.success(bool);
    }

    @RequestMapping(value = "/generate", method = RequestMethod.POST)
    public ServiceResult generateProject(@RequestBody GenerateCodeModel generateCodeRequest, HttpServletRequest request) {
        return codeService.generateProject(generateCodeRequest, request);
    }

    @RequestMapping(value = "/files")
    public ServiceResult<List<W2uiElement>> getFiles(@RequestParam(value = "projectId", defaultValue = "") String projectId,
                                                     @RequestParam(value = "name", defaultValue = "") String name) {

        if (StringUtils.isBlank(projectId) || StringUtils.isBlank(name)) {
            return ServiceResult.fail("projectId 或 name 为空!!!");
        }
        List<W2uiElement> files = new ArrayList<>();

        File currentProjectDir = new File(new File(consts.codeConsoleilePath, projectId), CodeGenConsts.JAVA);
        if (currentProjectDir.exists()) {
            File currentFile;
            if (StringUtils.isBlank(name)) {
                currentFile = currentProjectDir;
            } else {
                currentFile = new File(currentProjectDir, name);
            }
            if (null == currentFile || null == currentFile.listFiles()) {
                return ServiceResult.fail(currentProjectDir.getPath() + ", 文件目录未创建成功，请添加目录操作权限!!!");
            }
            for (File f : currentFile.listFiles()) {
                W2uiElement element = new W2uiElement();
                if (null == name || name.isEmpty()) {
                    element.setId(String.format("%s_%d", projectId, files.size()));
                } else {
                    element.setId(String.format("%s_%s_%d", projectId, name.replace("\\", ""), files.size()));
                }
                if (null == name || name.isEmpty()) {
                    element.setData(f.getName());
                } else {
                    element.setData(name + File.separator + f.getName());
                }
                element.setText(f.getName());
                element.setChildren(f.isDirectory());
                if (element.isChildren()) {
                    element.setType("folder");
                } else {
                    element.setType("file");
                }
                files.add(element);
            }
        }
        return ServiceResult.success(files);
    }

    @RequestMapping(value = "/content")
    public ServiceResult<String> getFileContent(@RequestParam(value = "projectId", defaultValue = "") String projectId,
                                                @RequestParam(value = "name", defaultValue = "") String name) throws Exception {
        return codeService.getFileContent(projectId, name);
    }

    @RequestMapping("/download")
    public String download(@RequestParam(value = "projectId") Long projectId, HttpServletResponse response) throws Exception {
        File file = new File(new File(consts.codeConsoleilePath, String.valueOf(projectId)), CodeGenConsts.JAVA);
        Project project = projectDao.getProjectByID(projectId);
        String date = DateUtil.getCurrentTime();
        final String zipFileName = project.getName() + "-" + date + ".zip";
        return FileUtils.download(response, file, zipFileName, consts.codeConsoleilePath);
    }

    @RequestMapping(value = "/clearFiles")
    public ServiceResult clearFiles(@RequestParam(value = "projectId") Integer projectId) {
        try {
            String path = consts.codeConsoleilePath;
            File dir = new File(String.format("%s/%s", path, projectId));
            if (dir.exists()) {
                try {
                    org.apache.commons.io.FileUtils.forceDelete(dir);
                } catch (IOException e) {
                }
            }
            return ServiceResult.success();
        } catch (Throwable e) {
            return ServiceResult.fail(e.getMessage());
        }
    }
}
