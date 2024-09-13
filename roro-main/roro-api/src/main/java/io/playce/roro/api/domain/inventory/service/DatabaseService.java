package io.playce.roro.api.domain.inventory.service;

import io.playce.roro.api.common.error.ErrorCode;
import io.playce.roro.api.common.error.exception.ResourceNotFoundException;
import io.playce.roro.api.common.error.exception.RoRoApiException;
import io.playce.roro.api.common.util.WebUtil;
import io.playce.roro.api.domain.assessment.service.AssessmentService;
import io.playce.roro.common.code.*;
import io.playce.roro.common.dto.common.excel.ListToExcelDto;
import io.playce.roro.common.dto.inventory.database.*;
import io.playce.roro.common.dto.inventory.process.InventoryProcess;
import io.playce.roro.common.util.ExcelUtil;
import io.playce.roro.jpa.entity.*;
import io.playce.roro.jpa.repository.*;
import io.playce.roro.mybatis.domain.common.label.LabelMapper;
import io.playce.roro.mybatis.domain.inventory.database.DatabaseMapper;
import io.playce.roro.mybatis.domain.inventory.process.InventoryProcessMapper;
import io.playce.roro.mybatis.domain.inventory.service.ServiceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DatabaseService {

    private final InventoryService inventoryService;
    private final AssessmentService assessmentService;

    private final CredentialMasterRepository credentialMasterRepository;
    private final InventoryMasterRepository inventoryMasterRepository;
    private final InventoryLabelRepository inventoryLabelRepository;
    private final ServiceInventoryRepository serviceInventoryRepository;
    private final DatabaseMasterRepository databaseMasterRepository;
    private final DiscoveredInstanceMasterRepository discoveredInstanceMasterRepository;
    private final DatabaseInstanceRepository databaseInstanceRepository;

    private final DatabaseMapper databaseMapper;
    private final ServiceMapper serviceMapper;
    private final LabelMapper labelMapper;
    private final InventoryProcessMapper inventoryProcessMapper;

    public List<DatabaseEngineListResponseDto> getDatabaseEngines(Long projectId, Long serviceId, Long serverId) {

        List<DatabaseEngineListResponseDto> result = databaseMapper.selectDatabaseEngineList(projectId, serviceId, serverId);

        for (DatabaseEngineListResponseDto databaseEngineDto : result) {
            databaseEngineDto.setServices(serviceMapper.getServiceSummaries(databaseEngineDto.getDatabaseInventoryId()));
            databaseEngineDto.setLabelList(labelMapper.getInventoryLabelList(databaseEngineDto.getDatabaseInventoryId()));

            // 가장 마지막으로 성공한 process 설정
            InventoryProcess.CompleteScan completeScan = inventoryProcessMapper
                    .selectLastCompleteInventoryProcess(databaseEngineDto.getDatabaseInventoryId(), Domain1002.SCAN.name());
            if (completeScan != null) {
                databaseEngineDto.setLastCompleteScan(completeScan);
            }

            // 마지막 Scan 데이터 설정
            InventoryProcess.Result processResult = inventoryProcessMapper
                    .selectLastInventoryProcess(databaseEngineDto.getDatabaseInventoryId(), Domain1002.SCAN.name());
            if (processResult != null) {
                databaseEngineDto.setLastInventoryProcess(processResult);
            }
        }

        return result;
    }

    public List<DatabaseInstanceListResponseDto> getDatabaseInstances(Long projectId, Long databaseInventoryId) {
        return databaseMapper.selectDatabaseInstanceList(projectId, databaseInventoryId);
    }

    public DatabaseEngineResponseDto getDatabaseEngine(Long projectId, Long databaseInventoryId) {
        inventoryMasterRepository.findByProjectIdAndInventoryId(projectId, databaseInventoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Database ID : " + databaseInventoryId + " Not Found in Project ID : " + projectId));

        DatabaseEngineResponseDto databaseEngineResponseDto = databaseMapper.selectDatabaseEngine(projectId, databaseInventoryId);

        databaseEngineResponseDto.setServices(serviceMapper.getServiceSummaries(databaseEngineResponseDto.getDatabaseInventoryId()));
        databaseEngineResponseDto.setLabelList(labelMapper.getInventoryLabelList(databaseEngineResponseDto.getDatabaseInventoryId()));

        // 가장 마지막으로 성공한 process 설정
        InventoryProcess.CompleteScan completeScan = inventoryProcessMapper
                .selectLastCompleteInventoryProcess(databaseInventoryId, Domain1002.SCAN.name());
        if (completeScan != null) {
            databaseEngineResponseDto.setLastCompleteScan(completeScan);
        }

        // 마지막 Scan 데이터 설정
        InventoryProcess.Result processResult = inventoryProcessMapper
                .selectLastInventoryProcess(databaseEngineResponseDto.getDatabaseInventoryId(), Domain1002.SCAN.name());
        if (processResult != null) {
            databaseEngineResponseDto.setLastInventoryProcess(processResult);
        }

        return databaseEngineResponseDto;
    }

    /**
     * https://cloud-osci.atlassian.net/browse/PCR-5593
     * 이중 서브밋 방지를 위한 방어코드로 @Transactional 애노테이션에는 synchronized가 동작하기 않기 때문에
     * 별도의 synchronized 메소드 내에서 @Transactional 메소드를 호출한다.
     */
    public synchronized Map<String, Object> createDatabase(Long projectId, DatabaseRequest databaseRequest) {
        return createDatabaseInternal(projectId, databaseRequest);
    }


    @Transactional
    public Map<String, Object> createDatabaseInternal(Long projectId, DatabaseRequest databaseRequest) {

        // 1. Credential 등록.
        CredentialMaster credentialMaster = new CredentialMaster();
        credentialMaster.setProjectId(projectId);
        credentialMaster.setCredentialTypeCode(Domain1009.DBMS.name());
        credentialMaster.setUserName(databaseRequest.getUserName());
        credentialMaster.setUserPassword(databaseRequest.getPassword());
        credentialMaster.setDeleteYn(Domain101.N.name());
        credentialMaster.setRegistUserId(WebUtil.getUserId());
        credentialMaster.setRegistDatetime(new Date());
        credentialMaster.setModifyUserId(WebUtil.getUserId());
        credentialMaster.setModifyDatetime(new Date());

        credentialMaster = credentialMasterRepository.save(credentialMaster);

        log.debug(credentialMaster.toString());

        // 2. Inventory Master 등록
        InventoryMaster inventoryMaster = new InventoryMaster();
        inventoryMaster.setProjectId(projectId);
        inventoryMaster.setServerInventoryId(databaseRequest.getServerInventoryId());
        inventoryMaster.setCredentialId(credentialMaster.getCredentialId());
        inventoryMaster.setInventoryTypeCode(Domain1001.DBMS.name());
        inventoryMaster.setInventoryDetailTypeCode(databaseRequest.getInventoryDetailTypeCode());
        inventoryMaster.setInventoryName(databaseRequest.getDatabaseInventoryName());
        inventoryMaster.setInventoryAnalysisYn(Domain101.Y.name());
        inventoryMaster.setCustomerInventoryCode(databaseRequest.getCustomerInventoryCode());
        inventoryMaster.setCustomerInventoryName(databaseRequest.getCustomerInventoryName());
        inventoryMaster.setInventoryIpTypeCode(Domain1006.INV.name());
        inventoryMaster.setDeleteYn(Domain101.N.name());
        inventoryMaster.setAutomaticRegistYn(Domain101.N.name());
        inventoryMaster.setDescription(databaseRequest.getDescription());
        inventoryMaster.setRegistUserId(WebUtil.getUserId());
        inventoryMaster.setRegistDatetime(new Date());
        inventoryMaster.setModifyUserId(WebUtil.getUserId());
        inventoryMaster.setModifyDatetime(new Date());

        inventoryMaster = inventoryService.saveInventoryMaster(inventoryMaster);

        log.debug(inventoryMaster.toString());

        // 3. Service Inventory 등록
        for (Long serviceId : databaseRequest.getServiceIds()) {
            ServiceInventory serviceInventory = new ServiceInventory();
            serviceInventory.setInventoryId(inventoryMaster.getInventoryId());
            serviceInventory.setServiceId(serviceId);

            serviceInventoryRepository.save(serviceInventory);
        }

        // 4. Database Master 등록
        DatabaseMaster databaseMaster = new DatabaseMaster();
        databaseMaster.setDatabaseInventoryId(inventoryMaster.getInventoryId());
        databaseMaster.setVendor(databaseRequest.getVendor());
        databaseMaster.setEngineVersion(databaseRequest.getEngineVersion());
        databaseMaster.setConnectionPort(databaseRequest.getConnectionPort());
        databaseMaster.setDatabaseServiceName(databaseRequest.getDatabaseServiceName());
        databaseMaster.setJdbcUrl(databaseRequest.getJdbcUrl());
        databaseMaster.setAllScanYn(databaseRequest.getAllScanYn());
        databaseMaster.setDatabaseAccessControlSystemSolutionName(databaseRequest.getDatabaseAccessControlSystemSolutionName());

        // 5. Discovered Database를 등록 할 때.
        // Discovered Database에 이미 있는지 체크를 한다.(Discovered Database에서 Add Inventory를 하지 않는 경우.)
        Long discoveredDatabaseInstanceId = databaseMapper.selectDiscoveredDatabaseInstance(projectId, databaseRequest);

        if (discoveredDatabaseInstanceId != null) {
            databaseRequest.setDiscoveredInstanceId(discoveredDatabaseInstanceId);
        }

        if (databaseRequest.getDiscoveredInstanceId() != null) {
            DiscoveredInstanceMaster discoveredInstanceMaster = discoveredInstanceMasterRepository.findById(databaseRequest.getDiscoveredInstanceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Discovered Instance ID : " + databaseRequest.getDiscoveredInstanceId() + " Not Found."));
            databaseMaster.setDiscoveredDatabaseYn(Domain101.Y.name());

            discoveredInstanceMaster.setPossessionInventoryId(inventoryMaster.getInventoryId());
            inventoryMaster.setInventoryDiscoveredDatetime(discoveredInstanceMaster.getRegistDatetime());
        } else {
            databaseMaster.setDiscoveredDatabaseYn(Domain101.N.name());
        }

        databaseMaster = databaseMasterRepository.save(databaseMaster);

        log.debug(databaseMaster.toString());

        // 5. Database Instance 테이블의 등록 여부 확인 (jdbc url 으로 체크)
        // 있는 경우 Discovered Instance Master 'possession_inventory_id' 업데이트 and discoveredDatabaseYn을 'Y'로 로 설정
        // 없으면 discoveredDatabaseYn을 'N'로 설정
//        DatabaseInstance databaseInstance = databaseInstanceRepository.findByJdbcUrl(databaseMaster.getJdbcUrl());
//        if (databaseInstance != null) {
//            DiscoveredInstanceMaster discoveredInstanceMaster = discoveredInstanceMasterRepository.findById(databaseInstance.getDatabaseInstanceId())
//                    .orElseThrow(() -> new ResourceNotFoundException("Discovered Instance ID : " + databaseInstance.getDatabaseInstanceId() + " Not Found."));
//
//            databaseMaster.setDiscoveredDatabaseYn(Domain101.Y.name());
//            discoveredInstanceMaster.setPossessionInventoryId(inventoryMaster.getInventoryId());
//        } else {
//            databaseMaster.setDiscoveredDatabaseYn(Domain101.N.name());
//        }

        // 6. Label 등록
        for (Long labelId : databaseRequest.getLabelIds()) {
            InventoryLabel inventoryLabel = new InventoryLabel();
            inventoryLabel.setInventoryId(inventoryMaster.getInventoryId());
            inventoryLabel.setLabelId(labelId);
            inventoryLabelRepository.save(inventoryLabel);
        }

        // 7. Inventory Process 등록.
        assessmentService.createAssessment(projectId, databaseMaster.getDatabaseInventoryId());

        Map<String, Object> databaseMap = new HashMap<>();
        databaseMap.put("databaseInventoryId", inventoryMaster.getInventoryId());
        databaseMap.put("databaseInventoryName", inventoryMaster.getInventoryName());

        return databaseMap;
    }

    @Transactional
    public Map<String, Object> modifyDatabase(Long projectId, Long databaseInventoryId, DatabaseRequest databaseRequest) {
        InventoryMaster inventoryMaster = inventoryMasterRepository.findByProjectIdAndInventoryId(projectId, databaseInventoryId)
                .orElseThrow(() -> new RoRoApiException(ErrorCode.INVENTORY_DATABASE_NOT_FOUND));


        // 서버를 수정할 때 성공한 Assessment 결과가 있는 경우 수정불가하다.
        if (!Objects.equals(inventoryMaster.getServerInventoryId(), databaseRequest.getServerInventoryId())) {
            if (inventoryProcessMapper.selectSuccessCompleteCount(inventoryMaster.getInventoryId()) != 0) {
                throw new RoRoApiException(ErrorCode.INVENTORY_DATABASE_NOT_MODIFY);
            }
        }

        // 1. Inventory Master 수정.
        inventoryMaster.setServerInventoryId(databaseRequest.getServerInventoryId());
        inventoryMaster.setInventoryDetailTypeCode(databaseRequest.getInventoryDetailTypeCode());
        inventoryMaster.setInventoryName(databaseRequest.getDatabaseInventoryName());
        inventoryMaster.setCustomerInventoryCode(databaseRequest.getCustomerInventoryCode());
        inventoryMaster.setCustomerInventoryName(databaseRequest.getCustomerInventoryName());
        inventoryMaster.setDescription(databaseRequest.getDescription());
        inventoryMaster.setModifyUserId(WebUtil.getUserId());
        inventoryMaster.setModifyDatetime(new Date());

        inventoryService.saveInventoryMaster(inventoryMaster);

        // 2. Credential 수정.
        CredentialMaster credentialMaster = credentialMasterRepository.findByProjectIdAndCredentialId(projectId, inventoryMaster.getCredentialId());

        credentialMaster.setUserName(databaseRequest.getUserName());

        if (StringUtils.isNotEmpty(databaseRequest.getPassword())) {
            credentialMaster.setUserPassword(databaseRequest.getPassword());
        }

        credentialMasterRepository.save(credentialMaster);

        // 3. Service Inventory 재 등록.
        serviceInventoryRepository.deleteAllByInventoryId(databaseInventoryId);
        serviceInventoryRepository.flush();
        for (Long serviceId : databaseRequest.getServiceIds()) {
            ServiceInventory serviceInventory = new ServiceInventory();
            serviceInventory.setInventoryId(databaseInventoryId);
            serviceInventory.setServiceId(serviceId);

            serviceInventoryRepository.save(serviceInventory);
        }

        // 4. Database Master 수정.
        DatabaseMaster databaseMaster = databaseMasterRepository.findById(databaseInventoryId)
                .orElseThrow(() -> new RoRoApiException(ErrorCode.INVENTORY_DATABASE_NOT_FOUND));

        databaseMaster.setVendor(databaseRequest.getVendor());
        databaseMaster.setEngineVersion(databaseRequest.getEngineVersion());
        databaseMaster.setConnectionPort(databaseRequest.getConnectionPort());
        databaseMaster.setDatabaseServiceName(databaseRequest.getDatabaseServiceName());
        databaseMaster.setJdbcUrl(databaseRequest.getJdbcUrl());
        databaseMaster.setAllScanYn(databaseRequest.getAllScanYn());
        databaseMaster.setDatabaseAccessControlSystemSolutionName(databaseRequest.getDatabaseAccessControlSystemSolutionName());
        databaseMasterRepository.save(databaseMaster);

        // 5. Label 재 등록.
        inventoryLabelRepository.deleteAllByInventoryId(databaseInventoryId);
        inventoryLabelRepository.flush();
        for (Long labelId : databaseRequest.getLabelIds()) {
            InventoryLabel inventoryLabel = new InventoryLabel();
            inventoryLabel.setInventoryId(databaseInventoryId);
            inventoryLabel.setLabelId(labelId);

            inventoryLabelRepository.save(inventoryLabel);
        }

        Map<String, Object> databaseMap = new HashMap<>();
        databaseMap.put("databaseInventoryId", inventoryMaster.getInventoryId());
        databaseMap.put("databaseInventoryName", inventoryMaster.getInventoryName());

        return databaseMap;
    }

    @Transactional
    public void removeDatabase(Long projectId, Long databaseInventoryId) {
        InventoryMaster inventoryMaster = inventoryMasterRepository.findByProjectIdAndInventoryId(projectId, databaseInventoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Database ID : " + databaseInventoryId + " Not Found."));

        inventoryMaster.setDeleteYn(Domain101.Y.name());
        inventoryMaster.setModifyUserId(WebUtil.getUserId());
        inventoryMaster.setModifyDatetime(new Date());

        inventoryMasterRepository.save(inventoryMaster);
    }

    public DatabaseInstanceResponseDto getDatabaseInstance(Long projectId, Long databaseInventoryId, Long databaseInstanceId) {
        DatabaseInstanceResponseDto databaseInstanceResponseDto =
                databaseMapper.selectDatabaseInstance(projectId, databaseInventoryId, databaseInstanceId);

        databaseInstanceResponseDto.setServices(serviceMapper.getServiceSummaries(databaseInventoryId));

        return databaseInstanceResponseDto;
    }

    public List<DatabaseInstanceMiddlewareResponseDto> getDatabaseInstanceMiddleware(Long projectId, Long databaseInventoryId, Long databaseInstanceId) {
        Map<String, String> databaseMap = databaseMapper.selectDatabaseServerAndName(projectId, databaseInventoryId, databaseInstanceId);

        String serverIp = databaseMap.get("server_ip");
        String databaseServiceName = databaseMap.get("database_service_name");

        return databaseMapper.selectDatabaseInstanceMiddlewares(serverIp, databaseServiceName);
    }

    public List<DatabaseInstanceApplicationResponseDto> getDatabaseInstanceApplications(Long projectId, Long databaseInventoryId, Long databaseInstanceId) {
        Map<String, String> databaseMap = databaseMapper.selectDatabaseServerAndName(projectId, databaseInventoryId, databaseInstanceId);

        String serverIp = databaseMap.get("server_ip");
        String databaseServiceName = databaseMap.get("database_service_name");

        return databaseMapper.selectDatabaseInstanceApplications(serverIp, databaseServiceName);
    }

    @Transactional
    public void removeDatabaseInstance(Long projectId, Long databaseInstanceId) {
        DiscoveredInstanceMaster discoveredInstanceMaster =
                discoveredInstanceMasterRepository.findByProjectIdAndDiscoveredInstanceId(projectId, databaseInstanceId);

        discoveredInstanceMaster.setDeleteYn(Domain101.Y.name());

        discoveredInstanceMasterRepository.save(discoveredInstanceMaster);
    }

    public ByteArrayInputStream getDatabaseListExcel(Long projectId, Long serviceId, Long serverId) {
        List<DatabaseExcelResponse> databaseExcels = databaseMapper.selectDatabaseExcel(projectId, serviceId, serverId);

        // 헤더 설정
        //-> 데이터베이스 헤더
        ListToExcelDto listToExcelDto = new ListToExcelDto();
        listToExcelDto.getHeaderItemList().add("Inventory Code");
        listToExcelDto.getHeaderItemList().add("Inventory Name");
        listToExcelDto.getHeaderItemList().add("Service ID");
        listToExcelDto.getHeaderItemList().add("Service Name");
        listToExcelDto.getHeaderItemList().add("Server ID");
        listToExcelDto.getHeaderItemList().add("Server Name");
        listToExcelDto.getHeaderItemList().add("Database ID");
        listToExcelDto.getHeaderItemList().add("Database Name");
        listToExcelDto.getHeaderItemList().add("Vendor");
        listToExcelDto.getHeaderItemList().add("Engine Name");
        listToExcelDto.getHeaderItemList().add("Engine Version");
        listToExcelDto.getHeaderItemList().add("Connection Port");
        listToExcelDto.getHeaderItemList().add("Database Service Name");
        listToExcelDto.getHeaderItemList().add("JDBC Connection URL");
        listToExcelDto.getHeaderItemList().add("Scan All Database Instances");
        listToExcelDto.getHeaderItemList().add("Username");
        listToExcelDto.getHeaderItemList().add("Access Control");
        listToExcelDto.getHeaderItemList().add("No. of Instances");
        listToExcelDto.getHeaderItemList().add("Labels");
        listToExcelDto.getHeaderItemList().add("Description");

        //-> 인스턴스 헤더
        listToExcelDto.getHeaderItemList().add("Database Instance ID");
        listToExcelDto.getHeaderItemList().add("Database Instance Name");
        listToExcelDto.getHeaderItemList().add("Table Count");
        listToExcelDto.getHeaderItemList().add("View Count");
        listToExcelDto.getHeaderItemList().add("Function Count");
        listToExcelDto.getHeaderItemList().add("Procedure Count");

        ListToExcelDto.RowItem rowItem;
        // 인스턴스, 서비스 매핑에 따라 신규 row를 생성한다.
        for (DatabaseExcelResponse data : databaseExcels) {

            rowItem = new ListToExcelDto.RowItem();
            // 데이터베이스 데이터 설정
            rowItem.getCellItemList().add(data.getCustomerInventoryCode());
            rowItem.getCellItemList().add(data.getCustomerInventoryName());
            rowItem.getCellItemList().add(data.getServiceId());
            rowItem.getCellItemList().add(data.getServiceName());
            rowItem.getCellItemList().add(data.getServerInventoryId());
            rowItem.getCellItemList().add(data.getServerInventoryName());
            rowItem.getCellItemList().add(data.getDatabaseInventoryId());
            rowItem.getCellItemList().add(data.getDatabaseInventoryName());
            rowItem.getCellItemList().add(data.getVendor());
            rowItem.getCellItemList().add(data.getInventoryDetailTypeCode());
            rowItem.getCellItemList().add(data.getEngineVersion());
            rowItem.getCellItemList().add(data.getConnectionPort());
            rowItem.getCellItemList().add(data.getDatabaseServiceName());
            rowItem.getCellItemList().add(data.getJdbcUrl());
            rowItem.getCellItemList().add(data.getAllScanYn());
            rowItem.getCellItemList().add(data.getUserName());
            rowItem.getCellItemList().add(data.getAccessControl());
            rowItem.getCellItemList().add(data.getInstanceCount());
            rowItem.getCellItemList().add(data.getLabels());
            rowItem.getCellItemList().add(data.getDescription());

            // 인스턴스 데이터 설정
            rowItem.getCellItemList().add(data.getDatabaseInstanceId());
            rowItem.getCellItemList().add(data.getDatabaseInstanceName());
            rowItem.getCellItemList().add(data.getTableCount());
            rowItem.getCellItemList().add(data.getViewCount());
            rowItem.getCellItemList().add(data.getFunctionCount());
            rowItem.getCellItemList().add(data.getProcedureCount());

            listToExcelDto.getRowItemList().add(rowItem);
        }

        ByteArrayOutputStream out;
        try {
            out = ExcelUtil.listToExcel("Databases", listToExcelDto);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            log.debug("Unhandled exception occurred while create database excel list.", e);
            throw new RoRoApiException(ErrorCode.EXCEL_CREATE_FAILED, e.getMessage());
        }
    }

}
