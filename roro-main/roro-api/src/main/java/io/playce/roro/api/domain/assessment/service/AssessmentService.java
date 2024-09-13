package io.playce.roro.api.domain.assessment.service;/*
 * Copyright 2021 The Playce-RoRo Project.
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
 * Hoon Oh          12월 16, 2021		First Draft.
 */

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import io.playce.roro.api.common.error.ErrorCode;
import io.playce.roro.api.common.error.exception.ResourceNotFoundException;
import io.playce.roro.api.common.error.exception.RoRoApiException;
import io.playce.roro.api.common.i18n.LocaleMessageConvert;
import io.playce.roro.api.common.util.WebUtil;
import io.playce.roro.api.domain.inventory.service.InventoryProcessService;
import io.playce.roro.common.cancel.InventoryProcessCancelInfo;
import io.playce.roro.common.code.*;
import io.playce.roro.common.config.InventoryProcessCancelProcessor;
import io.playce.roro.common.dto.assessment.AssessmentResponseDto;
import io.playce.roro.common.dto.assessment.PageAssessmentRequestDto;
import io.playce.roro.common.dto.inventory.process.InventoryProcessDetailResponse;
import io.playce.roro.common.dto.inventory.process.InventoryProcessListResponse;
import io.playce.roro.common.dto.inventory.server.JavaProcessResponse;
import io.playce.roro.common.util.JsonUtil;
import io.playce.roro.jpa.entity.*;
import io.playce.roro.jpa.repository.*;
import io.playce.roro.mybatis.domain.inventory.process.InventoryProcessMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static io.playce.roro.api.common.CommonConstants.YES;
import static io.playce.roro.api.common.error.ErrorCode.ASSESSMENT_DUPLICATED;
import static io.playce.roro.api.common.error.ErrorCode.RESOURCE_NOT_FOUND;

