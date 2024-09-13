/*
 * Copyright 2022 The playce-roro-v3 Project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Revision History
 * Author			Date				Description
 * ---------------	----------------	------------
 * Dong-Heon Han    Jan 06, 2022		First Draft.
 */

package io.playce.roro.scheduler.component;

import io.playce.roro.api.common.error.exception.ResourceNotFoundException;
import io.playce.roro.api.domain.inventory.service.*;
import io.playce.roro.app.asmt.result.ApplicationAssessmentResult;
import io.playce.roro.common.code.Domain1001;
import io.playce.roro.common.code.Domain1003;
import io.playce.roro.common.dto.inventory.process.InventoryProcessQueueItem;
import io.playce.roro.common.dto.thirdparty.ThirdPartyDiscoveryResult;
import io.playce.roro.common.exception.NotsupportedException;
import io.playce.roro.common.exception.RoRoException;
import io.playce.roro.common.property.CommonProperties;
import io.playce.roro.common.report.ExcelExporter;
import io.playce.roro.common.util.JsonUtil;
import io.playce.roro.jpa.entity.InventoryMaster;
import io.playce.roro.jpa.entity.InventoryProcess;
import io.playce.roro.jpa.entity.InventoryProcessResult;
import io.playce.roro.jpa.repository.InventoryMasterRepository;
import io.playce.roro.jpa.repository.InventoryProcessRepository;
import io.playce.roro.mw.asmt.dto.MiddlewareAssessmentResult;
import io.playce.roro.svr.asmt.dto.ServerAssessmentResult;
import io.playce.roro.svr.asmt.dto.result.WindowsAssessmentResult;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static io.playce.roro.common.code.Domain1013.*;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Dong-Heon Han
 * @version 3.0
 */
@Slf4j
public abstract class AbstractAssessmentProcess {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");

    private ApplicationContext applicationContext;
    private ServerService serverService;
    private MiddlewareService middlewareService;
    private ApplicationService applicationService;
    private DatabaseService databaseService;
    private DiscoveredThirdPartyService discoveredThirdPartyService;
    private InventoryProcessRepository inventoryProcessRepository;
    private InventoryMasterRepository inventoryMasterRepository;

    public abstract Domain1003 assessment(InventoryProcessQueueItem item, Domain1003 resultState) throws InterruptedException;

    public String saveResult(InventoryProcessQueueItem item, InventoryProcessService inventoryProcessService, Long inventoryProcessId, Object result, String errorMessage, boolean saveReport) throws InterruptedException {
        String message = null;
        InventoryProcessResult inventoryProcessResult = null;

        if (saveReport) {
            try {
                inventoryProcessResult = saveReport(inventoryProcessId, result);
            } catch (Exception e) {
                RoRoException.checkInterruptedException(e);
                log.error("Unhandled exception occurred while create a scan report.", e);
                message = "Scan report create failed.";
            }
        }

        try {
            if (discoveredThirdPartyService == null) {
                if (applicationContext == null) {
                    applicationContext = CommonProperties.getApplicationContext();
                }

                discoveredThirdPartyService = applicationContext.getBean(DiscoveredThirdPartyService.class);
            }

            List<ThirdPartyDiscoveryResult> thirdPartySolutions = null;

            if (result instanceof ServerAssessmentResult) {
                thirdPartySolutions = ((ServerAssessmentResult) result).getThirdPartySolutions();
            } else if (result instanceof MiddlewareAssessmentResult) {
                thirdPartySolutions = ((MiddlewareAssessmentResult) result).getThirdPartySolutions();
            } else if (result instanceof ApplicationAssessmentResult) {
                thirdPartySolutions = ((ApplicationAssessmentResult) result).getThirdPartySolutions();
            }

            if (thirdPartySolutions != null && thirdPartySolutions.size() > 0) {
                discoveredThirdPartyService.addDiscoveredThirdParties(inventoryProcessId, thirdPartySolutions);
            }
        } catch (Exception e) {
            log.error("Unhandled exception occurred while add discovered 3rd party solutions.", e);
            message = "3rd party solutions discovery failed.";
        }

        if (StringUtils.isNotEmpty(message)) {
            if (StringUtils.isEmpty(errorMessage)) {
                errorMessage = message;
            } else {
                errorMessage += "\n" + message;
            }
        }

        inventoryProcessService.addInventoryProcessResult(item, inventoryProcessId, result, errorMessage, inventoryProcessResult);

        return message;
    }

