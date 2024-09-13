/*
 * Copyright 2022 The Playce-RoRo Project.
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
 * Author            Date                Description
 * ---------------  ----------------    ------------
 * Hoon Oh       2월 24, 2022            First Draft.
 */
package io.playce.roro.api.domain.inventory.service;

import io.playce.roro.api.common.error.ErrorCode;
import io.playce.roro.api.common.error.exception.ResourceNotFoundException;
import io.playce.roro.api.common.error.exception.RoRoApiException;
import io.playce.roro.common.code.Domain1001;
import io.playce.roro.common.dto.discovered.*;
import io.playce.roro.common.dto.info.JdbcInfo;
import io.playce.roro.common.dto.inventory.service.ServiceResponse;
import io.playce.roro.common.util.JdbcURLParser;
import io.playce.roro.jpa.entity.DiscoveredInstanceMaster;
import io.playce.roro.jpa.repository.DiscoveredInstanceMasterRepository;
import io.playce.roro.jpa.repository.ProjectMasterRepository;
import io.playce.roro.mybatis.domain.discovered.DiscoveredInstanceMapper;
import io.playce.roro.mybatis.domain.inventory.service.ServiceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

;

/**
 * <pre>
 *
 *
 * </pre>
 *
 * @author Hoon Oh
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DiscoveredResourceService {

    private final ProjectMasterRepository projectMasterRepository;
    private final ServiceMapper serviceMapper;
    private final DiscoveredInstanceMasterRepository discoveredInstanceMasterRepository;
    private final DiscoveredInstanceMapper discoveredInventoryMasterMapper;

    public DiscoveredServerListResponse getDiscoveredServerList(Long projectId, PageDiscoveredRequestDto pageRequestDto) {

        DiscoveredServerListResponse result = new DiscoveredServerListResponse();
        try {

            if (pageRequestDto.getPort().contains("-")) {
                String[] range = pageRequestDto.getPort().split("-");
                pageRequestDto.setStartPort(Long.valueOf(range[0]));
                pageRequestDto.setEndPort(Long.valueOf(range[1]));
            } else {
                pageRequestDto.setStartPort(Long.valueOf(pageRequestDto.getPort()));
            }
            List<DiscoveredServerListResponse.Content> contents =
                    discoveredInventoryMasterMapper.selectDiscoveredServer(projectId, pageRequestDto);
            long totalSize = discoveredInventoryMasterMapper.selectDiscoveredServerCount(projectId, pageRequestDto);

            DiscoveredServerListResponse.Data data = new DiscoveredServerListResponse.Data();
            data.setContents(contents);
            data.setTotalCount(totalSize);

            result.setData(data);
        } catch (Exception e) {
            log.error("{}", e.getMessage(), e);
        }

        return result;
    }

    public DiscoveredServerDetailResponse getDiscoveredServerDetail(Long projectId, Long discoveredInstanceId) {
        DiscoveredInstanceMaster discoveredInstanceMaster = discoveredInstanceMasterRepository.findByProjectIdAndDiscoveredInstanceId(projectId, discoveredInstanceId);

        if (discoveredInstanceMaster == null || !Domain1001.SVR.name().equals(discoveredInstanceMaster.getInventoryTypeCode())) {
            throw new RoRoApiException(ErrorCode.RESOURCE_NOT_FOUND, "Discovered server does not exist.");
        }

        DiscoveredServerDetailResponse response = new DiscoveredServerDetailResponse();
        response.setProjectId(discoveredInstanceMaster.getProjectId());
        response.setDiscoveredInstanceId(discoveredInstanceMaster.getDiscoveredInstanceId());
        response.setDiscoveredIpAddress(discoveredInstanceMaster.getDiscoveredIpAddress());
        response.setRegistDatetime(discoveredInstanceMaster.getRegistDatetime());

        return response;
    }

    public List<DiscoveredDatabaseListResponse> getDiscoveredDatabaseList(Long projectId) throws InterruptedException {
        List<DiscoveredDatabaseListResponse> discoveredDatabaseList = discoveredInventoryMasterMapper.selectDiscoveredDatabaseList(projectId);

        if (CollectionUtils.isNotEmpty(discoveredDatabaseList)) {
            for (DiscoveredDatabaseListResponse res : discoveredDatabaseList) {

                List<String> connectionPort = new ArrayList<>();
                List<JdbcInfo> parse = JdbcURLParser.parse(StringUtils.defaultString(res.getJdbcUrl()));
                for (JdbcInfo info : parse) {
                    if (info.getPort() != null) {
                        connectionPort.add(info.getPort().toString());
                    }
                }
                res.setDiscoveredServicePort(StringUtils.join(connectionPort, ","));

                List<ServiceResponse> serviceList = serviceMapper.selectServiceByInventoryId(res.getFinderInventoryId());
                if (CollectionUtils.isNotEmpty(serviceList)) {
                    res.setServiceIds(serviceList.stream().map(ServiceResponse::getServiceId).collect(Collectors.toList()));
                }
            }
        }

        return discoveredDatabaseList;
    }

    public DiscoveredDatabaseDetailResponse getDiscoveredDatabaseDetail(Long projectId, Long discoveredInstanceId) {
        DiscoveredDatabaseDetailResponse response = discoveredInventoryMasterMapper.selectDiscoveredDatabaseDetail(projectId, discoveredInstanceId);

        if (response == null) {
            throw new RoRoApiException(ErrorCode.RESOURCE_NOT_FOUND, "Discovered database does not exist.");
        }

        try {
            if (StringUtils.isNotEmpty(response.getDiscoveredDetailDivision()) && response.getDiscoveredDetailDivision().contains("|")) {
                response.setDiscoveredServicePort(Integer.valueOf(response.getDiscoveredDetailDivision().split("\\|")[0]));
            }

//            Domain1013 engineName = Domain1013.valueOf(response.getInventoryDetailTypeCode());
//            if (engineName != null) {
//                response.setInventoryDetailTypeCode(engineName.enname());
//            }
        } catch (IllegalArgumentException e) {
            // ignore
        }

        return response;
    }

    public ByteArrayInputStream getDiscoveredDatabaseExcel(Long projectId) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            XSSFWorkbook workbook = new XSSFWorkbook(DiscoveredResourceService.class.getResourceAsStream("/template/RoRo-Inventory-Template.xlsx"));

            // add discovered database
            addDiscoveredDatabases(projectId, workbook);

            workbook.write(out);
        } catch (Exception e) {
            log.error("Unhandled Exception occurred while Export to Excel : " + e);
            e.printStackTrace();
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    public ByteArrayInputStream getDiscoveredServerExcel(Long projectId) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            XSSFWorkbook workbook = new XSSFWorkbook(DiscoveredResourceService.class.getResourceAsStream("/template/RoRo-Inventory-Template.xlsx"));

            // add discovered database
            addDiscoveredServers(projectId, workbook);

            workbook.write(out);
        } catch (Exception e) {
            log.error("Unhandled Exception occurred while Export to Excel : " + e);
            e.printStackTrace();
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    /**
     * Upload Inventory 템플릿에 Discovered Server 정보 중 IP Address 를 입력한다.
     */
    private void addDiscoveredServers(Long projectId, XSSFWorkbook workbook) {
        projectMasterRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project ID : " + projectId + " Not Found."));

        /*
        // 페이징에 상관없이 전체를 조회 하여 inventory_upload_template에 입력 후 엑셀 파일로 내려준다.
        List<DiscoveredServerListExcelResponse> discoveredServerList
                = discoveredInventoryMasterMapper.selectDiscoveredServerWithoutPaging(projectId);

        Sheet workSheet = workbook.getSheet("server");
        if (CollectionUtils.isNotEmpty(discoveredServerList)) {

            List<String> ipList = new ArrayList<>();
            for (DiscoveredServerListExcelResponse svr : discoveredServerList) {
                // 중복 체크
                if (!ipList.contains(svr.getDiscoveredIpAddress())) {
                    Row row = workSheet.createRow(workSheet.getLastRowNum() + 1);
                    row.createCell(3).setCellValue(StringUtils.defaultString(svr.getDiscoveredIpAddress()));

                    ipList.add(svr.getDiscoveredIpAddress());
                }
            }
        }
        */
        List<String> discoveredServerIPList = discoveredInventoryMasterMapper.selectDiscoveredServerIPList(projectId);

        Sheet workSheet = workbook.getSheet("server");
        if (CollectionUtils.isNotEmpty(discoveredServerIPList)) {
            // 중복 제거
            discoveredServerIPList = discoveredServerIPList.stream().distinct().collect(Collectors.toList());

            for (String ipAddress : discoveredServerIPList) {
                Row row = workSheet.createRow(workSheet.getLastRowNum() + 1);
                row.createCell(4).setCellValue(ipAddress);
            }
        }
    }

    /**
     * Upload Inventory 템플릿에 Discovered Database 정보를 입력한다.
     */
    private void addDiscoveredDatabases(Long projectId, XSSFWorkbook workbook) throws InterruptedException {
        projectMasterRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project ID : " + projectId + " Not Found."));

        List<DiscoveredDatabaseListResponse> discoveredDatabaseList
                = discoveredInventoryMasterMapper.selectDiscoveredDatabaseList(projectId);

        Sheet workSheet = workbook.getSheet("database");
        if (CollectionUtils.isNotEmpty(discoveredDatabaseList)) {
            for (DiscoveredDatabaseListResponse data : discoveredDatabaseList) {

                List<String> connectionPort = new ArrayList<>();
                List<JdbcInfo> parse = JdbcURLParser.parse(StringUtils.defaultString(data.getJdbcUrl()));
                for (JdbcInfo info : parse) {
                    if (info.getPort() != null) {
                        connectionPort.add(info.getPort().toString());
                    }
                }
                data.setDiscoveredServicePort(StringUtils.join(connectionPort, ","));

                if (StringUtils.isNotEmpty(StringUtils.defaultString(data.getDatabaseServiceName())) ||
                        StringUtils.isNotEmpty(StringUtils.defaultString(data.getDiscoveredServicePort())) ||
                        StringUtils.isNotEmpty(StringUtils.defaultString(data.getUserName())) ||
                        StringUtils.isNotEmpty(StringUtils.defaultString(data.getJdbcUrl()))) {
                    Row row = workSheet.createRow(workSheet.getLastRowNum() + 1);
                    row.createCell(7).setCellValue(StringUtils.defaultString(data.getDiscoveredServicePort()));
                    row.createCell(8).setCellValue(StringUtils.defaultString(data.getDatabaseServiceName()));
                    row.createCell(9).setCellValue(StringUtils.defaultString(data.getJdbcUrl()));
                    row.createCell(10).setCellValue(StringUtils.defaultString(data.getUserName()));
                }
            }
        }
    }

    public ByteArrayInputStream getDiscoveredSvrCsvDownload(Long projectId, PageDiscoveredRequestDto pageDiscoveredRequestDto) {
        pageDiscoveredRequestDto.setExcelDownload(true);

        DiscoveredServerListResponse response = getDiscoveredServerList(projectId, pageDiscoveredRequestDto);
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            CSVPrinter csvPrinter = new CSVPrinter(new PrintWriter(out), CSVFormat.DEFAULT);

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");

            String[] header = {
                    "Target ID", "Target Name", "Target IP", "Port", "Protocol",
                    "Resource Type", "Resource ID", "Resource Name", "Resource Sub-Type",
                    "Service(Solution) Name", "Source ID", "Source Name", "Source IP", "Discovered Date"};

            csvPrinter.printRecord(header);

            for (DiscoveredServerListResponse.Content content : response.getData().getContents()) {
                List<String> data = Arrays.asList(
                        content.getTargetId() != null ? String.valueOf(content.getTargetId()) : "",
                        StringUtils.defaultString(content.getTargetName()),
                        StringUtils.defaultString(content.getTargetIp()),
                        StringUtils.defaultString(String.valueOf(content.getPort())),
                        StringUtils.defaultString(content.getProtocol()),
                        StringUtils.defaultString(content.getResourceType()),
                        content.getResourceId() != null ? String.valueOf(content.getResourceId()) : "",
                        StringUtils.defaultString(content.getResourceName()),
                        StringUtils.defaultString(content.getResourceSubType()),
                        StringUtils.defaultString(content.getServiceName()),
                        content.getSourceId() != null ? String.valueOf(content.getSourceId()) : "",
                        StringUtils.defaultString(content.getSourceName()),
                        StringUtils.defaultString(content.getSourceIp()),
                        StringUtils.defaultString(format.format(content.getDiscoveredDate()))
                );

                csvPrinter.printRecord(data);
            }
            csvPrinter.flush();
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Unhandled error occured with " + e.getMessage());
        }
    }
}
//end of DiscoveredResourceService.java