/**
 * <pre>
 *
 *
 * </pre>
 *
 * @author Hoon Oh
 * @version 1.0r
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AssessmentService {

    private final LocaleMessageConvert localeMessageConvert;

    private final InventoryProcessService inventoryProcessService;
    private final InventoryProcessMapper inventoryProcessMapper;

    private final InventoryProcessRepository inventoryProcessRepository;
    private final InventoryMasterRepository inventoryMasterRepository;

    private final ServiceInventoryRepository serviceInventoryRepository;
    private final ServiceMasterRepository serviceMasterRepository;
    private final ServerMasterRepository serverMasterRepository;
    private final InventoryProcessJsonDetailRepository inventoryProcessJsonDetailRepository;
    private final InventoryProcessCancelProcessor inventoryProcessCancelProcessor;

    /**
     * https://cloud-osci.atlassian.net/browse/PCR-5593
     * 이중 서브밋 방지를 위한 방어코드로 @Transactional 애노테이션에는 synchronized가 동작하기 않기 때문에
     * 별도의 synchronized 메소드 내에서 @Transactional 메소드를 호출한다.
     */
    public synchronized List<AssessmentResponseDto> createAssessments(Long projectId, String inventoryTypeCode, List<Long> inventoryIds) {
        return createAssessmentsInternal(projectId, inventoryTypeCode, inventoryIds);
    }

    /**
     * <pre>
     * 서비스, 서버, 미들웨어, 애플리케이션, 데이터베이스에 대한 Assessment 요청
     * </pre>
     *
     * @param projectId
     * @param inventoryTypeCode
     * @param inventoryIds
     */
    @Transactional
    public List<AssessmentResponseDto> createAssessmentsInternal(Long projectId, String inventoryTypeCode, List<Long> inventoryIds) {
        List<AssessmentResponseDto> assessmentResponseDtoList = new ArrayList<>();
        ServiceMaster serviceMaster = null;
        InventoryMaster inventoryMaster = null;

        // 언어 설정 문제로 지역 변수로 존재해야 함.
        String notFoundMessage = localeMessageConvert.getMessage(RESOURCE_NOT_FOUND.getMessage(), new String[]{""});
        String duplicatedMessage = localeMessageConvert.getMessage(ASSESSMENT_DUPLICATED.getMessage(), new String[]{""});

        // TODO inventoryProcessGroup만 추가되고 inventoryProcess가 추가되지 않는 경우는 inventoryProcessGroup을 남겨둬야할지...
        InventoryProcessGroup inventoryProcessGroup = inventoryProcessService.addInventoryGroup();

        // 주어진 inventoryIds가 inventoryType이 맞는지 확인한다.
        if ("SERV".equals(inventoryTypeCode)) {
            // 입력된 모든 서비스에 대한 하위 인벤토리 ID 목록 (중복 제거용)
            List<Long> globalInventoryIdList = new ArrayList<>();

            for (Long serviceId : new ArrayList<>(inventoryIds)) {
                serviceMaster = serviceMasterRepository.findById(serviceId).orElse(null);

                if (serviceMaster == null) {
                    assessmentResponseDtoList.add(createAssessmentFailResponse(projectId, serviceId, null, inventoryTypeCode, notFoundMessage));
                } else {
                    // 인벤토리 타입이 서비스 인 경우 해당 서비스 내의 모든 리소스 ID 목록을 구한다.
                    List<Long> inventoryIdList = new ArrayList<>();
                    List<ServiceInventory> serviceInventoryList = serviceInventoryRepository.findAllByServiceId(serviceId);

                    AssessmentResponseDto assessmentResponseDto = new AssessmentResponseDto();
                    assessmentResponseDto.setProjectId(projectId);
                    assessmentResponseDto.setInventoryId(serviceId);
                    assessmentResponseDto.setInventoryName(serviceMaster.getServiceName());
                    assessmentResponseDto.setInventoryTypeCode(inventoryTypeCode);
                    assessmentResponseDto.setResult("SUCCESS");

                    for (ServiceInventory serviceInventory : serviceInventoryList) {
                        if (globalInventoryIdList.contains(serviceInventory.getInventoryId())) {
                            inventoryMaster = inventoryMasterRepository.findById(serviceInventory.getInventoryId()).orElse(null);

                            if (inventoryMaster != null) {
                                assessmentResponseDto.getSubInventoryList().add(
                                        createAssessmentFailResponse(
                                                projectId,
                                                serviceInventory.getInventoryId(),
                                                inventoryMaster.getInventoryName(),
                                                inventoryMaster.getInventoryTypeCode(),
                                                duplicatedMessage));
                            }
                        } else {
                            inventoryIdList.add(serviceInventory.getInventoryId());
                            globalInventoryIdList.add(serviceInventory.getInventoryId());
                        }
                    }

                    for (Long inventoryId : inventoryIdList) {
                        assessmentResponseDto.getSubInventoryList().add(createAssessment(inventoryProcessGroup, projectId, inventoryId));
                    }

                    assessmentResponseDtoList.add(assessmentResponseDto);
                }
            }
        } else {
            if (Domain1001.SVR.equals(Domain1001.valueOf(inventoryTypeCode))) {
                for (Long inventoryId : new ArrayList<>(inventoryIds)) {
                    inventoryMaster = inventoryMasterRepository.findByInventoryIdAndInventoryTypeCode(inventoryId, Domain1001.SVR.name()).orElse(null);

                    if (inventoryMaster == null) {
                        assessmentResponseDtoList.add(createAssessmentFailResponse(projectId, inventoryId, null, inventoryTypeCode, notFoundMessage));
                        inventoryIds.remove(inventoryId);
                    }
                }
            } else if (Domain1001.MW.equals(Domain1001.valueOf(inventoryTypeCode))) {
                for (Long inventoryId : new ArrayList<>(inventoryIds)) {
                    inventoryMaster = inventoryMasterRepository.findByInventoryIdAndInventoryTypeCode(inventoryId, Domain1001.MW.name()).orElse(null);

                    if (inventoryMaster == null) {
                        assessmentResponseDtoList.add(createAssessmentFailResponse(projectId, inventoryId, null, inventoryTypeCode, notFoundMessage));
                        inventoryIds.remove(inventoryId);
                    }
                }
            } else if (Domain1001.APP.equals(Domain1001.valueOf(inventoryTypeCode))) {
                for (Long inventoryId : new ArrayList<>(inventoryIds)) {
                    inventoryMaster = inventoryMasterRepository.findByInventoryIdAndInventoryTypeCode(inventoryId, Domain1001.APP.name()).orElse(null);

                    if (inventoryMaster == null) {
                        assessmentResponseDtoList.add(createAssessmentFailResponse(projectId, inventoryId, null, inventoryTypeCode, notFoundMessage));
                        inventoryIds.remove(inventoryId);
                    }
                }
            } else if (Domain1001.DBMS.equals(Domain1001.valueOf(inventoryTypeCode))) {
                for (Long inventoryId : new ArrayList<>(inventoryIds)) {
                    inventoryMaster = inventoryMasterRepository.findByInventoryIdAndInventoryTypeCode(inventoryId, Domain1001.DBMS.name()).orElse(null);

                    if (inventoryMaster == null) {
                        assessmentResponseDtoList.add(createAssessmentFailResponse(projectId, inventoryId, null, inventoryTypeCode, notFoundMessage));
                        inventoryIds.remove(inventoryId);
                    }
                }
            }

            // ID 중복 제거
            inventoryIds = inventoryIds.stream().distinct().collect(Collectors.toList());
            for (Long inventoryId : inventoryIds) {
                assessmentResponseDtoList.add(createAssessment(inventoryProcessGroup, projectId, inventoryId));
            }
        }

        return assessmentResponseDtoList;
    }

    /**
     * <pre>
     * Server, Middleware 등의 Assessment 후 처리로 신규 Inventory가 등록되고 자동 Assessment가 수행되는 경우 기존 inventoryProcessGroup을 함께 넘겨줘야 한다.
     * See also {@link AssessmentService#createAssessment(io.playce.roro.jpa.entity.InventoryProcessGroup, java.lang.Long, java.lang.Long)}
     * </pre>
     *
     * @param projectId
     * @param inventoryId
     */
