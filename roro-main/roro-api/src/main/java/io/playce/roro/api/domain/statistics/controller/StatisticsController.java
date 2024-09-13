package io.playce.roro.api.domain.statistics.controller;

import io.playce.roro.api.domain.statistics.service.StatisticsService;
import io.playce.roro.common.code.Domain1001;
import io.playce.roro.common.dto.statistics.*;
import io.playce.roro.common.dto.trackinginfo.TrackingInfoWidgetDto;
import io.playce.roro.api.domain.tracking.TrackingInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(value = "/api/projects/{projectId}/statistics")
@Slf4j
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class StatisticsController {

    private final StatisticsService statisticsService;
    private final TrackingInfoService trackingInfoService;

    private final String CPU_UTIL = "cpu-util";
    private final String MEM_UTIL = "mem-util";
    private final String MEM_USAGE = "mem-usage";
    private final String DISK_UTIL = "disk-util";

    @Operation(summary = "Dashboard Overview 조회", description = "Dashboard Overview 조회한다.")
    @ApiResponse(responseCode = "200")
    @GetMapping("/summary")
    public ResponseEntity<?> getOverviewSummary(@PathVariable Long projectId,
                                                @RequestParam(name = "serviceIds") String serviceIds) {
        OverviewSummaryResponse overviewSummaryResponse = statisticsService.getOverviewSummary(projectId, serviceIds);

        return new ResponseEntity<>(overviewSummaryResponse, HttpStatus.OK);
    }

    @Operation(summary = "서비스당 서버 개수 조회", description = "서비스당 서버 개수를 조회한다.")
    @ApiResponse(responseCode = "200")
    @GetMapping("/server-count")
    public ResponseEntity<?> getServerCountPerService(@PathVariable Long projectId,
                                                      @RequestParam(name = "serviceIds") String serviceIds,
                                                      @RequestParam(name = "sortDirection") String sortDirection) {
        List<ChartServerCountPerServiceResponse> chartServerCountResponse =
                statisticsService.getServerCountPerService(projectId, serviceIds, sortDirection);

        return new ResponseEntity<>(chartServerCountResponse, HttpStatus.OK);
    }

    @Operation(summary = "서버 OS 분포 조회", description = "서버 OS 분포를 조회한다.")
    @ApiResponse(responseCode = "200")
    @GetMapping("/{metric}/server-count")
    public ResponseEntity<?> getOsDistribution(@PathVariable Long projectId,
                                               @PathVariable String metric,
                                               @RequestParam(name = "serviceIds") String serviceIds,
                                               @RequestParam(name = "sortDirection") String sortDirection) {
        List<ChartMetricResponse> chartOsDistributionResponse =
                statisticsService.getOsDistribution(projectId, metric, serviceIds, sortDirection);

        return new ResponseEntity<>(chartOsDistributionResponse, HttpStatus.OK);
    }

    @Operation(summary = "미들웨어 분포 조회", description = "미들웨어 분포를 조회한다.")
    @ApiResponse(responseCode = "200")
    @GetMapping("/{metric}/middleware-count")
    public ResponseEntity<?> getMiddlewareDistribution(@PathVariable Long projectId,
                                                       @PathVariable String metric,
                                                       @RequestParam(name = "serviceIds") String serviceIds,
                                                       @RequestParam(name = "sortDirection") String sortDirection) {
        List<ChartMetricResponse> chartMiddlewareDistributionResponse =
                statisticsService.getMiddlewareDistribution(projectId, metric, serviceIds, sortDirection);

        return new ResponseEntity<>(chartMiddlewareDistributionResponse, HttpStatus.OK);
    }

    @Operation(summary = "애플리케이션 분포 조회", description = "애플리케이션 분포를 조회한다.")
    @ApiResponse(responseCode = "200")
    @GetMapping("/application-count")
    public ResponseEntity<?> getApplicationDistribution(@PathVariable Long projectId,
                                                        @RequestParam(name = "serviceIds") String serviceIds,
                                                        @RequestParam(name = "sortDirection") String sortDirection) {
        List<ChartMetricResponse> chartApplicationDistributionResponse =
                statisticsService.getApplicationDistribution(projectId, serviceIds, sortDirection);

        return new ResponseEntity<>(chartApplicationDistributionResponse, HttpStatus.OK);
    }

    @Operation(summary = "데이터베이스 분포 조회", description = "데이터베이스 분포를 조회한다.")
    @ApiResponse(responseCode = "200")
    @GetMapping("/database-count")
    public ResponseEntity<?> getDatabaseDistribution(@PathVariable Long projectId,
                                                     @RequestParam(name = "serviceIds") String serviceIds,
                                                     @RequestParam(name = "sortDirection") String sortDirection) {
        List<ChartMetricResponse> chartDatabaseDistributionResponse =
                statisticsService.getDatabaseDistribution(projectId, serviceIds, sortDirection);

        return new ResponseEntity<>(chartDatabaseDistributionResponse, HttpStatus.OK);
    }

    @Operation(summary = "서버 모니터링 조회", description = "서버 모니터링한 데이터를 조회한다.")
    @ApiResponse(responseCode = "200")
    @GetMapping("/{metric}/monitoring")
    public ResponseEntity<?> getMonitoring(@PathVariable Long projectId,
                                           @PathVariable String metric,
                                           @RequestParam(name = "serviceIds") String serviceIds,
                                           ChartRequest chartRequest) {

        List<ChartMonitoringResponse> chartMonitoringResponse = new ArrayList<>();

        if (metric.equals(CPU_UTIL)) {
            chartMonitoringResponse = statisticsService.getCpuUtilMonitoring(projectId, serviceIds, chartRequest);
        } else if (metric.equals(MEM_UTIL)) {
            chartMonitoringResponse = statisticsService.getMemUtilMonitoring(projectId, serviceIds, chartRequest);
        } else if (metric.equals(MEM_USAGE)) {
            chartMonitoringResponse = statisticsService.getMemUsageMonitoring(projectId, serviceIds, chartRequest);
        }

        return new ResponseEntity<>(chartMonitoringResponse, HttpStatus.OK);
    }

    @Operation(summary = "서버별 사용량 조회", description = "서버별 디스크 사용량을 조회한다.")
    @ApiResponse(responseCode = "200")
    @GetMapping("/{metric}/server-usage")
    public ResponseEntity<?> getServerUsage(@PathVariable Long projectId,
                                            @PathVariable String metric,
                                            @RequestParam(name = "serviceIds") String serviceIds,
                                            ChartRequest chartRequest) {
        List<ChartUsageResponse> chartUsageResponses = new ArrayList<>();

        if (metric.equals(CPU_UTIL)) {
            chartUsageResponses = statisticsService.getCpuUtilUsage(projectId, serviceIds, chartRequest);
        } else if (metric.equals(MEM_UTIL)) {
            chartUsageResponses = statisticsService.getMemUtilUsage(projectId, serviceIds, chartRequest);
        } else if (metric.equals(DISK_UTIL)) {
            chartUsageResponses = statisticsService.getDiskUtilUsage(projectId, serviceIds, chartRequest);
        }

        return new ResponseEntity<>(chartUsageResponses, HttpStatus.OK);
    }

    @Operation(summary = "변경사항 추적", description = "프로젝트내 서버와 미들웨어의 변경사항을 조회한다.")
    @ApiResponse(responseCode = "200")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/tracking")
    public List<TrackingInfoWidgetDto> getTrackingInfo(@PathVariable Long projectId,
                                                       @RequestParam String inventoryTypeCode) {
        return trackingInfoService.getTrackingInfosForWidget(projectId, Domain1001.valueOf(inventoryTypeCode));
    }


}