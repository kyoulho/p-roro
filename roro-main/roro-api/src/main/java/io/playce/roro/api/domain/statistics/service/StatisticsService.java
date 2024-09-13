package io.playce.roro.api.domain.statistics.service;

import io.playce.roro.common.code.Domain1001;
import io.playce.roro.common.dto.statistics.*;
import io.playce.roro.common.dto.statistics.OverviewSummaryResponse.Application;
import io.playce.roro.common.dto.statistics.OverviewSummaryResponse.Database;
import io.playce.roro.common.dto.statistics.OverviewSummaryResponse.Middleware;
import io.playce.roro.common.dto.statistics.OverviewSummaryResponse.Server;
import io.playce.roro.mybatis.domain.statistics.StatisticsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final StatisticsMapper statisticsMapper;

    public OverviewSummaryResponse getOverviewSummary(Long projectId, String serviceIds) {
        int serviceCount = statisticsMapper.selectOverviewServiceCount(projectId, convertServiceId(serviceIds));

        List<Map<String, Object>> serverSummaries = statisticsMapper.selectOverviewSummaryCount(projectId, convertServiceId(serviceIds), Domain1001.SVR.name());
        List<Map<String, Object>> middlewareSummaries = statisticsMapper.selectOverviewSummaryCount(projectId, convertServiceId(serviceIds), Domain1001.MW.name());
        List<Map<String, Object>> databaseSummaries = statisticsMapper.selectOverviewSummaryCount(projectId, convertServiceId(serviceIds), Domain1001.DBMS.name());
        List<Map<String, Object>> applicationSummaries = statisticsMapper.selectOverviewSummaryCount(projectId, convertServiceId(serviceIds), Domain1001.APP.name());

        OverviewSummaryResponse overviewSummaryResponse = new OverviewSummaryResponse();
        overviewSummaryResponse.setService(new OverviewSummaryResponse.Service(serviceCount));
        overviewSummaryResponse.setServer(new Server(serverSummaries));
        overviewSummaryResponse.setMiddleware(new Middleware(middlewareSummaries));
        overviewSummaryResponse.setDatabase(new Database(databaseSummaries));
        overviewSummaryResponse.setApplication(new Application(applicationSummaries));

        return overviewSummaryResponse;
    }

    public List<ChartServerCountPerServiceResponse> getServerCountPerService(Long projectId, String serviceIds, String sortDirection) {
        return statisticsMapper.selectServerCountPerService(projectId, convertServiceId(serviceIds), sortDirection);
    }

    public List<ChartMetricResponse> getOsDistribution(Long projectId, String metric, String serviceIds, String sortDirection) {
        return statisticsMapper.selectOsDistribution(projectId, metric, convertServiceId(serviceIds), sortDirection);
    }

    public List<ChartMetricResponse> getMiddlewareDistribution(Long projectId, String metric, String serviceIds, String sortDirection) {
        return statisticsMapper.selectMiddlewareDistribution(projectId, metric, convertServiceId(serviceIds), sortDirection);
    }

    public List<ChartMetricResponse> getApplicationDistribution(Long projectId, String serviceIds, String sortDirection) {
        return statisticsMapper.selectApplicationDistribution(projectId, convertServiceId(serviceIds), sortDirection);
    }

    public List<ChartMetricResponse> getDatabaseDistribution(Long projectId, String serviceIds, String sortDirection) {
        return statisticsMapper.selectDatabaseDistribution(projectId, convertServiceId(serviceIds), sortDirection);
    }

    public List<ChartMonitoringResponse> getCpuUtilMonitoring(Long projectId, String serviceIds, ChartRequest chartRequest) {
        // Type(AVG, MAX), metric, Limit에 따라서 Server의 정보만 가져온다.
        // 서버의 정보를 구한 뒤 모니터링 데이터를 조회한다.
        List<ChartMonitoringResponse> chartMonitoringResponse =
                statisticsMapper.selectCpuMonitoringServer(projectId, convertServiceId(serviceIds), chartRequest);

        for (ChartMonitoringResponse monitoringResponse : chartMonitoringResponse) {
            if (monitoringResponse.getIsExistData().equals("Y")) {
                monitoringResponse.setData(statisticsMapper.selectCpuMonitoringData(monitoringResponse.getServerInventoryId(), chartRequest));
            } else {
                monitoringResponse.setData(new ArrayList<>());
            }
        }

        return chartMonitoringResponse;
    }

    public List<ChartMonitoringResponse> getMemUtilMonitoring(Long projectId, String serviceIds, ChartRequest chartRequest) {
        List<ChartMonitoringResponse> chartMonitoringResponse =
                statisticsMapper.selectMemoryUtilMonitoringServer(projectId, convertServiceId(serviceIds), chartRequest);

        for (ChartMonitoringResponse monitoringResponse : chartMonitoringResponse) {
            if (monitoringResponse.getIsExistData().equals("Y")) {
                monitoringResponse.setData(statisticsMapper.selectMemoryUtilMonitoringData(monitoringResponse.getServerInventoryId(), chartRequest));
            } else {
                monitoringResponse.setData(new ArrayList<>());
            }
        }

        return chartMonitoringResponse;
    }

    public List<ChartMonitoringResponse> getMemUsageMonitoring(Long projectId, String serviceIds, ChartRequest chartRequest) {
        List<ChartMonitoringResponse> chartMonitoringResponse =
                statisticsMapper.selectMemoryUsageMonitoringServer(projectId, convertServiceId(serviceIds), chartRequest);

        for (ChartMonitoringResponse monitoringResponse : chartMonitoringResponse) {
            if (monitoringResponse.getIsExistData().equals("Y")) {
                monitoringResponse.setData(statisticsMapper.selectMemoryUsageMonitoringData(monitoringResponse.getServerInventoryId(), chartRequest));
            } else {
                monitoringResponse.setData(new ArrayList<>());
            }
        }

        return chartMonitoringResponse;
    }

    public List<ChartUsageResponse> getCpuUtilUsage(Long projectId, String serviceIds, ChartRequest chartRequest) {
        return statisticsMapper.selectCpuUtilUsageServer(projectId, convertServiceId(serviceIds), chartRequest);
    }

    public List<ChartUsageResponse> getMemUtilUsage(Long projectId, String serviceIds, ChartRequest chartRequest) {
        return statisticsMapper.selectMemoryUtilUsageServer(projectId, convertServiceId(serviceIds), chartRequest);
    }

    public List<ChartUsageResponse> getDiskUtilUsage(Long projectId, String serviceIds, ChartRequest chartRequest) {
        return statisticsMapper.selectDiskUtilUsageServer(projectId, convertServiceId(serviceIds), chartRequest);
    }

    private List<Integer> convertServiceId(String serviceIds) {
        List<Integer> serviceIdList = new ArrayList<>();

        StringTokenizer st = new StringTokenizer(serviceIds, ",");
        while (st.hasMoreTokens()) {
            serviceIdList.add(Integer.parseInt(st.nextToken()));
        }

        return serviceIdList;
    }

}