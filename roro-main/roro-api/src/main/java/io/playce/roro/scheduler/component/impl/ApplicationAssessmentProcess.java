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

import io.playce.roro.api.common.util.WebUtil;
import io.playce.roro.api.domain.insights.service.InsightsService;
import io.playce.roro.api.domain.inventory.service.ApplicationService;
import io.playce.roro.api.domain.inventory.service.InventoryProcessService;
import io.playce.roro.app.asmt.ApplicationAssessment;
import io.playce.roro.app.asmt.ApplicationScanConfig;
import io.playce.roro.app.asmt.result.ApplicationAssessmentResult;
import io.playce.roro.app.asmt.result.ApplicationAssessmentResult.DataSource.JdbcProperty;
import io.playce.roro.app.asmt.support.ApplicationAssessmentHelper;
import io.playce.roro.app.asmt.util.ApplicationFileUtil;
import io.playce.roro.common.cancel.InventoryProcessCancelInfo;
import io.playce.roro.common.code.*;
import io.playce.roro.common.config.RoRoProperties;
import io.playce.roro.common.dto.assessment.ApplicationDto;
import io.playce.roro.common.dto.common.InventoryProcessConnectionInfo;
import io.playce.roro.common.dto.common.RemoteExecResult;
import io.playce.roro.common.dto.inventory.application.ApplicationDetailResponse;
import io.playce.roro.common.dto.inventory.process.InventoryProcessQueueItem;
import io.playce.roro.common.exception.InsufficientException;
import io.playce.roro.common.exception.NotsupportedException;
import io.playce.roro.common.util.CommandUtil;
import io.playce.roro.common.util.SSHUtil;
import io.playce.roro.common.util.ThreadLocalUtils;
import io.playce.roro.common.util.WinRmUtils;
import io.playce.roro.common.util.support.TargetHost;
import io.playce.roro.jpa.entity.*;
import io.playce.roro.jpa.repository.*;
import io.playce.roro.mw.asmt.component.CommandConfig;
import io.playce.roro.mw.asmt.enums.COMMAND;
import io.playce.roro.mw.asmt.util.GetInfoStrategy;
import io.playce.roro.mw.asmt.util.MWCommonUtil;
import io.playce.roro.mybatis.domain.inventory.application.ApplicationMapper;
import io.playce.roro.mybatis.domain.inventory.server.ServerMapper;
import io.playce.roro.mybatis.domain.thirdparty.ThirdPartyMapper;
import io.playce.roro.scheduler.component.AbstractAssessmentProcess;
import io.playce.roro.scheduler.service.impl.AssessmentSchedulerManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.playce.roro.common.util.ThreadLocalUtils.APP_SCAN_ERROR;
import static io.playce.roro.mw.asmt.util.MWCommonUtil.getJavaVendorProperty;

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
public class ApplicationAssessmentProcess extends AbstractAssessmentProcess {

    private final InventoryProcessService inventoryProcessService;
    private final ApplicationService applicationService;
    private final InsightsService insightsService;
    private final ServerMapper serverMapper;
    private final ApplicationMapper applicationMapper;
    private final Map<String, ApplicationAssessment> applicationAssessmentMap;
    private final ApplicationStatusRepository applicationStatusRepository;
    private final InventoryMasterRepository inventoryMasterRepository;
    private final ApplicationMasterRepository applicationMasterRepository;
    private final DiscoveredInstanceMasterRepository discoveredInstanceMasterRepository;
    private final DiscoveredInstanceInterfaceRepository discoveredInstanceInterfaceRepository;
    private final DiscoveredInstanceInterfaceIpsRepository discoveredInstanceInterfaceIpsRepository;
    private final DatabaseInstanceRepository databaseInstanceRepository;
    private final ExternalConnectionRepository externalConnectionRepository;
    private final ThirdPartyMapper thirdPartyMapper;
    private final RoRoProperties roroProperties;
    private final CommandConfig commandConfig;
    private final ApplicationScanConfig applicationScanConfig;


