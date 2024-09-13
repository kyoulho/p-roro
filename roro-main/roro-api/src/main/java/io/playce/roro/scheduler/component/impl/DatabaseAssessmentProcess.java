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

package io.playce.roro.scheduler.component.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.playce.roro.api.common.util.WebUtil;
import io.playce.roro.api.domain.insights.service.InsightsService;
import io.playce.roro.api.domain.inventory.service.InventoryProcessService;
import io.playce.roro.common.cancel.InventoryProcessCancelInfo;
import io.playce.roro.common.code.*;
import io.playce.roro.common.dto.assessment.DatabaseDto;
import io.playce.roro.common.dto.inventory.process.InventoryProcessQueueItem;
import io.playce.roro.common.exception.NotsupportedException;
import io.playce.roro.common.util.ThreadLocalUtils;
import io.playce.roro.db.asmt.DBAssessment;
import io.playce.roro.db.asmt.mariadb.dto.MariaDbDto;
import io.playce.roro.db.asmt.mssql.dto.MsSqlDto;
import io.playce.roro.db.asmt.mysql.dto.MySqlDto;
import io.playce.roro.db.asmt.oracle.dto.OracleDto;
import io.playce.roro.db.asmt.postgresql.dto.PostgreSqlDto;
import io.playce.roro.db.asmt.sybase.dto.SybaseDto;
import io.playce.roro.db.asmt.tibero.dto.TiberoDto;
import io.playce.roro.jpa.entity.*;
import io.playce.roro.jpa.repository.*;
import io.playce.roro.mybatis.domain.inventory.database.DatabaseMapper;
import io.playce.roro.scheduler.component.AbstractAssessmentProcess;
import io.playce.roro.scheduler.service.impl.AssessmentSchedulerManager;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static io.playce.roro.common.util.ThreadLocalUtils.DB_SCAN_ERROR;
import static io.playce.roro.db.asmt.constant.DBConstants.*;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Dong-Heon Han
 * @version 3.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseAssessmentProcess extends AbstractAssessmentProcess {

    private final ObjectMapper objectMapper;

    private final Map<String, DBAssessment> dbAssessmentMap;
    private final InventoryProcessService inventoryProcessService;

    private final DatabaseMasterRepository databaseMasterRepository;
    private final DatabaseSummaryRepository databaseSummaryRepository;
    private final DiscoveredInstanceMasterRepository discoveredInstanceMasterRepository;
    private final DatabaseInstanceRepository databaseInstanceRepository;
    private final DiscoveredInstanceInterfaceRepository discoveredInstanceInterfaceRepository;
    private final DatabaseMapper databaseMapper;
    private final InsightsService insightsService;

    @Override
    public Domain1003 assessment(InventoryProcessQueueItem item, Domain1003 resultState) throws InterruptedException {
        Long inventoryProcessId = item.getInventoryProcessId();
        log.debug("Step 4 ~ item: {}", item);

        Object result = null;
        String resultString = null;
        boolean saveReport = false;

        try {
            String componentName = makeComponentName(item.getInventoryDetailTypeCode());
            DBAssessment assessment = dbAssessmentMap.get(componentName);
            if (assessment == null) {
                // throw new RoRoException("The processing component does not exist.");
                throw new NotsupportedException("Scan cannot be performed. It is not supported Database.");
            }

            DatabaseDto databaseDto = databaseMapper.selectDatabaseDtoInfo(item.getInventoryProcessId());
            result = assessment.assessment(databaseDto);

            // Scan이 정상적으로 완료된 경우 상태에 관계없이 Report 생성 대상이 된다.
            saveReport = true;

            // Unsupported Middleware version 체크
            if (!checkUnsupportedDatabase(result)) {
                // 지원되지 않는 버전의 Middleware 이지만 정상 수행 가능성이 있기 때문에 Exception을 throw 하지 않고 데이터 누락이 있을 수 있다는 메시지만 추가한다.
                // resultState = Domain1003.UNS;
                resultString = "Not tested database version, some information may be missing.";
            }

            synchronized (AssessmentSchedulerManager.lockDB) {
                if (!InventoryProcessCancelInfo.hasCancelRequest(item.getInventoryProcessId())) {
                    try {
                        runPostProcessing(inventoryProcessId, databaseDto, result);
                    } catch (Exception e) {
                        log.error("Unhandled exception occurred while execute database scan's post processing.", e);

                        // 후 처리 과정에서 에러가 발생한 경우도 Partially Completed로 처리한다.
                        resultState = Domain1003.PC;

                        if (StringUtils.isEmpty(resultString)) {
                            resultString = "Post processing failed. [Reason] " + e.getMessage();
                        } else {
                            resultString += "\nPost processing failed. [Reason] " + e.getMessage();
                        }
                    }
                }
            }

            if (ThreadLocalUtils.get(DB_SCAN_ERROR) == null) {
                resultState = Domain1003.CMPL;
            } else {
                resultState = Domain1003.PC;
                resultString = (String) ThreadLocalUtils.get(DB_SCAN_ERROR);
            }
        } catch (Throwable e) {
            ScanResult scanResult = getScanResult(e);

            if (scanResult != null) {
                resultState = scanResult.getResultState();
                resultString = scanResult.getResultString();
            }

            log.error("item {} - {}", item, resultString, e);
        } finally {
            ThreadLocalUtils.clearSharedObject();

            synchronized (AssessmentSchedulerManager.lockDB) {
                if (!InventoryProcessCancelInfo.hasCancelRequest(item.getInventoryProcessId())) {
                    String message = saveResult(item, inventoryProcessService, inventoryProcessId, result, resultString, saveReport);

                    if (StringUtils.isNotEmpty(message)) {
                        if (resultState.equals(Domain1003.CMPL)) {
                            resultState = Domain1003.PC;
                        }
                    }
                }
            }
        }
        return resultState;
    }

    @SuppressWarnings("unchecked")
    @Transactional
    public void runPostProcessing(Long inventoryProcessId, DatabaseDto databaseDto, Object result) {
        log.debug("DatabaseAssessment Type : {}", databaseDto.getDatabaseType());

        Map<String, Object> resultMap = objectMapper.convertValue(result, new TypeReference<>() {
        });

        // 0. Database Server의 정보를 가져온다.
        Map<String, Object> databaseServerMap = databaseMapper.selectDatabaseServerInfo(databaseDto);

        // 1. DatabaseSummary 저장.
        DatabaseSummary databaseSummary = getDatabaseSummary(databaseDto.getDatabaseId(), resultMap);
        databaseSummaryRepository.save(databaseSummary);

        // Update DatabaseMaster engineVersion
        DatabaseMaster databaseMaster = databaseMasterRepository.findById(databaseDto.getDatabaseId()).get();
        databaseMaster.setEngineVersion(databaseSummary.getVersion());
        databaseMaster.setVendor(getDatabaseVendor(databaseDto.getDatabaseType()));
        databaseMasterRepository.save(databaseMaster);

        // [PCR-6016] Insights - Product Lifecycle 처리를 위한 OS 정보 저장
        insightsService.createInventoryLifecycleVersionLink(databaseDto.getDatabaseId(), Domain1001.DBMS, databaseDto.getDatabaseType(), databaseSummary.getVersion(), null, null);

        List<Map<String, Object>> databases = (List<Map<String, Object>>) resultMap.get("databases");

        List<Map<String, Object>> dbLinksMaps = (List<Map<String, Object>>) resultMap.get("dbLinks");

        // 2. Instance 등록.
        for (Map<String, Object> databaseMap : databases) {
            // 있으면 수정 없으면 등록.
            Map<String, Object> databaseInstanceMap = new HashMap<>();
            databaseInstanceMap.put("projectId", databaseDto.getProjectId());
            databaseInstanceMap.put("discoveredIpAddress", databaseServerMap.get("server_ip"));
            databaseInstanceMap.put("discoveredDetailDivision", databaseServerMap.get("connection_port") + "|" + databaseMap.get("name"));

            // 삭제 여부는 판단하지 않는다.
            // 삭제된 인스턴인 경우에는 다시 되살린다.
            Long discoveredInstanceId = databaseMapper.selectDiscoveredInstanceId(databaseInstanceMap);
            Map<String, Integer> objectCountMap = getDatabaseObject(databaseMap);

            // Update
            if (discoveredInstanceId != null && discoveredInstanceId != 0) {
                // 삭제여부 무시.
                DiscoveredInstanceMaster discoveredInstanceMaster = discoveredInstanceMasterRepository.selectDiscoveredInstanceMaster(
                        databaseDto.getProjectId(), discoveredInstanceId);

                if (discoveredInstanceMaster.getPossessionInventoryId() == null) {
                    discoveredInstanceMaster.setPossessionInventoryId(databaseDto.getDatabaseId());
                    discoveredInstanceMasterRepository.save(discoveredInstanceMaster);
                }

                // discoveredInstanceMaster.setInventoryProcessId(inventoryProcessId);
                // discoveredInstanceMaster.setDeleteYn(Domain101.N.name());
                // log.debug(discoveredInstanceMaster.toString());
                // discoveredInstanceMasterRepository.save(discoveredInstanceMaster);

                // Update : DiscoveredInstanceMaster
                Map<String, Object> discoveredInstanceMasterMap = new HashMap<>();
                discoveredInstanceMasterMap.put("projectId", databaseDto.getProjectId());
                discoveredInstanceMasterMap.put("discoveredInstanceId", discoveredInstanceId);
                discoveredInstanceMasterMap.put("inventoryProcessId", inventoryProcessId);
                databaseMapper.updateDiscoveredInstanceMaster(discoveredInstanceMasterMap);

                DatabaseInstance databaseInstance = databaseInstanceRepository.findByDatabaseInstanceId(discoveredInstanceId);

                Map<String, Object> tempDatabaseInstanceMap = new HashMap<>();
                tempDatabaseInstanceMap.put("databaseInstanceId", discoveredInstanceId);
                tempDatabaseInstanceMap.put("userName", databaseDto.getUserName());
                tempDatabaseInstanceMap.put("tableCount", objectCountMap.get("tableCount"));
                tempDatabaseInstanceMap.put("viewCount", objectCountMap.get("viewCount"));
                tempDatabaseInstanceMap.put("functionCount", objectCountMap.get("functionCount"));
                tempDatabaseInstanceMap.put("procedureCount", objectCountMap.get("procedureCount"));

                databaseMapper.updateDatabaseInstance(tempDatabaseInstanceMap);

                // databaseInstance.setUserName(databaseDto.getUserName());
                // databaseInstance.setTableCount(objectCountMap.get("tableCount"));
                // databaseInstance.setViewCount(objectCountMap.get("viewCount"));
                // databaseInstance.setFunctionCount(objectCountMap.get("functionCount"));
                // databaseInstance.setProcedureCount(objectCountMap.get("procedureCount"));
                //
                // databaseInstanceRepository.save(databaseInstance);
                // log.debug(databaseInstance.toString());

                // 3. Db Link 등록. (DISCOVERED_INSTANCE_INTERFACE, DISCOVERED_INSTANCE_INTERFACE_IPS)
                createDatabaseLink(databaseDto, discoveredInstanceMaster, dbLinksMaps);
            } else {
                // insert
                DiscoveredInstanceMaster discoveredInstanceMaster = new DiscoveredInstanceMaster();
                discoveredInstanceMaster.setProjectId(databaseDto.getProjectId());
                discoveredInstanceMaster.setFinderInventoryId(databaseDto.getDatabaseId());
                discoveredInstanceMaster.setPossessionInventoryId(databaseDto.getDatabaseId());
                discoveredInstanceMaster.setInventoryProcessId(inventoryProcessId);
                discoveredInstanceMaster.setInventoryTypeCode(Domain1001.DBMS.name());
                discoveredInstanceMaster.setInventoryDetailTypeCode(databaseDto.getDatabaseType());
                discoveredInstanceMaster.setInventoryRegistTypeCode(Domain1006.INV.name());
                discoveredInstanceMaster.setDiscoveredIpAddress((String) databaseInstanceMap.get("discoveredIpAddress"));
                discoveredInstanceMaster.setDiscoveredDetailDivision((String) databaseInstanceMap.get("discoveredDetailDivision"));
                discoveredInstanceMaster.setDeleteYn(Domain101.N.name());
                discoveredInstanceMaster.setRegistDatetime(new Date());
                discoveredInstanceMaster = discoveredInstanceMasterRepository.save(discoveredInstanceMaster);

                log.debug(discoveredInstanceMaster.toString());

                DatabaseInstance databaseInstance = new DatabaseInstance();
                databaseInstance.setDatabaseInstanceId(discoveredInstanceMaster.getDiscoveredInstanceId());
                databaseInstance.setDatabaseServiceName((String) databaseMap.get("name"));
                databaseInstance.setJdbcUrl(databaseDto.getJdbcUrl());
                databaseInstance.setUserName(databaseDto.getUserName());
                databaseInstance.setTableCount(objectCountMap.get("tableCount"));
                databaseInstance.setViewCount(objectCountMap.get("viewCount"));
                databaseInstance.setFunctionCount(objectCountMap.get("functionCount"));
                databaseInstance.setProcedureCount(objectCountMap.get("procedureCount"));
                databaseInstance.setRegistUserId(WebUtil.getUserId());
                databaseInstance.setRegistDatetime(new Date());

                databaseInstanceRepository.save(databaseInstance);

                log.debug(databaseInstance.toString());

                createDatabaseLink(databaseDto, discoveredInstanceMaster, dbLinksMaps);
            }

        }

    }

    @SuppressWarnings("unchecked")
    private DatabaseSummary getDatabaseSummary(Long databaseId, Map<String, Object> resultMap) {
        DatabaseSummary databaseSummary = new DatabaseSummary();

        Map<String, Object> instanceMap = (Map<String, Object>) resultMap.get("instance");

        databaseSummary.setDatabaseInventoryId(databaseId);
        databaseSummary.setHostName(StringUtils.defaultString((String) instanceMap.get("hostName")));
        databaseSummary.setVersion(StringUtils.defaultString((String) instanceMap.get("version")));

        if (instanceMap.get("startupTime") != null) {
            databaseSummary.setStartupDatetime(new Date((Long) instanceMap.get("startupTime")));
        }

        if (instanceMap.get("dbSizeMb") != null) {
            databaseSummary.setDbSizeMb((Long) instanceMap.get("dbSizeMb"));
        }

        return databaseSummary;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Integer> getDatabaseObject(Map<String, Object> databaseMap) {
        Map<String, Integer> objectCountMap = new HashMap<>();

        List<Map<String, Object>> tables = (List<Map<String, Object>>) databaseMap.get("tables");
        List<Map<String, Object>> views = (List<Map<String, Object>>) databaseMap.get("views");
        List<Map<String, Object>> procedures = (List<Map<String, Object>>) databaseMap.get("procedures");
        List<Map<String, Object>> functions = (List<Map<String, Object>>) databaseMap.get("functions");

        int tableSize = (tables == null ? 0 : tables.size());
        int viewSize = (views == null ? 0 : views.size());
        int procedureSize = (procedures == null ? 0 : procedures.size());
        int functionSize = (functions == null ? 0 : functions.size());

        objectCountMap.put("tableCount", tableSize);
        objectCountMap.put("viewCount", viewSize);
        objectCountMap.put("procedureCount", procedureSize);
        objectCountMap.put("functionCount", functionSize);

        return objectCountMap;
    }

    @SneakyThrows
    private void createDatabaseLink(DatabaseDto databaseDto, DiscoveredInstanceMaster discoveredInstanceMaster, List<Map<String, Object>> dbLinksMaps) {
        List<DiscoveredInstanceInterface> discoveredInstanceInterfaces = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(dbLinksMaps)) {
            for (int i = 0; i < dbLinksMaps.size(); i++) {
                Map<String, Object> dblinkMap = dbLinksMaps.get(i);

                DiscoveredInstanceInterface discoveredInstanceInterface = new DiscoveredInstanceInterface();
                discoveredInstanceInterface.setDiscoveredInstanceInterfaceId(discoveredInstanceMaster.getDiscoveredInstanceId());
                discoveredInstanceInterface.setDiscoveredInstanceInterfaceSeq(i + 1);
                discoveredInstanceInterface.setDiscoveredInstanceInterfaceDetailTypeCode(Domain1109.DBLINK.name());
                discoveredInstanceInterface.setFullDescriptors(objectMapper.writeValueAsString(dblinkMap));

                if (databaseDto.getDatabaseType().equals(Domain1013.MYSQL.name()) ||
                        databaseDto.getDatabaseType().equals(Domain1013.MARIADB.name())) {
                    discoveredInstanceInterface.setDescriptorsName((String) dblinkMap.get("serverName"));
                }

                if (databaseDto.getDatabaseType().equals(Domain1013.ORACLE.name()) ||
                        databaseDto.getDatabaseType().equals(Domain1013.TIBERO.name())) {
                    discoveredInstanceInterface.setDescriptorsName((String) dblinkMap.get("dbLink"));
                }

                if (databaseDto.getDatabaseType().equals(Domain1013.MSSQL.name())) {
                    discoveredInstanceInterface.setDescriptorsName((String) dblinkMap.get("srvName"));
                }

                discoveredInstanceInterfaces.add(discoveredInstanceInterface);
            }

        }

        discoveredInstanceInterfaceRepository.deleteAllByDiscoveredInstanceInterfaceId(discoveredInstanceMaster.getDiscoveredInstanceId());
        discoveredInstanceInterfaceRepository.flush();
        discoveredInstanceInterfaceRepository.saveAll(discoveredInstanceInterfaces);
    }

    private boolean checkUnsupportedDatabase(Object result) {
        boolean isSupported = false;

        String version;
        if (result instanceof MariaDbDto) {
            io.playce.roro.db.asmt.mariadb.dto.Instance instance = ((MariaDbDto) result).getInstance();
            if (instance != null) {
                version = instance.getVersion().substring(0, instance.getVersion().indexOf("-"));
                version = version.substring(0, version.lastIndexOf("."));

                if (StringUtils.isNotEmpty(version) && (version.compareTo("5.5") >= 0 || version.compareTo("10.0") >= 0)) {
                    isSupported = true;
                }
            }
        } else if (result instanceof MsSqlDto) {
            io.playce.roro.db.asmt.mssql.dto.Instance instance = ((MsSqlDto) result).getInstance();
            if (instance != null) {
                version = instance.getVersion();

                if (StringUtils.isNotEmpty(version)) {
                    version = version.replaceAll("Microsoft ", "")
                            .replaceAll("SQL ", "")
                            .replaceAll("Server ", "");
                }

                if (StringUtils.isNotEmpty(version) && version.compareTo("2012") >= 0) {
                    isSupported = true;
                }
            }
        } else if (result instanceof MySqlDto) {
            io.playce.roro.db.asmt.mysql.dto.Instance instance = ((MySqlDto) result).getInstance();
            if (instance != null) {
                version = instance.getVersion();

                if (StringUtils.isNotEmpty(version) && version.compareTo("5.6") >= 0) {
                    isSupported = true;
                }
            }
        } else if (result instanceof OracleDto) {
            io.playce.roro.db.asmt.oracle.dto.Instance instance = ((OracleDto) result).getInstance();
            if (instance != null) {
                version = instance.getVersion();

                if (StringUtils.isNotEmpty(version) && version.compareTo("10") >= 0) {
                    isSupported = true;
                }
            }
        } else if (result instanceof SybaseDto) {
            io.playce.roro.db.asmt.sybase.dto.Instance instance = ((SybaseDto) result).getInstance();
            if (instance != null) {
                version = instance.getVersion();

                if (StringUtils.isNotEmpty(version) && version.compareTo("15.7") >= 0) {
                    isSupported = true;
                }
            }
        } else if (result instanceof TiberoDto) {
            io.playce.roro.db.asmt.tibero.dto.Instance instance = ((TiberoDto) result).getInstance();
            if (instance != null) {
                version = instance.getVersion();

                if (StringUtils.isNotEmpty(version) && version.compareTo("4") >= 0) {
                    isSupported = true;
                }
            }
        } else if (result instanceof PostgreSqlDto) {
            io.playce.roro.db.asmt.postgresql.dto.Instance instance = ((PostgreSqlDto) result).getInstance();
            if (instance != null) {
                version = instance.getVersion();

                if (StringUtils.isNotEmpty(version) && version.compareTo("11") >= 0) {
                    isSupported = true;
                }
            }
        }

        return isSupported;
    }

    private String getDatabaseVendor(String databaseType) {
        String databaseVendor = "";

        if (databaseType.equals(DATABASE_TYPE_ORACLE) || databaseType.equals(DATABASE_TYPE_MYSQL)) {
            databaseVendor = "Oracle";
        } else if (databaseType.equals(DATABASE_TYPE_MARIADB)) {
            databaseVendor = "MariaDB Foundation";
        } else if (databaseType.equals(DATABASE_TYPE_TIBERO)) {
            databaseVendor = "TmaxSoft";
        } else if (databaseType.equals(DATABASE_TYPE_MSSQL)) {
            databaseVendor = "Microsoft";
        } else if (databaseType.equals(DATABASE_TYPE_SYBASE)) {
            databaseVendor = "SAP";
        } else if (databaseType.equals(DATABASE_TYPE_POSTGRESQL)) {
            databaseVendor = "PostgreSQL Global Development Group";
        }

        return databaseVendor;
    }

}