//    public AssessmentResponseDto createAssessment(Long projectId, Long inventoryId) {
//        return createAssessment(null, projectId, inventoryId);
//    }
    public void createAssessment(Long projectId, Long inventoryId) {
        createAssessment(null, projectId, inventoryId);
    }

    /**
     * @param inventoryProcessGroup
     * @param projectId
     * @param inventoryId
     */
    public AssessmentResponseDto createAssessment(InventoryProcessGroup inventoryProcessGroup, Long projectId, Long inventoryId) {
        AssessmentResponseDto assessmentResponseDto = new AssessmentResponseDto();
        assessmentResponseDto.setProjectId(projectId);
        assessmentResponseDto.setInventoryId(inventoryId);

        // 서비스에 매핑된 리소스 중 이미 삭제된 인벤토리에 대해 scan 요청이 발생할 수 있음. 따라서 orElseThrow()를 하지 않고 orElse(null)로 처리함.
        // 인벤토리 삭제 시 서비스 매핑, 매니저 매핑 등의 정보를 삭제하지 않고 delete_yn 필드만 업데이트 하고 있음.
        InventoryMaster inventoryMaster = inventoryMasterRepository.findByProjectIdAndInventoryId(projectId, inventoryId).orElse(null);

        if (inventoryMaster != null) {
            assessmentResponseDto.setInventoryTypeCode(inventoryMaster.getInventoryTypeCode());
            assessmentResponseDto.setInventoryName(inventoryMaster.getInventoryName());

            io.playce.roro.common.dto.inventory.process.InventoryProcess.Result lastScanProcess = inventoryProcessMapper.selectLastInventoryProcess(inventoryId, Domain1002.SCAN.name());

            // 가장 마지막 상태가 완료, 취소, 실패, NS, PC 상태일 경우에만 추가한다.
            if (lastScanProcess == null ||
                    lastScanProcess.getInventoryProcessResultCode().equals(Domain1003.CMPL.name()) ||
                    lastScanProcess.getInventoryProcessResultCode().equals(Domain1003.CNCL.name()) ||
                    lastScanProcess.getInventoryProcessResultCode().equals(Domain1003.FAIL.name()) ||
                    lastScanProcess.getInventoryProcessResultCode().equals(Domain1003.NS.name()) ||
                    lastScanProcess.getInventoryProcessResultCode().equals(Domain1003.PC.name())) {

                if (inventoryProcessGroup == null) {
                    inventoryProcessGroup = inventoryProcessService.addInventoryGroup();
                }

                inventoryProcessService.addInventoryProcess(inventoryProcessGroup.getInventoryProcessGroupId(), inventoryId, Domain1002.SCAN, Domain1003.REQ);
                assessmentResponseDto.setResult("SUCCESS");
            } else {
                assessmentResponseDto.setResult("FAIL");
                assessmentResponseDto.setMessage(localeMessageConvert.getMessage(ASSESSMENT_DUPLICATED.getMessage()));
            }
        } else {
            assessmentResponseDto.setResult("FAIL");
            assessmentResponseDto.setMessage(localeMessageConvert.getMessage(RESOURCE_NOT_FOUND.getMessage(), new String[]{""}));
        }

        return assessmentResponseDto;
    }

    public InventoryProcessListResponse getAssessment(long projectId, Long inventoryId, PageAssessmentRequestDto assessmentRequestDto) {
        InventoryProcessListResponse result = new InventoryProcessListResponse();
        try {
            List<InventoryProcessListResponse.Content> contents =
                    inventoryProcessMapper.selectInventoryProcessList(projectId, inventoryId, assessmentRequestDto, Domain1002.SCAN.name());
            long totalSize = inventoryProcessMapper.selectInventoryProcessCount(projectId, inventoryId, assessmentRequestDto, Domain1002.SCAN.name());

            // Do not use..
            // InventoryProcessListResponse.Summary summary = inventoryProcessMapper.selectInventoryProcessSummary(projectId, inventoryId, Domain1002.SCAN.name());

            InventoryProcessListResponse.Data data = new InventoryProcessListResponse.Data();
            data.setContents(contents);
            data.setTotalCount(totalSize);

            // result.setSummary(summary);
            result.setData(data);
        } catch (Exception e) {
            log.error("Unhandled error occured with " + e.getMessage());
        }
        return result;
    }

    /**
     * @param projectId
     * @param inventoryProcessId
     *
     * @return
     */
    public InventoryProcessDetailResponse getAssessment(Long projectId, Long inventoryProcessId) {
        InventoryProcessDetailResponse response = inventoryProcessMapper.getInventoryProcessDetail(projectId, inventoryProcessId);

        if (response == null) {
            throw new RoRoApiException(ErrorCode.ASSESSMENT_NOT_FOUND);
        } else {
            response.setJsonMetas(JsonUtil.readTree(response.getInventoryProcessResultMetaList()));
        }
        return response;
    }

    public JsonNode getAssessmentDetail(Long inventoryProcessId, String path) {
        InventoryProcessJsonDetail detail = inventoryProcessJsonDetailRepository.getByInventoryProcessIdAndJsonKey(inventoryProcessId, path);
        return detail == null ? NullNode.getInstance() : JsonUtil.readTree(detail.getJsonContent());
    }

    /**
     * <pre>
     * 취소, 실패, 지원되지 않음 상태의 Assessment만 삭제할 수 있다.
     * </pre>
     *
     * @param projectId
     * @param inventoryProcessId
     */
    @Transactional
    public void removeAssessment(Long projectId, Long inventoryProcessId) {
        InventoryProcess inventoryProcess = inventoryProcessRepository.findById(inventoryProcessId)
                .orElseThrow(() -> new RoRoApiException(ErrorCode.ASSESSMENT_NOT_FOUND));

        // 인벤토리 존재 여부와 관계없이 Scan 이력은 삭제할 수 있음. (인벤토리를 먼저 삭제하면 실패한 Scan 이력은 삭제할 수 없음)
        // inventoryMasterRepository.findByProjectIdAndInventoryId(projectId, inventoryProcess.getInventoryId())
        //         .orElseThrow(() -> new RoRoApiException(ErrorCode.RESOURCE_NOT_FOUND, "Inventory does not exist."));

        if (!inventoryProcess.getInventoryProcessResultCode().equals(Domain1003.CNCL.name()) &&
                !inventoryProcess.getInventoryProcessResultCode().equals(Domain1003.FAIL.name()) &&
                !inventoryProcess.getInventoryProcessResultCode().equals(Domain1003.NS.name())) {
            throw new RoRoApiException(ErrorCode.ASSESSMENT_DELETE_INVALID_STATUS);
        } else {
            inventoryProcess.setDeleteYn(YES);
            inventoryProcess.setModifyUserId(WebUtil.getUserId());
            inventoryProcess.setModifyDatetime(new Date());
            inventoryProcessRepository.save(inventoryProcess);
        }
    }

    /**
     * <pre>
     * 완료, 취소, 실패, 진행중 상태의 Assessment는 취소할 수 없다.
     * </pre>
     */
    @Transactional
    public void stopAssessment(long projectId, Long inventoryProcessId) {
        InventoryProcess inventoryProcess = inventoryProcessRepository.findById(inventoryProcessId)
                .orElseThrow(() -> new RoRoApiException(ErrorCode.ASSESSMENT_NOT_FOUND));

        inventoryMasterRepository.findByProjectIdAndInventoryId(projectId, inventoryProcess.getInventoryId())
                .orElseThrow(() -> new RoRoApiException(ErrorCode.RESOURCE_NOT_FOUND, "Inventory does not exist."));

        if (inventoryProcess.getInventoryProcessResultCode().equals(Domain1003.CMPL.name()) ||
                inventoryProcess.getInventoryProcessResultCode().equals(Domain1003.CNCL.name()) ||
                inventoryProcess.getInventoryProcessResultCode().equals(Domain1003.FAIL.name()) ||
                inventoryProcess.getInventoryProcessResultCode().equals(Domain1003.NS.name()) ||
                inventoryProcess.getInventoryProcessResultCode().equals(Domain1003.PC.name())) {
            log.debug("cancel failed status: {}, inventoryProcess: {}", inventoryProcess.getInventoryProcessResultCode(), inventoryProcess);
            throw new RoRoApiException(ErrorCode.ASSESSMENT_CANCEL_INVALID_STATUS);
        } else {
            InventoryProcessCancelInfo.addCancelRequest(inventoryProcessId, System.currentTimeMillis());

            if (inventoryProcess.getInventoryProcessResultCode().equals(Domain1003.PROC.name())) {
                boolean canceled = inventoryProcessCancelProcessor.jobCancel(Domain1002.valueOf(inventoryProcess.getInventoryProcessTypeCode()).executeKey(inventoryProcessId));
                if (canceled) {
                    saveInventoryProcessCancel(inventoryProcess);
                }
            } else {
                saveInventoryProcessCancel(inventoryProcess);
            }
        }
    }

    private void saveInventoryProcessCancel(InventoryProcess inventoryProcess) {
        inventoryProcess.setInventoryProcessResultCode(Domain1003.CNCL.name());
        inventoryProcess.setModifyUserId(WebUtil.getUserId());
        inventoryProcess.setModifyDatetime(new Date());
        inventoryProcessRepository.save(inventoryProcess);
    }

    private AssessmentResponseDto createAssessmentFailResponse(Long projectId, Long inventoryId, String inventoryName, String inventoryTypeCode, String message) {
        AssessmentResponseDto assessmentResponseDto = new AssessmentResponseDto();
        assessmentResponseDto.setProjectId(projectId);
        assessmentResponseDto.setInventoryId(inventoryId);
        assessmentResponseDto.setInventoryName(inventoryName);
        assessmentResponseDto.setInventoryTypeCode(inventoryTypeCode);
        assessmentResponseDto.setResult("FAIL");
        assessmentResponseDto.setMessage(message);

        return assessmentResponseDto;
    }

    public ByteArrayInputStream getAssessmentCsvDownload(Long projectId, PageAssessmentRequestDto pageAssessmentRequestDto) {

        pageAssessmentRequestDto.setExcelDownload(true);

        InventoryProcessListResponse response = getAssessment(projectId, null, pageAssessmentRequestDto);
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            CSVPrinter csvPrinter = new CSVPrinter(new PrintWriter(out), CSVFormat.DEFAULT);

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");

            String[] header = {
                    "Job ID", "Resource Type", "Resource Name", "Resource IP", "Scanned Date",
                    "Scanned By", "Assessment Status", "Status Message", "Assessment Data"};

            csvPrinter.printRecord(header);

            for (InventoryProcessListResponse.Content content : response.getData().getContents()) {
                List<String> data = Arrays.asList(
                        content.getInventoryProcessId() != null ? String.valueOf(content.getInventoryProcessId()) : "",
                        StringUtils.defaultString(Domain1001.valueOf(content.getInventoryTypeCode()).fullname()),
                        StringUtils.defaultString(content.getInventoryName()),
                        StringUtils.defaultString(content.getRepresentativeIpAddress()),
                        StringUtils.defaultString(content.getInventoryProcessStartDatetime() != null ?
                                format.format(content.getInventoryProcessStartDatetime()) : ""),
                        StringUtils.defaultString(content.getRegistUserLoginId()),
                        StringUtils.defaultString(Domain1003.valueOf(content.getInventoryProcessResultCode()).fullname()),
                        StringUtils.defaultString(content.getInventoryProcessResultTxt()),
                        StringUtils.defaultString(makeLink(content))
                );

                csvPrinter.printRecord(data);
            }
            csvPrinter.flush();
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Unhandled error occured with " + e.getMessage());
        }
    }

    private String makeLink(InventoryProcessListResponse.Content content) {
        if (Domain1003.CMPL.name().equals(content.getInventoryProcessResultCode()) ||
                Domain1003.PC.name().equals(content.getInventoryProcessResultCode())) {
            String baseUrl = WebUtil.getBaseUrl();

            return baseUrl + "/console/projects/" + content.getProjectId()
                    + "/popup/assessment-data/" + Domain1001.valueOf(content.getInventoryTypeCode()).fullname().toLowerCase()
                    + "/" + content.getInventoryProcessId();
        } else {
            return null;
        }
    }

    public List<JavaProcessResponse> getJavaProcessList(Long projectId, Long assessmentId) {
        List<JavaProcessResponse> javaProcessResponseList = new ArrayList<>();
        JavaProcessResponse javaProcessResponse;

        InventoryProcessDetailResponse response = getAssessment(projectId, assessmentId);

        if (response == null ||
                !response.getInventoryProcessTypeCode().equals(Domain1002.SCAN.name()) ||
                !response.getInventoryTypeCode().equals(Domain1001.SVR.name()) ||
                response.getInventoryProcessResultJson() == null) {
            throw new RoRoApiException(ErrorCode.ASSESSMENT_NOT_FOUND);
        }

        ServerMaster serverMaster = serverMasterRepository.findById(response.getInventoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Server ID : " + response.getInventoryId() + " Not Found."));


        boolean isWindow = false;
        if (Domain101.Y.name().equals(serverMaster.getWindowsYn()) || Domain1013.WINDOWS.name().equals(response.getInventoryDetailTypeCode())) {
            isWindow = true;
        }

        JsonNode rootNode = JsonUtil.readTree(response.getInventoryProcessResultJson());

        JsonNode processNodes;
        if (isWindow) {
            processNodes = rootNode.get("process");

            if (processNodes != null) {
                Iterator<JsonNode> processes = processNodes.elements();

                while (processes.hasNext()) {
                    JsonNode processNode = processes.next();
                    String name = processNode.get("path").asText();
                    name = name.replaceAll("\"", StringUtils.EMPTY);

                    if (StringUtils.isEmpty(name)) {
                        name = processNode.get("processName").asText();
                        name = name.replaceAll("\"", StringUtils.EMPTY);
                    }

                    if (name.endsWith("java") || name.endsWith("javaw") || name.endsWith("java.exe") || name.endsWith("javaw.exe")) {
                        JsonNode cmdNode = processNode.get("commandLine");
                        String cmd = cmdNode.asText();
                        cmd = cmd.trim().replaceAll(" +", StringUtils.SPACE).replaceAll("\"", StringUtils.EMPTY);

                        javaProcessResponse = new JavaProcessResponse();
                        javaProcessResponse.setProcessName(name);
                        javaProcessResponse.setCommand(cmd);
                        javaProcessResponse.setCommands(Arrays.asList(cmd.split(StringUtils.SPACE)));

                        javaProcessResponseList.add(javaProcessResponse);
                    }
                }
            }
        } else {
            processNodes = rootNode.get("processes");
            if (processNodes != null) {
                Iterator<JsonNode> processes = processNodes.elements();

                while (processes.hasNext()) {
                    JsonNode processNode = processes.next();
                    String name = processNode.get("name").asText();

                    if (name.endsWith("java")) {
                        JsonNode cmdNode = processNode.get("cmd");

                        List<String> cmdList = new ArrayList<>();
                        Iterator<JsonNode> cmds = cmdNode.elements();
                        while (cmds.hasNext()) {
                            cmdList.add(cmds.next().asText());
                        }

                        javaProcessResponse = new JavaProcessResponse();
                        javaProcessResponse.setProcessName(name);
                        javaProcessResponse.setCommand(String.join(StringUtils.SPACE, cmdList));
                        javaProcessResponse.setCommands(cmdList);

                        javaProcessResponseList.add(javaProcessResponse);
                    }
                }
            }
        }

        // ServerAssessmentResult result = gson.fromJson(response.getInventoryProcessResultJson(), ServerAssessmentResult.class);
        //
        // if (result != null) {
        //     if (result.getProcesses() != null) {
        //         for (Process process : result.getProcesses()) {
        //             if (process.getName().equals("java")) {
        //                 javaProcessResponse = new JavaProcessResponse();
        //                 javaProcessResponse.setProcessName(process.getName());
        //                 javaProcessResponse.setCommand(String.join(StringUtils.SPACE, process.getCmd()));
        //                 javaProcessResponse.setCommands(process.getCmd());
        //
        //                 javaProcessResponseList.add(javaProcessResponse);
        //             }
        //         }
        //     }
        // }

        return javaProcessResponseList;
    }
}
//end of AssessmentService.java