    protected String makeComponentName(String... keys) {
        return getString(keys, "Assessment");
    }

    @NotNull
    private String getString(String[] keys, String type) {
        StringBuilder sb = new StringBuilder();
        for (String key : keys) {
            if (EAR.name().equals(key) || WAR.name().equals(key) || JAR.name().equals(key) || ETC.name().equals(key) || ETC.enname().equals(key)) {
                sb.append("Java");
            } else {
                sb.append(key);
            }
        }
        sb.append(type);
        return sb.toString();
    }

    protected String makePostProcessorName(String... keys) {
        return getString(keys, "PostProcessor");
    }

    private InventoryProcessResult saveReport(Long inventoryProcessId, Object result) throws Exception {
        InventoryProcessResult inventoryProcessResult = new InventoryProcessResult();

        checkBeans();

        InventoryProcess inventoryProcess = inventoryProcessRepository.findById(inventoryProcessId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory Process(" + inventoryProcessId + ") Not Found."));

        InventoryMaster inventoryMaster = inventoryMasterRepository.findById(inventoryProcess.getInventoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Inventory (" + inventoryProcess.getInventoryId() + ") Not Found."));

        // Scan 결과 json 및 excel report를 생성하고 저장한다.
        String inventoryTypeCode = inventoryMaster.getInventoryTypeCode();

        String workDirPath = getAssessmentResultPath(inventoryProcessId);

        if (result instanceof String) {
            inventoryProcessResult.setInventoryProcessResultJson((String) result);
        } else {
            if (result instanceof WindowsAssessmentResult) {
                inventoryProcessResult.setInventoryProcessResultJson(JsonUtil.objToJson(((WindowsAssessmentResult) result).getWindowsResult(), true));
            } else {
                inventoryProcessResult.setInventoryProcessResultJson(JsonUtil.objToJson(result, true));
            }
        }

        String name = inventoryMaster.getInventoryName();
        if (name != null) {
            name = name.replaceAll(" ", "-");
        }

        // Write Json Result File
        String sb = workDirPath + File.separator +
                "roro_" +
                inventoryTypeCode.toLowerCase() + "_" +
                "assessment_" +
                name + "_" +
                DATE_FORMAT.format(new Date()) +
                ".json";

        File resultFile = new File(FilenameUtils.separatorsToSystem(sb));
        FileUtils.forceMkdirParent(resultFile);
        log.debug("Assessment result will be saved to [{}]", resultFile.getAbsoluteFile());

        FileUtils.writeStringToFile(resultFile, inventoryProcessResult.getInventoryProcessResultJson(), "UTF-8");

        inventoryProcessResult.setInventoryProcessResultJsonPath(resultFile.getAbsolutePath());

        // Write Excel Report File
        if (inventoryProcessResult.getInventoryProcessResultJson() != null) {
            try {
                String excelFilePath = inventoryProcessResult.getInventoryProcessResultJsonPath().replaceAll("json", "xlsx");

                Object entity = null;
                // get inventory information that include services and server
                if (inventoryTypeCode.equals(Domain1001.SVR.name())) {
                    entity = serverService.getServer(inventoryMaster.getProjectId(), inventoryMaster.getInventoryId());
                } else if (inventoryTypeCode.equals(Domain1001.MW.name())) {
                    entity = middlewareService.getMiddlewareDetail(inventoryMaster.getProjectId(), inventoryMaster.getInventoryId());
                } else if (inventoryTypeCode.equals(Domain1001.APP.name())) {
                    entity = applicationService.getApplication(inventoryMaster.getProjectId(), inventoryMaster.getInventoryId());
                } else if (inventoryTypeCode.equals(Domain1001.DBMS.name())) {
                    entity = databaseService.getDatabaseEngine(inventoryMaster.getProjectId(), inventoryMaster.getInventoryId());
                }

                ExcelExporter excelExporter = new ExcelExporter();
                excelExporter.export(excelFilePath, entity, JsonUtil.readTree(inventoryProcessResult.getInventoryProcessResultJson()));

                inventoryProcessResult.setInventoryProcessResultExcelPath(excelFilePath);
            } catch (Exception e) {
                RoRoException.checkInterruptedException(e);
                // Excel 생성이 실패하더라도 Assessment는 성공되어야 함.
                log.error("Unhandled exception occurred while create an excel report.", e);
            }
        }

        return inventoryProcessResult;
    }

    @NotNull
    public String getAssessmentResultPath(Long inventoryProcessId) {
        return CommonProperties.getWorkDir() + File.separator +
                "assessment" + File.separator +
                (inventoryProcessId == null ? UUID.randomUUID().toString() : inventoryProcessId);
    }

    protected ScanResult getScanResult(Throwable e) {
        ScanResult result = new ScanResult();

        Domain1003 resultState = Domain1003.FAIL;
        String resultString = e.getMessage();

        if (e instanceof NotsupportedException) {
            resultState = Domain1003.NS;
            // } else if (e instanceof InsufficientException) {
            //     resultState = Domain1003.FAIL;
            // } else if (e instanceof UnauthorizedException) {
            //     resultState = Domain1003.FAIL;
            // } else if (e instanceof ConnectionErrorException) {
            //     resultState = Domain1003.FAIL;
        } else {
            e = getCausedException(e);
            resultString = getMessage(e.getMessage());
            // if (StringUtils.isNotEmpty(e.getMessage())) {
            //     if (e.getMessage().startsWith("java.net") || e.getClass().getCanonicalName().startsWith("java.net") ||
            //             e.getClass().getCanonicalName().startsWith("com.microsoft.sqlserver.jdbc.SQLServerException")) {
            //         resultState = Domain1003.FAIL;
            //     }
            //
            //     resultString = getMessage(e.getMessage());
            // }
        }

        result.setResultState(resultState);
        result.setResultString(resultString);

        return result;
    }

    protected Throwable getCausedException(Throwable e) {
        if (e.getCause() != null && e.getCause() instanceof Exception) {
            return getCausedException(e.getCause());
        }

        return e;
    }

    private String getMessage(String message) {
        if (StringUtils.isNotEmpty(message) && message.startsWith("java.net")) {
            int idx = message.indexOf(":");

            if (idx > 0) {
                message = message.substring(idx + 2);
            }
        }

        return message;
    }

    private void checkBeans() {
        if (applicationContext == null) {
            applicationContext = CommonProperties.getApplicationContext();
        }

        if (serverService == null) {
            serverService = applicationContext.getBean(ServerService.class);
        }

        if (middlewareService == null) {
            middlewareService = applicationContext.getBean(MiddlewareService.class);
        }

        if (applicationService == null) {
            applicationService = applicationContext.getBean(ApplicationService.class);
        }

        if (databaseService == null) {
            databaseService = applicationContext.getBean(DatabaseService.class);
        }

        if (discoveredThirdPartyService == null) {
            discoveredThirdPartyService = applicationContext.getBean(DiscoveredThirdPartyService.class);
        }

        if (inventoryProcessRepository == null) {
            inventoryProcessRepository = applicationContext.getBean(InventoryProcessRepository.class);
        }

        if (inventoryMasterRepository == null) {
            inventoryMasterRepository = applicationContext.getBean(InventoryMasterRepository.class);
        }
    }

    @Getter
    @Setter
    public static class ScanResult {
        Domain1003 resultState;
        String resultString;
    }
}