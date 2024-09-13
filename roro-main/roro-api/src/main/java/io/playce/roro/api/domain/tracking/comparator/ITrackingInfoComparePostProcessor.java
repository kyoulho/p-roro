package io.playce.roro.api.domain.tracking.comparator;

import io.playce.roro.common.dto.trackinginfo.TrackingInfoCompareDto;
import io.playce.roro.common.dto.trackinginfo.TrackingInfoCompareRawDto;
import io.playce.roro.common.enums.TrackingKey;

import java.util.List;

public interface ITrackingInfoComparePostProcessor {

    void postProcess(TrackingInfoCompareRawDto rawDto, List<TrackingInfoCompareDto> result);

    boolean isSupported(TrackingKey trackingKey);
}
