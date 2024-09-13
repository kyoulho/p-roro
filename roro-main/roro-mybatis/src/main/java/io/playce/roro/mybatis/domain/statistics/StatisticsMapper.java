package io.playce.roro.mybatis.domain.statistics;

import io.playce.roro.common.dto.statistics.*;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface StatisticsMapper {

    int selectOverviewServiceCount(@Param("projectId") Long projectId,
                                   @Param("serviceIds") List<Integer> serviceIds);

    List<Map<String, Object>> selectOverviewSummaryCount(@Param("projectId") Long projectId,
                                                         @Param("serviceIds") List<Integer> serviceIds,
                                                         @Param("inventoryType") String inventoryType);

    List<ChartServerCountPerServiceResponse> selectServerCountPerService(@Param("projectId") Long projectId,
                                                                         @Param("serviceIds") List<Integer> serviceIds,
                                                                         @Param("sortDirection") String sortDirection);

    List<ChartMetricResponse> selectOsDistribution(@Param("projectId") Long projectId,
                                                   @Param("metric") String metric,
                                                   @Param("serviceIds") List<Integer> serviceIds,
                                                   @Param("sortDirection") String sortDirection);

    List<ChartMetricResponse> selectMiddlewareDistribution(@Param("projectId") Long projectId,
                                                           @Param("metric") String metric,
                                                           @Param("serviceIds") List<Integer> serviceIds,
                                                           @Param("sortDirection") String sortDirection);

    List<ChartMetricResponse> selectApplicationDistribution(@Param("projectId") Long projectId,
                                                            @Param("serviceIds") List<Integer> serviceIds,
                                                            @Param("sortDirection") String sortDirection);

    List<ChartMetricResponse> selectDatabaseDistribution(@Param("projectId") Long projectId,
                                                         @Param("serviceIds") List<Integer> serviceIds,
                                                         @Param("sortDirection") String sortDirection);

    List<ChartMonitoringResponse> selectCpuMonitoringServer(@Param("projectId") Long projectId,
                                                            @Param("serviceIds") List<Integer> serviceIds,
                                                            @Param("chartRequest") ChartRequest chartRequest);

    List<ChartData> selectCpuMonitoringData(@Param("serverId") Long serverId,
                                            @Param("chartRequest") ChartRequest chartRequest);

    List<ChartMonitoringResponse> selectMemoryUtilMonitoringServer(@Param("projectId") Long projectId,
                                                                   @Param("serviceIds") List<Integer> serviceIds,
                                                                   @Param("chartRequest") ChartRequest chartRequest);

    List<ChartData> selectMemoryUtilMonitoringData(@Param("serverId") Long serverId,
                                                   @Param("chartRequest") ChartRequest chartRequest);

    List<ChartMonitoringResponse> selectMemoryUsageMonitoringServer(@Param("projectId") Long projectId,
                                                                    @Param("serviceIds") List<Integer> serviceIds,
                                                                    @Param("chartRequest") ChartRequest chartRequest);

    List<ChartData> selectMemoryUsageMonitoringData(@Param("serverId") Long serverId,
                                                    @Param("chartRequest") ChartRequest chartRequest);

    List<ChartUsageResponse> selectCpuUtilUsageServer(@Param("projectId") Long projectId,
                                                      @Param("serviceIds") List<Integer> serviceIds,
                                                      @Param("chartRequest") ChartRequest chartRequest);

    List<ChartUsageResponse> selectMemoryUtilUsageServer(@Param("projectId") Long projectId,
                                                         @Param("serviceIds") List<Integer> serviceIds,
                                                         @Param("chartRequest") ChartRequest chartRequest);

    List<ChartUsageResponse> selectDiskUtilUsageServer(@Param("projectId") Long projectId,
                                                       @Param("serviceIds") List<Integer> serviceIds,
                                                       @Param("chartRequest") ChartRequest chartRequest);
}