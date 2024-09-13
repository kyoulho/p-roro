package io.playce.roro.api.domain.tracking;

import io.playce.roro.api.domain.tracking.comparator.ITrackingInfoComparePostProcessor;
import io.playce.roro.api.domain.tracking.processor.middleware.IMiddlewareTrackingInfoProcessor;
import io.playce.roro.api.domain.tracking.processor.server.IServerTrackingInfoProcessor;
import io.playce.roro.common.code.Domain1001;
import io.playce.roro.common.dto.trackinginfo.TrackingInfoCompareDto;
import io.playce.roro.common.dto.trackinginfo.TrackingInfoCompareRawDto;
import io.playce.roro.common.dto.trackinginfo.TrackingInfoWidgetDto;
import io.playce.roro.mw.asmt.dto.MiddlewareAssessmentResult;
import io.playce.roro.mybatis.domain.trackinginfo.TrackingInfoMapper;
import io.playce.roro.svr.asmt.dto.ServerAssessmentResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrackingInfoService {
    private final TrackingInfoMapper trackingInfoMapper;
    private final Set<IMiddlewareTrackingInfoProcessor> middlewareTrackingInfoProcessors;
    private final Set<IServerTrackingInfoProcessor> serverTrackingInfoProcessors;
    private final Set<ITrackingInfoComparePostProcessor> trackingInfoComparators;


    public void saveServerTrackingInfo(Long inventoryProcessId, ServerAssessmentResult result) {
        for (IServerTrackingInfoProcessor trackingInfoProcessor : serverTrackingInfoProcessors) {
            if (trackingInfoProcessor.isSupported(result)) {
                trackingInfoProcessor.saveTrackingInfo(inventoryProcessId, result);
                break;
            }
        }
    }

    public void saveMiddlewareTrackingInfo(Long inventoryProcessId, MiddlewareAssessmentResult result) {
        for (IMiddlewareTrackingInfoProcessor trackingInfoProcessor : middlewareTrackingInfoProcessors) {
            if (trackingInfoProcessor.isSupported(result)) {
                trackingInfoProcessor.saveTrackingInfo(inventoryProcessId, result);
                break;
            }
        }
    }

    public List<TrackingInfoWidgetDto> getTrackingInfosForWidget(Long projectId, Domain1001 inventoryTypeCode) {
        List<TrackingInfoWidgetDto> result = new ArrayList<>();
        List<Long> inventoryIds = trackingInfoMapper.getTwiceScanCompletedInventoryIds(projectId, inventoryTypeCode.name());
        for (Long inventoryId : inventoryIds) {
            List<Long> inventoryProcessIds = trackingInfoMapper.getScanCompletedInventoryProcessIdLimit2(inventoryId);
            TrackingInfoWidgetDto widgetDto = trackingInfoMapper.getWidgetDto(inventoryProcessIds.get(0), inventoryProcessIds.get(1));
            if (widgetDto.getChangeCount() > 0) {
                result.add(widgetDto);
            }
        }
        return result;
    }

    public List<TrackingInfoCompareDto> getTrackingInfoForCompare(Long selectedInventoryProcessId, Long compareInventoryProcessId) {
        List<TrackingInfoCompareDto> result = new ArrayList<>();
        List<TrackingInfoCompareRawDto> compareDtoList = trackingInfoMapper.getCompareDtoList(selectedInventoryProcessId, compareInventoryProcessId);
        for (TrackingInfoCompareRawDto dto : compareDtoList) {
            for (ITrackingInfoComparePostProcessor comparator : trackingInfoComparators) {
                if (comparator.isSupported(dto.getTrackingKey())) {
                    comparator.postProcess(dto, result);
                }
            }
        }
        return result;
    }
}