    @Override
    public Domain1003 assessment(InventoryProcessQueueItem item, Domain1003 resultState) throws InterruptedException {
        Long inventoryProcessId = item.getInventoryProcessId();

        ApplicationAssessmentResult result = null;
        String resultString = null;
        boolean saveReport = false;

        ApplicationDto applicationDto = new ApplicationDto();
        InventoryProcessConnectionInfo connectionInfo;
        TargetHost targetHost;
        try {
            ApplicationDetailResponse application = applicationService.getApplication(item.getProjectId(), item.getInventoryId());

            connectionInfo = serverMapper.selectServerConnectionInfoByInventoryProcessId(item.getInventoryProcessId());
            log.debug("Step 4 ~ id: {}. load connection info: {}", inventoryProcessId, connectionInfo);

            targetHost = InventoryProcessConnectionInfo.targetHost(connectionInfo);

            if (!targetHost.isValid()) {
                throw new InsufficientException("Insufficient server connection information.");
            }

            // List<ThirdPartySearchTypeResponse> thirdPartySearchTypeList = thirdPartyMapper.selectThirdPartySearchType();
            // targetHost.setThirdPartySearchTypeList(thirdPartySearchTypeList);

            applicationDto.setInventoryProcessId(inventoryProcessId);
            applicationDto.setServerId(application.getServerInventoryId());
            applicationDto.setTargetHost(targetHost);
            applicationDto.setApplicationId(application.getApplicationInventoryId());
            applicationDto.setWindowsYn(connectionInfo.getWindowsYn());
            applicationDto.setDeployPath(application.getDeployPath());
            applicationDto.setSourceLocationUri(application.getSourceLocationUri());
            applicationDto.setUploadSourceFileName(application.getUploadSourceFileName());
            applicationDto.setUploadSourceFilePath(application.getUploadSourceFilePath());
            applicationDto.setAnalysisLibList(application.getAnalysisLibList());
            applicationDto.setAnalysisStringList(application.getAnalysisStringList());
            applicationDto.setSsh(roroProperties.getSsh());

            // implement된 component name 생성 (eg. JavaAssessment)
            String componentName = makeComponentName(application.getInventoryDetailTypeCode());

            ApplicationAssessment assessment = applicationAssessmentMap.get(componentName);
            if (assessment == null) {
                // throw new RoRoException("The processing component does not exist.");
                throw new NotsupportedException("Scan cannot be performed. It is not supported Application.");
            }
            result = assessment.assessment(applicationDto);

            // Scan이 정상적으로 완료된 경우 상태에 관계없이 Report 생성 대상이 된다.
            saveReport = true;

            /**
             * Run Application Scan Post Process 
             */
            synchronized (AssessmentSchedulerManager.lockApp) {
                if (!InventoryProcessCancelInfo.hasCancelRequest(item.getInventoryProcessId())) {
                    try {
                        runPostProcessing(application, inventoryProcessId, result, applicationDto);
                    } catch (Exception e) {
                        log.error("Unhandled exception occurred while execute application scan's post processing.", e);

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

            // Partial Completed Check
            if (ThreadLocalUtils.get(APP_SCAN_ERROR) == null) {
                resultState = Domain1003.CMPL;
            } else {
                resultState = Domain1003.PC;
                resultString = (String) ThreadLocalUtils.get(APP_SCAN_ERROR);
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

            synchronized (AssessmentSchedulerManager.lockApp) {
                if (!InventoryProcessCancelInfo.hasCancelRequest(item.getInventoryProcessId())) {
                    String message = saveResult(item, inventoryProcessService, inventoryProcessId, result, resultString, saveReport);

                    if (StringUtils.isNotEmpty(message)) {
                        if (resultState.equals(Domain1003.CMPL)) {
                            resultState = Domain1003.PC;
                        }
                    }
                }
            }

            try {
                if (result.getAssessmentDir() != null && !result.getAssessmentDir().equals(result.getApplicationDir())) {
                    remove(result.getAssessmentDir());
                }

                if (StringUtils.isEmpty(applicationDto.getUploadSourceFilePath())) {
                    if (applicationScanConfig.getRemove().isFilesAfterScan()) {
                        remove(result.getApplicationDir());
                    }
                }
            } catch (Exception e) {
                // ignore
                log.warn("Unhandled exception occurred while remove application directory. Reason : [{}]", e.getMessage());
            }
        }

        return resultState;
    }

    /**
     * @param application
     * @param inventoryProcessId
     * @param result
     * @param applicationDto
     */
    @Transactional(rollbackFor = Exception.class)
    public void runPostProcessing(ApplicationDetailResponse application, Long inventoryProcessId, ApplicationAssessmentResult result, ApplicationDto applicationDto) throws Exception {
        InventoryMaster inventoryMaster = inventoryMasterRepository.findById(application.getApplicationInventoryId()).orElse(null);
        ApplicationStatus applicationStatus = new ApplicationStatus();
        applicationStatus.setApplicationInventoryId(application.getApplicationInventoryId());

        if (inventoryMaster != null && result != null && result.getApplicationType() != null) {
            String type = result.getApplicationType().toLowerCase();

            if (type.contains("java")) {
                if (type.contains("enterprise")) {
                    inventoryMaster.setInventoryDetailTypeCode(Domain1013.EAR.enname());
                } else if (type.contains("web")) {
                    inventoryMaster.setInventoryDetailTypeCode(Domain1013.WAR.enname());
                } else {
                    inventoryMaster.setInventoryDetailTypeCode(Domain1013.JAR.enname());
                }

                applicationStatus.setDevelopLanguage("Java");

                // applicationStatus 개발 당시 자바 버전을 유추
                List<Path> list;
                String javaVersion = null;
                try (Stream<Path> paths = Files.walk(Path.of(result.getAssessmentDir()))) {
                    list = paths.filter(
                                    path -> (path.toString().contains("/META-INF") && path.toString().endsWith("MANIFEST.MF")) ||
                                            (path.toString().contains("/WEB-INF") && path.toString().endsWith(".class"))
                            ).sorted() // MANIFEST 우선
                            .collect(Collectors.toList());
                }
                for (Path path : list) {
                    CommandLine commandLine = CommandUtil.getCommandLine(
                            CommandUtil.findCommand("sh"),
                            ApplicationAssessmentHelper.getJavapFile().getAbsolutePath(),
                            path.toString()
                    );
                    javaVersion = CommandUtil.executeCommand(commandLine);
                    if (!javaVersion.isBlank())
                        break;
                }
                applicationStatus.setDevelopLanguageVersion(javaVersion);

                // applicationStatus 프레임워크 정보
                List<ApplicationAssessmentResult.File> libraries = result.getLibraries().getAll();
                if (!libraries.isEmpty()) {
                    List<String> prioiryList = Arrays.asList("org\\.egovframe\\.rte\\..*\\.jar", "spring-boot-\\d+(\\.\\d+)*\\.jar", "spring-core-\\d+(\\.\\d+)*\\.jar");
                    String regex = String.join("|", prioiryList);

                    libraries.stream().map(ApplicationAssessmentResult.File::getFile)
                            .filter(filename ->
                                    filename.matches(regex)
                            ).sorted() // o => s-b => s-c
                            .findFirst()
                            .ifPresent(string -> {
                                String version = string.substring(string.lastIndexOf("-") + 1, string.lastIndexOf(".jar"));
                                String framework = null;
                                if (string.contains("org.egovframe")) {
                                    framework = "eGovFrame";
                                } else if (string.contains("spring-boot")) {
                                    framework = "Spring Boot";
                                } else if (string.contains("spring-core")) {
                                    framework = "Spring";
                                }
                                applicationStatus.setFrameworkName(framework);
                                applicationStatus.setFrameworkVersion(version);
                            });
                }
            } else {
                inventoryMaster.setInventoryDetailTypeCode(Domain1013.ETC.name());
            }

            inventoryMasterRepository.save(inventoryMaster);
        }

        // application_master 테이블에 size를 업데이트한다.
        ApplicationMaster applicationMaster = applicationMasterRepository.findById(application.getApplicationInventoryId()).orElse(null);

        // Middleware에서 Java 정보를 가져온다.
        Map<String, String> javaInfoMap = applicationMapper.selectApplicationJavaInfo(application.getProjectId(), application.getApplicationInventoryId());

        // Java 정보가 없으면 서버에 직접 접속해서 찾아본다.
        // process 정보에 bin/java 와 deploy Path를 체크한다.
        if (applicationMaster != null && ObjectUtils.isEmpty(javaInfoMap)) {
            javaInfoMap = new HashMap<>();

            String command;
            String response;

            if (applicationDto.getWindowsYn().equals("Y")) {
                command = "wmic process where 'CommandLine like \"%java%\" and CommandLine like \"%" + applicationMaster.getDeployPath() + "%\" and not CommandLine like \"%wmic%\"' get CommandLine /format:list";
                response = WinRmUtils.executeCommand(applicationDto.getTargetHost(), command)
                        .replaceAll("\\s+", " ")
                        .replaceAll("\"", "");

                if (StringUtils.isNotEmpty(response)) {
                    response = StringUtils.defaultString(response.substring("CommandLine=".length()));
                } else {
                    response = StringUtils.EMPTY;
                }
            } else {
                command = "ps -ef | grep -v 'grep'| grep java | grep " + applicationMaster.getDeployPath();
                response = SSHUtil.executeCommand(applicationDto.getTargetHost(), command).replaceAll("\\s+", " ");
            }

            javaInfoMap.put("javaVersion", getJavaVersion(response, applicationDto));
            javaInfoMap.put("javaVendor", getJavaVendor(response, applicationDto));
        }

        if (applicationMaster != null) {
            Long fileSize = result.getApplicationSize();
            if (fileSize == null || fileSize == 0L) {
                File f = new File(result.getApplicationFile());

                if (f.exists()) {
                    fileSize = FileUtils.sizeOf(f);
                }
            }

            applicationMaster.setJavaVersion(javaInfoMap.get("javaVersion"));
            applicationMaster.setJavaVendor(javaInfoMap.get("javaVendor"));
            applicationMaster.setApplicationSize(fileSize);
            applicationMasterRepository.save(applicationMaster);
        }

        if (StringUtils.isNotEmpty(applicationMaster.getJavaVendor()) && StringUtils.isNotEmpty(applicationMaster.getJavaVersion())) {
            insightsService.createInventoryLifecycleVersionLink(application.getApplicationInventoryId(), Domain1001.APP, "", "",
                    applicationMaster.getJavaVendor(), applicationMaster.getJavaVersion());
        } else {
            log.debug("Not Found Java Vendor : {}, Java Version : {}", applicationMaster.getJavaVendor(), applicationMaster.getJavaVersion());
        }

        // External Connection 정보를 저장한다.
        List<ApplicationAssessmentResult.HardCodedIp> hardCodedIPAddressList = result.getHardCodedIpList();
        externalConnectionRepository.deleteByApplicationInventoryId(application.getApplicationInventoryId());

        for (ApplicationAssessmentResult.HardCodedIp hardCodedIPAddress : hardCodedIPAddressList) {
            ExternalConnection externalConnection = new ExternalConnection();
            externalConnection.setFileName(FilenameUtils.getName(hardCodedIPAddress.getFileName()));
            externalConnection.setLineNum(hardCodedIPAddress.getLineNum());
            externalConnection.setApplicationInventoryId(application.getApplicationInventoryId());
            externalConnection.setIp(hardCodedIPAddress.getIpAddress());
            externalConnection.setPort(hardCodedIPAddress.getPort());
            externalConnection.setProtocol(hardCodedIPAddress.getProtocol());

            externalConnectionRepository.save(externalConnection);
        }

        // discovered_instance 관련 테이블에 DataSource 관련 정보를 저장한다.
        // 1. discovered_instance 테이블에 애플리케이션 정보를 저장한다. (애플리케이션 인벤토리 아이디로 조회 후 없으면 등록)
        DiscoveredInstanceMaster discoveredInstanceMaster = discoveredInstanceMasterRepository.findByPossessionInventoryId(application.getApplicationInventoryId())
                .orElse(null);

        if (discoveredInstanceMaster == null) {
            discoveredInstanceMaster = new DiscoveredInstanceMaster();
            discoveredInstanceMaster.setPossessionInventoryId(application.getApplicationInventoryId());
            discoveredInstanceMaster.setFinderInventoryId(application.getApplicationInventoryId());
            discoveredInstanceMaster.setInventoryTypeCode(application.getInventoryTypeCode());
            discoveredInstanceMaster.setInventoryDetailTypeCode(application.getInventoryDetailTypeCode());
            if ("N".equals(application.getAutomaticRegistYn())) {
                discoveredInstanceMaster.setInventoryRegistTypeCode(Domain1006.INV.name());
            } else {
                discoveredInstanceMaster.setInventoryRegistTypeCode(Domain1006.DISC.name());
            }
            discoveredInstanceMaster.setProjectId(application.getProjectId());
            discoveredInstanceMaster.setDiscoveredIpAddress(application.getRepresentativeIpAddress());
            discoveredInstanceMaster.setDiscoveredDetailDivision(application.getDeployPath());
            discoveredInstanceMaster.setRegistDatetime(new Date());
            discoveredInstanceMaster.setDeleteYn(Domain101.N.name());
            discoveredInstanceMaster.setInventoryProcessId(inventoryProcessId);

            discoveredInstanceMaster = discoveredInstanceMasterRepository.save(discoveredInstanceMaster);
        }

        // 2. discovered_instance_interface_ips, discovered_instance_interface 테이블의 정보를 삭제한다.
        discoveredInstanceInterfaceIpsRepository.deleteAllByDiscoveredInstanceInterfaceId(discoveredInstanceMaster.getDiscoveredInstanceId());
        discoveredInstanceInterfaceRepository.deleteAllByDiscoveredInstanceInterfaceId(discoveredInstanceMaster.getDiscoveredInstanceId());

        // 3. discovered_instance_master, discovered_instance_interface 및 discovered_instance_interface_ips 테이블에 인터페이스 정보를 저장한다.
        ApplicationAssessmentResult.DataSource dataSource;
        Set<String> dbmsSet = new HashSet<>();
        int seq = 0;
        for (int i = 0; i < result.getDataSourceList().size(); i++) {
            dataSource = result.getDataSourceList().get(i);

            // datasource type이 jdbc일 때랑 아닐 때랑 나눠진다.
            if (Domain1109.JDBC.name().equals(dataSource.getType())) {
                // application status useDbms

                for (int j = 0; j < dataSource.getJdbcProperties().size(); j++) {
                    JdbcProperty jdbcProperty = dataSource.getJdbcProperties().get(j);

                    if (StringUtils.isNotEmpty(jdbcProperty.getHost())) {
                        // https://cloud-osci.atlassian.net/browse/ROROQA-1046
                        // host 정보가 100보다 크다면 파싱이 제대로 이루어지지 않은 Garbage 데이터일 확률이 높으며, 로깅만 하고 DB에는 저장하지 않는다.
                        if (jdbcProperty.getHost().length() > 100) {
                            log.info("Detected host's address is too large. [{}]", jdbcProperty.getHost());
                            continue;
                        }


                        String detailDivision = jdbcProperty.getPort() + "|" + jdbcProperty.getDatabase();
                        String descriptorName = jdbcProperty.getDatabase();

                        // application status useDbms
                        String dbname;
                        try {
                            dbname = Domain1013.valueOf(jdbcProperty.getType()).enname();
                        } catch (Exception e) {
                            dbname = WordUtils.capitalize(jdbcProperty.getType().toLowerCase());
                        }
                        dbmsSet.add(dbname);

                        // DiscoveredInstanceMaster 저장.
                        DiscoveredInstanceMaster discoveredInstanceMasterForDatabase = discoveredInstanceMasterRepository
                                .findByProjectIdAndDiscoveredIpAddressAndDiscoveredDetailDivision(application.getProjectId(), jdbcProperty.getHost(), detailDivision)
                                .orElse(new DiscoveredInstanceMaster());

                        discoveredInstanceMasterForDatabase.setFinderInventoryId(application.getApplicationInventoryId());
                        discoveredInstanceMasterForDatabase.setInventoryTypeCode(Domain1001.DBMS.name());
                        discoveredInstanceMasterForDatabase.setInventoryDetailTypeCode(jdbcProperty.getType());
                        discoveredInstanceMasterForDatabase.setInventoryRegistTypeCode(Domain1006.DISC.name());
                        discoveredInstanceMasterForDatabase.setProjectId(application.getProjectId());
                        discoveredInstanceMasterForDatabase.setDiscoveredIpAddress(jdbcProperty.getHost());
                        discoveredInstanceMasterForDatabase.setDiscoveredDetailDivision(detailDivision);
                        discoveredInstanceMasterForDatabase.setRegistDatetime(new Date());
                        discoveredInstanceMasterForDatabase.setDeleteYn(Domain101.N.name());
                        discoveredInstanceMasterForDatabase.setInventoryProcessId(inventoryProcessId);
                        discoveredInstanceMasterForDatabase = discoveredInstanceMasterRepository.save(discoveredInstanceMasterForDatabase);

                        DatabaseInstance databaseInstance = databaseInstanceRepository.findById(discoveredInstanceMasterForDatabase.getDiscoveredInstanceId()).orElse(new DatabaseInstance());
                        databaseInstance.setDatabaseInstanceId(discoveredInstanceMasterForDatabase.getDiscoveredInstanceId());
                        databaseInstance.setDatabaseServiceName(jdbcProperty.getDatabase());
                        databaseInstance.setJdbcUrl(dataSource.getValue());
                        databaseInstance.setRegistUserId(WebUtil.getUserId());
                        databaseInstance.setUserName(null);
                        databaseInstance.setRegistDatetime(new Date());
                        databaseInstanceRepository.save(databaseInstance);

                        // DiscoveredInstanceInterface save.
                        DiscoveredInstanceInterface discoveredInstanceInterface = new DiscoveredInstanceInterface();
                        discoveredInstanceInterface.setDiscoveredInstanceInterfaceId(discoveredInstanceMaster.getDiscoveredInstanceId());
                        discoveredInstanceInterface.setDiscoveredInstanceInterfaceSeq(++seq);
                        discoveredInstanceInterface.setDiscoveredInstanceInterfaceDetailTypeCode(Domain1109.JDBC.name());

                        // https://cloud-osci.atlassian.net/browse/ROROQA-1048
                        if (StringUtils.isNotEmpty(descriptorName) && descriptorName.length() > 100) {
                            discoveredInstanceInterface.setDescriptorsName(descriptorName.substring(0, 100));
                        } else {
                            discoveredInstanceInterface.setDescriptorsName(descriptorName);
                        }

                        List<String> descriptors = new ArrayList<>();
                        for (ApplicationAssessmentResult.DataSource.Use use : dataSource.getUses()) {
                            descriptors.add(use.getValue());
                        }
                        descriptors = descriptors.stream().distinct().collect(Collectors.toList());

                        StringBuilder sb = new StringBuilder();
                        sb.append(String.join(",", descriptors));

                        if (sb.length() > 512) {
                            discoveredInstanceInterface.setFullDescriptors(sb.substring(0, 512));
                        } else {
                            discoveredInstanceInterface.setFullDescriptors(sb.toString());
                        }

                        discoveredInstanceInterfaceRepository.save(discoveredInstanceInterface);

                        DiscoveredInstanceInterfaceIps discoveredInstanceInterfaceIps = new DiscoveredInstanceInterfaceIps();
                        discoveredInstanceInterfaceIps.setDiscoveredInstanceInterfaceId(discoveredInstanceMaster.getDiscoveredInstanceId());
                        discoveredInstanceInterfaceIps.setDiscoveredInstanceInterfaceSeq(seq);
                        discoveredInstanceInterfaceIps.setDiscoveredInstanceInterfaceIpAddress(StringUtils.defaultString(jdbcProperty.getHost()));
                        discoveredInstanceInterfaceIps.setServiceName(jdbcProperty.getDatabase());
                        discoveredInstanceInterfaceIps.setServicePort(jdbcProperty.getPort() == null ? 0 : jdbcProperty.getPort());
                        discoveredInstanceInterfaceIps.setUserName(null);
                        discoveredInstanceInterfaceIps.setUserPassword(null);

                        discoveredInstanceInterfaceIpsRepository.save(discoveredInstanceInterfaceIps);
                    }
                }
            } else {
                String descriptorName = dataSource.getValue();

                if (StringUtils.isEmpty(descriptorName)) {
                    continue;
                }

                // 3-2. ApplicationAssessmentResult의 dataSource 정보를 기준으로 discovered_instance_interface 데이터를 저장한다.
                DiscoveredInstanceInterface discoveredInstanceInterface = new DiscoveredInstanceInterface();
                discoveredInstanceInterface.setDiscoveredInstanceInterfaceId(discoveredInstanceMaster.getDiscoveredInstanceId());
                discoveredInstanceInterface.setDiscoveredInstanceInterfaceSeq(++seq);
                discoveredInstanceInterface.setDiscoveredInstanceInterfaceDetailTypeCode(dataSource.getType());

                if (descriptorName.length() > 100) {
                    discoveredInstanceInterface.setDescriptorsName(descriptorName.substring(0, 100));
                } else {
                    discoveredInstanceInterface.setDescriptorsName(descriptorName);
                }

                List<String> descriptors = new ArrayList<>();
                for (ApplicationAssessmentResult.DataSource.Use use : dataSource.getUses()) {
                    descriptors.add(use.getValue());
                }
                descriptors = descriptors.stream().distinct().collect(Collectors.toList());

                StringBuilder sb = new StringBuilder();
                sb.append(String.join(",", descriptors));

                if (sb.length() > 512) {
                    discoveredInstanceInterface.setFullDescriptors(sb.substring(0, 512));
                } else {
                    discoveredInstanceInterface.setFullDescriptors(sb.toString());
                }

                discoveredInstanceInterfaceRepository.save(discoveredInstanceInterface);
            }

//            String descriptorName;
//            if (dataSource.getJdbcProperty() != null) {
//                descriptorName = dataSource.getJdbcProperty().getDatabase();
//            } else {
//                descriptorName = dataSource.getValue();
//            }

//            if (StringUtils.isEmpty(descriptorName)) {
//                continue;
//            }

            // 3-1. ApplicationAssessmentResult의 dataSource 정보를 기준으로 discovered_instance_master 데이터를 저장한다.
//            if (Domain1109.JDBC.name().equals(dataSource.getType()) && dataSource.getJdbcProperty() != null && dataSource.getJdbcProperty().getHost() != null) {
//                String detailDivision = dataSource.getJdbcProperty().getPort() + "|" + dataSource.getJdbcProperty().getDatabase();
//
//                DiscoveredInstanceMaster discoveredInstanceMasterForDatabase = discoveredInstanceMasterRepository
//                        .findByProjectIdAndDiscoveredIpAddressAndDiscoveredDetailDivision(application.getProjectId(), dataSource.getJdbcProperty().getHost(), detailDivision)
//                        .orElse(new DiscoveredInstanceMaster());
//
//                discoveredInstanceMasterForDatabase.setFinderInventoryId(application.getApplicationInventoryId());
//                discoveredInstanceMasterForDatabase.setInventoryTypeCode(Domain1001.DBMS.name());
//                discoveredInstanceMasterForDatabase.setInventoryDetailTypeCode(dataSource.getJdbcProperty().getType());
//                discoveredInstanceMasterForDatabase.setInventoryRegistTypeCode(Domain1006.DISC.name());
//                discoveredInstanceMasterForDatabase.setProjectId(application.getProjectId());
//                discoveredInstanceMasterForDatabase.setDiscoveredIpAddress(dataSource.getJdbcProperty().getHost());
//                discoveredInstanceMasterForDatabase.setDiscoveredDetailDivision(detailDivision);
//                discoveredInstanceMasterForDatabase.setRegistDatetime(new Date());
//                discoveredInstanceMasterForDatabase.setDeleteYn(Domain101.N.name());
//                discoveredInstanceMasterForDatabase.setInventoryProcessId(inventoryProcessId);
//                discoveredInstanceMasterForDatabase = discoveredInstanceMasterRepository.save(discoveredInstanceMasterForDatabase);
//
//                DatabaseInstance databaseInstance = databaseInstanceRepository.findById(discoveredInstanceMasterForDatabase.getDiscoveredInstanceId()).orElse(new DatabaseInstance());
//                databaseInstance.setDatabaseInstanceId(discoveredInstanceMasterForDatabase.getDiscoveredInstanceId());
//                databaseInstance.setDatabaseServiceName(dataSource.getJdbcProperty().getDatabase());
//                databaseInstance.setJdbcUrl(dataSource.getValue());
//                databaseInstance.setRegistUserId(WebUtil.getUserId());
//                databaseInstance.setUserName(null);
//                databaseInstance.setRegistDatetime(new Date());
//                databaseInstanceRepository.save(databaseInstance);
//            }
//
//            // 3-2. ApplicationAssessmentResult의 dataSource 정보를 기준으로 discovered_instance_interface 데이터를 저장한다.
//            DiscoveredInstanceInterface discoveredInstanceInterface = new DiscoveredInstanceInterface();
//            discoveredInstanceInterface.setDiscoveredInstanceInterfaceId(discoveredInstanceMaster.getDiscoveredInstanceId());
//            discoveredInstanceInterface.setDiscoveredInstanceInterfaceSeq(i + 1);
//            discoveredInstanceInterface.setDiscoveredInstanceInterfaceDetailTypeCode(dataSource.getType());
//
//            if (descriptorName.length() > 100) {
//                discoveredInstanceInterface.setDescriptorsName(descriptorName.substring(0, 100));
//            } else {
//                discoveredInstanceInterface.setDescriptorsName(descriptorName);
//            }
//
//            // StringBuilder sb = new StringBuilder();
//            // for (ApplicationAssessmentResult.DataSource.Use use : dataSource.getUses()) {
//            //     if (sb.length() > 0) {
//            //         sb.append(",");
//            //     }
//            //
//            //     sb.append(use.getValue());
//            // }
//
//            List<String> descriptors = new ArrayList<>();
//            for (ApplicationAssessmentResult.DataSource.Use use : dataSource.getUses()) {
//                descriptors.add(use.getValue());
//            }
//            descriptors = descriptors.stream().distinct().collect(Collectors.toList());
//
//            StringBuilder sb = new StringBuilder();
//            sb.append(String.join(",", descriptors));
//
//            if (sb.length() > 512) {
//                discoveredInstanceInterface.setFullDescriptors(sb.substring(0, 512));
//            } else {
//                discoveredInstanceInterface.setFullDescriptors(sb.toString());
//            }
//
//            discoveredInstanceInterfaceRepository.save(discoveredInstanceInterface);
//
//            // 3-3. datasource의 jdbcProperty 정보를 기준으로 discovered_instance_interface_ips 데이터를 저장한다.
//            if (Domain1109.JDBC.name().equals(dataSource.getType())) {
//                ApplicationAssessmentResult.DataSource.JdbcProperty jdbcProperty = dataSource.getJdbcProperty();
//
//                if (jdbcProperty != null) {
//                    DiscoveredInstanceInterfaceIps discoveredInstanceInterfaceIps = new DiscoveredInstanceInterfaceIps();
//                    discoveredInstanceInterfaceIps.setDiscoveredInstanceInterfaceId(discoveredInstanceMaster.getDiscoveredInstanceId());
//                    discoveredInstanceInterfaceIps.setDiscoveredInstanceInterfaceSeq(i + 1);
//                    discoveredInstanceInterfaceIps.setDiscoveredInstanceInterfaceIpAddress(jdbcProperty.getHost());
//                    discoveredInstanceInterfaceIps.setServiceName(jdbcProperty.getDatabase());
//                    discoveredInstanceInterfaceIps.setServicePort(jdbcProperty.getPort());
//                    discoveredInstanceInterfaceIps.setUserName(null);
//                    discoveredInstanceInterfaceIps.setUserPassword(null);
//
//                    discoveredInstanceInterfaceIpsRepository.save(discoveredInstanceInterfaceIps);
//                }
//            }
        }

        String useDbms = dbmsSet.toString().replaceAll("[\\[\\]]", "");
        applicationStatus.setUseDbms(useDbms);
        // application status https 여부
        String YN = applicationMapper.selectMiddlewareInstanceProtocolHttpsYN(application.getApplicationInventoryId());
        applicationStatus.setHttpsUseYn(YN);
        applicationStatusRepository.save(applicationStatus);
    }

    private String getJavaVersion(String response, ApplicationDto applicationDto) throws InterruptedException {
        GetInfoStrategy strategy = GetInfoStrategy.getStrategy(applicationDto.getWindowsYn().equals("Y"));

        if (StringUtils.isNotEmpty(response)) {
            String[] responseArray = response.split(" ");
            for (String splitString : responseArray) {
                if (splitString.contains("/bin/java") || splitString.contains("\\bin\\java")) {
                    boolean sudo = !strategy.isWindows() && SSHUtil.isSudoer(applicationDto.getTargetHost());

                    Map<String, String> commandMap = Map.of(COMMAND.JAVA_VERSION.name(), COMMAND.JAVA_VERSION.command(commandConfig, strategy.isWindows(), splitString));
                    Map<String, RemoteExecResult> resultMap = MWCommonUtil.executeCommand(applicationDto.getTargetHost(), commandMap, sudo, strategy);
                    RemoteExecResult result = resultMap.get(COMMAND.JAVA_VERSION.name());

                    if (!result.isErr()) {
                        return result.getResult().trim();
                    }
                }
            }
        }

        return null;
    }

    private String getJavaVendor(String response, ApplicationDto applicationDto) throws InterruptedException {
        GetInfoStrategy strategy = GetInfoStrategy.getStrategy(applicationDto.getWindowsYn().equals("Y"));

        if (StringUtils.isNotEmpty(response)) {
            String[] responseArray = response.split(" ");
            for (String splitString : responseArray) {
                if (splitString.contains("/bin/java") || splitString.contains("\\bin\\java")) {
                    boolean sudo = !strategy.isWindows() && SSHUtil.isSudoer(applicationDto.getTargetHost());

                    Map<String, String> commandMap = Map.of(COMMAND.JAVA_VENDOR.name(), COMMAND.JAVA_VENDOR.command(commandConfig, strategy.isWindows(), splitString));
                    Map<String, RemoteExecResult> resultMap = MWCommonUtil.executeCommand(applicationDto.getTargetHost(), commandMap, sudo, strategy);
                    RemoteExecResult result = resultMap.get(COMMAND.JAVA_VENDOR.name());

                    if (!result.isErr()) {
                        return getJavaVendorProperty(result.getResult().trim());
                    }
                }
            }
        }

        return null;
    }

    private void remove(String dir) {
        ApplicationFileUtil.rm(dir);
    }
}