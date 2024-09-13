package io.playce.roro.api.domain.hostscan.service;

import io.playce.roro.api.common.error.ErrorCode;
import io.playce.roro.api.common.error.exception.ResourceNotFoundException;
import io.playce.roro.api.common.error.exception.RoRoApiException;
import io.playce.roro.api.domain.hostscan.component.HostScanManager;
import io.playce.roro.common.dto.hostscan.DiscoveredHostDto;
import io.playce.roro.common.dto.hostscan.HostScanHistoryDto;
import io.playce.roro.jpa.entity.HostScanHistory;
import io.playce.roro.jpa.repository.HostScanHistoryRepository;
import io.playce.roro.mybatis.domain.hostscan.HostScanMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.util.SubnetUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

@Transactional
@Slf4j
@Service
@RequiredArgsConstructor
public class HostScanService {
    private final HostScanManager hostScanManager;
    private final HostScanHistoryRepository hostScanHistoryRepository;
    private final HostScanMapper hostScanMapper;

    public void scan(Long projectId, String cidr) {
        if (hasIncompleteScan()) {
            throw new RoRoApiException(ErrorCode.HOST_SCAN_INCOMPLETE_SCAN);
        }
        SubnetUtils subnetUtils = new SubnetUtils(cidr);
        // B 클래스 이상의 네트워크는 거절
        if (!isBelowBClass(subnetUtils.getInfo().getNetmask())) {
            throw new RoRoApiException(ErrorCode.HOST_SCAN_BELOW_BCLASS);
        }
        // 스캔 범위에 Network Ip ,Broadcast Ip 추가
        subnetUtils.setInclusiveHostCount(true);
        // 스캔 하여야 할 범위
        String[] addresses = subnetUtils.getInfo().getAllAddresses();
        // 스캔 요청
        hostScanManager.scanAddressesAndSendToUser(projectId, cidr, List.of(addresses));
    }

    // 스캔 내역 조회
    public List<HostScanHistoryDto> getHostScanHistories(Long projectId) {
        // 스캔 요청 날짜 역순 정렬
        List<HostScanHistory> hostScanHistories = hostScanHistoryRepository.findByProjectIdOrderByScanStartDatetimeDesc(projectId);
        return hostScanHistories.stream()
                .map(HostScanHistory::toDto)
                .collect(Collectors.toList());
    }

    // 발견된 host 조회
    public List<DiscoveredHostDto> getDiscoveredHosts(Long projectId, Long scanHistoryId) {
        hostScanHistoryRepository.findByProjectIdAndScanHistoryId(projectId, scanHistoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Project ID(" + projectId + ") HostScanHistory ID(" + scanHistoryId + ") Not Found"));


        List<DiscoveredHostDto> discoveredHostDtos = hostScanMapper.selectDiscoveredHostAndRegisteredServers(projectId, scanHistoryId);
        // Ip Address 로 정렬
        discoveredHostDtos.sort(DiscoveredHostDto::compareTo);
        return discoveredHostDtos;
    }

    // 스캔 내역 건별 삭제
    public void removeHostScanHistory(Long projectId, Long scanHistoryId) {
        // 스캔 기록 조회 및 변경
        HostScanHistory hostScanHistory = hostScanHistoryRepository.findByProjectIdAndScanHistoryId(projectId, scanHistoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Project ID(" + projectId + ") HostScanHistory ID(" + scanHistoryId + ") Not Found"));

        if (hostScanHistory.getCompletedYn().equals("N")) {
            throw new RoRoApiException(ErrorCode.HOST_SCAN_DELETE_FAIL);
        }
        hostScanHistory.delete();
    }

    // 스캔 내역 전체 삭제
    public void removeHostScanHistories(Long projectId) {
        List<HostScanHistory> histories = hostScanHistoryRepository.findByProjectId(projectId);

        if (histories.stream().anyMatch(hostScanHistory -> hostScanHistory.getCompletedYn().equals("N"))) {
            throw new RoRoApiException(ErrorCode.HOST_SCAN_DELETE_FAIL);
        } else {
            histories.forEach(HostScanHistory::delete);
        }

    }

    // 스캔 내역 Excel Export
    public ByteArrayInputStream getDiscoveredHostExcel(Long projectId, Long scanHistoryId) {
        hostScanHistoryRepository.findByProjectIdAndScanHistoryId(projectId, scanHistoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Project ID(" + projectId + ") HostScanHistory ID(" + scanHistoryId + ") Not Found"));

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XSSFWorkbook workbook = new XSSFWorkbook(Objects.requireNonNull(
                    // todo 파일명 하드코딩
                    getClass().getResourceAsStream("/template/RoRo-Inventory-Template.xlsx")));

            // IpAddress ,windowsYn 를 Excel 파일에 작성
            writeToWorkbook(scanHistoryId, workbook);
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            log.error("Unhandled Exception occurred while Export to Excel : " + e);
            throw new RuntimeException(e.getMessage());
        }
    }

    // B 클래스 이하인지 여부
    private boolean isBelowBClass(String netmask) {
        StringTokenizer st = new StringTokenizer(netmask, ".");

        for (int i = 0; i < 2; i++) {
            int octet = Integer.parseInt(st.nextToken());
            if (octet < 255) {
                return false;
            }
        }
        return true;
    }

    private void writeToWorkbook(Long scanHistoryId, XSSFWorkbook workbook) {
        Sheet workSheet = workbook.getSheet("server");
        List<DiscoveredHostDto> ipAddressAndOsName = hostScanMapper.selectIpAddressAndOsName(scanHistoryId);

        ipAddressAndOsName.stream()
                .sorted(DiscoveredHostDto::compareTo)
                .forEach(dto -> {
                    Row row = workSheet.createRow(workSheet.getLastRowNum() + 1);
                    // todo 컬럼 하드코딩
                    row.createCell(3).setCellValue(dto.getIpAddress());
                    row.createCell(7).setCellValue(dto.getOsName().equals("windows") ? "Y" : "N");
                });
    }

    // 이전 스캔이 완료 되었는지 확인한다.
    private boolean hasIncompleteScan() {
        Optional<HostScanHistory> optional = hostScanHistoryRepository.findFirstByOrderByScanHistoryIdDesc();
        return optional.map(hostScanHistory -> hostScanHistory.getCompletedYn().equals("N")).orElse(false);
    }
}



