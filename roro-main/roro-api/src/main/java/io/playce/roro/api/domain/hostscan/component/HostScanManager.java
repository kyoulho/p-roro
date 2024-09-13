package io.playce.roro.api.domain.hostscan.component;

import io.playce.roro.api.common.error.exception.ResourceNotFoundException;
import io.playce.roro.api.websocket.manager.WebSocketManager;
import io.playce.roro.common.code.Domain1002;
import io.playce.roro.common.code.Domain1003;
import io.playce.roro.common.dto.websocket.HostScanMessage;
import io.playce.roro.host.scan.HostScanner;
import io.playce.roro.host.scan.dto.ScanResult;
import io.playce.roro.jpa.entity.DiscoveredHost;
import io.playce.roro.jpa.entity.HostScanHistory;
import io.playce.roro.jpa.repository.DiscoveredHostRepository;
import io.playce.roro.jpa.repository.HostScanHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class HostScanManager {
    private final HostScanHistoryRepository hostScanHistoryRepository;
    private final DiscoveredHostRepository discoveredHostRepository;
    private final HostScanner hostScanner;
    private final WebSocketManager webSocketManager;

    @Async("hostScanTaskExecutor")
    public void scanAddressesAndSendToUser(Long projectId, String cidr, List<String> addresses) {
        // 스캔 결과 개수
        AtomicInteger discoveredHostCount = new AtomicInteger();
        // 스캔 내역 저장
        Long scanHistoryId = createHostScanHistory(projectId, cidr).getScanHistoryId();

        log.info("Start scanning");

        // 스캔 요청
        List<CompletableFuture<ScanResult>> futures = addresses.stream()
                .map(hostScanner::scanIpAddress)
                .collect(Collectors.toList());

        // 스캔 결과 처리
        futures.stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .forEach(scanResult -> {
                    createDiscoveredHost(scanHistoryId, scanResult);
                    discoveredHostCount.getAndIncrement();
                });

        // 스캔 내역 업데이트
        HostScanHistory hostScanHistory = updateHostScanHistory(projectId, scanHistoryId, discoveredHostCount.get());
        log.info("Finish Scanning");

        // 완료 메세지 전달 요청
        sendNotification(projectId, cidr, scanHistoryId, hostScanHistory);
    }

    private void sendNotification(Long projectId, String cidr, Long scanHistoryId, HostScanHistory hostScanHistory) {
        HostScanMessage message = new HostScanMessage();
        message.setProjectId(projectId);
        message.setScanHistoryId(scanHistoryId);
        message.setType(Domain1002.RANGE);
        message.setStatus(Domain1003.CMPL);
        message.setCidr(cidr);
        message.setStartDate(hostScanHistory.getScanStartDatetime());
        message.setEndDate(hostScanHistory.getScanEndDatetime());
        webSocketManager.sendNotification(message);
    }

    private HostScanHistory createHostScanHistory(Long projectId, String cidr) {
        return hostScanHistoryRepository.save(HostScanHistory.builder()
                .projectId(projectId)
                .cidr(cidr)
                .scanStartDateTime(new Date())
                .build());
    }

    private HostScanHistory updateHostScanHistory(Long projectId, Long scanHistoryId, int discoveredHostCount) {
        HostScanHistory hostScanHistory = hostScanHistoryRepository.findByProjectIdAndScanHistoryId(projectId, scanHistoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Project ID(" + projectId + ") HostScanHistory ID(" + scanHistoryId + ") Not Found"));
        hostScanHistory.complete(new Date(), discoveredHostCount);
        return hostScanHistoryRepository.save(hostScanHistory);
    }

    private void createDiscoveredHost(Long scanHistoryId, ScanResult scanResult) {
        discoveredHostRepository.save(DiscoveredHost.builder()
                .scanHistoryId(scanHistoryId)
                .ipAddress(scanResult.getIpAddress())
                .replyTTl(scanResult.getReplyTTL())
                .osName(scanResult.getOsName())
                .discoveredDatetime(scanResult.getDiscoveredDatetime())
                .build());
    }

    // 앱 종료 시
    @PreDestroy
    public void forceComplete() {
        hostScanHistoryRepository.findFirstByOrderByScanHistoryIdDesc()
                .ifPresent(hostScanHistory -> {
                    if (hostScanHistory.getCompletedYn().equals("N")) {
                        int count = discoveredHostRepository.countByDiscoveredHostIdScanHistoryId(hostScanHistory.getScanHistoryId());
                        hostScanHistory.complete(new Date(), count);
                    }
                });
    }


}
