package io.playce.roro.mybatis.domain.trackinginfo;

import io.playce.roro.common.dto.trackinginfo.TrackingInfoCompareRawDto;
import io.playce.roro.common.dto.trackinginfo.TrackingInfoWidgetDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TrackingInfoMapper {

    List<Long> getTwiceScanCompletedInventoryIds(@Param("projectId") Long projectId, @Param("inventoryTypeCode") String inventoryTypeCode);

    List<Long> getScanCompletedInventoryProcessIdLimit2(Long inventoryId);

    TrackingInfoWidgetDto getWidgetDto(@Param("lastInventoryProcessId") Long lastInventoryProcessId, @Param("penultimateInventoryProcessId") Long penultimateInventoryProcessId);

    List<TrackingInfoCompareRawDto> getCompareDtoList(@Param("selectedInventoryProcessId") Long selectedInventoryProcessId, @Param("compareInventoryProcessId") Long compareInventoryProcessId);
}
