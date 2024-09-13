package io.playce.roro.api.domain.inventory.service.helper;

import io.playce.roro.api.common.error.exception.ResourceNotFoundException;
import io.playce.roro.api.domain.inventory.service.ApplicationService;
import io.playce.roro.api.domain.inventory.service.DatabaseService;
import io.playce.roro.api.domain.inventory.service.MiddlewareService;
import io.playce.roro.common.code.Domain1002;
import io.playce.roro.common.dto.inventory.server.ServerDetailResponse;
import io.playce.roro.common.dto.inventory.server.ServerResponse;
import io.playce.roro.common.exception.RoRoException;
import io.playce.roro.common.property.CommonProperties;
import io.playce.roro.common.report.ExcelExporter;
import io.playce.roro.common.util.JsonUtil;
import io.playce.roro.jpa.entity.InventoryMaster;
import io.playce.roro.jpa.entity.InventoryProcess;
import io.playce.roro.jpa.entity.InventoryProcessResult;
import io.playce.roro.jpa.repository.InventoryMasterRepository;
import io.playce.roro.jpa.repository.InventoryProcessRepository;
import io.playce.roro.mybatis.domain.common.label.LabelMapper;
import io.playce.roro.mybatis.domain.inventory.manager.ManagerMapper;
import io.playce.roro.mybatis.domain.inventory.process.InventoryProcessMapper;
import io.playce.roro.mybatis.domain.inventory.server.ServerMapper;
import io.playce.roro.mybatis.domain.inventory.server.ServerSummaryMapper;
import io.playce.roro.mybatis.domain.inventory.service.ServiceMapper;
import io.playce.roro.svr.asmt.dto.result.WindowsAssessmentResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ManualProcess {

    private final MiddlewareService middlewareService;
    private final DatabaseService databaseService;
    private final ApplicationService applicationService;

    private final ServiceMapper serviceMapper;
    private final ServerMapper serverMapper;
    private final ServerSummaryMapper serverSummaryMapper;
    private final ManagerMapper managerMapper;
    private final LabelMapper labelMapper;
    private final InventoryProcessMapper inventoryProcessMapper;
    private final InventoryMasterRepository inventoryMasterRepository;
    private final InventoryProcessRepository inventoryProcessRepository;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");

    public InventoryProcessResult saveReport(Long inventoryProcessId, Object result) throws Exception {
        InventoryProcessResult inventoryProcessResult = new InventoryProcessResult();

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

                Object entity = getServer(inventoryMaster.getProjectId(), inventoryMaster.getInventoryId());

                ExcelExporter excelExporter = new ExcelExporter();
                excelExporter.export(excelFilePath, entity, JsonUtil.readTree(inventoryProcessResult.getInventoryProcessResultJson()));

                inventoryProcessResult.setInventoryProcessResultExcelPath(excelFilePath);
            } catch (Exception e) {
                RoRoException.checkInterruptedException(e);
                // Excel 생성이 실패하더라도 Assessment는 성공되어야 함.
                log.error("Unhandled exception occurred while create an excel report.", e);
            }
        }

        inventoryProcessResult.setInventoryProcessId(inventoryProcessId);
        return inventoryProcessResult;
    }

    private String getAssessmentResultPath(Long inventoryProcessId) {
        return CommonProperties.getWorkDir() + File.separator +
                "assessment" + File.separator +
                (inventoryProcessId == null ? UUID.randomUUID().toString() : inventoryProcessId);
    }

    public ServerDetailResponse getServer(Long projectId, Long serverId) {
        inventoryMasterRepository.findByProjectIdAndInventoryId(projectId, serverId)
                .orElseThrow(() -> new ResourceNotFoundException("Server ID : " + serverId + " Not Found in Project ID : " + projectId));

        ServerDetailResponse server = serverMapper.selectServerDetail(projectId, serverId);
        if (server != null) {
            setServerDetail(server);

            // resource count
            server.setMiddlewareCount(middlewareService.getMiddlewares(projectId, null, serverId).size());
            server.setApplicationCount(applicationService.getApplications(projectId, null, serverId).size());
            server.setDatabaseCount(databaseService.getDatabaseEngines(projectId, null, serverId).size());
        }

        return server;
    }

    private void setServerDetail(ServerResponse server) {
        if (server != null) {
            server.setServices(serviceMapper.getServiceSummaries(server.getServerInventoryId()));
            server.setServerSummary(serverSummaryMapper.selectServerSummary(server.getServerInventoryId()));
            server.setLabelList(labelMapper.getInventoryLabelList(server.getServerInventoryId()));
            server.setInventoryManagers(managerMapper.getInventoryManagers(server.getServerInventoryId()));

            // 가장 마지막으로 성공한 process 설정
            io.playce.roro.common.dto.inventory.process.InventoryProcess.CompleteScan completeScan = inventoryProcessMapper
                    .selectLastCompleteInventoryProcess(server.getServerInventoryId(), Domain1002.SCAN.name());
            if (completeScan != null) {
                server.setLastCompleteScan(completeScan);
            }

            // 마지막 Preq, Scan, Mig 데이터 설정
            io.playce.roro.common.dto.inventory.process.InventoryProcess.Result result = inventoryProcessMapper.selectLastInventoryProcess(server.getServerInventoryId(), Domain1002.PREQ.name());
            if (result != null) {
                server.getLastInventoryProcesses().add(result);
            }

            result = inventoryProcessMapper.selectLastInventoryProcess(server.getServerInventoryId(), Domain1002.SCAN.name());
            if (result != null) {
                server.getLastInventoryProcesses().add(result);
            }

            result = inventoryProcessMapper.selectLastInventoryProcess(server.getServerInventoryId(), Domain1002.MIG.name());
            if (result != null) {
                server.getLastInventoryProcesses().add(result);
            }
        }
    }
}
