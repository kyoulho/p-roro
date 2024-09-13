package io.playce.roro.api.domain.tracking.comparator;

import io.playce.roro.common.dto.trackinginfo.TrackingInfoCompareDto;
import io.playce.roro.common.dto.trackinginfo.TrackingInfoCompareRawDto;
import io.playce.roro.common.enums.TrackingKey;
import io.playce.roro.common.util.JsonUtil;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;


@Component
public class HostComparePostProcessor implements ITrackingInfoComparePostProcessor {

    private final TrackingKey trackingKey = TrackingKey.HOST;

    @SneakyThrows
    @Override
    public void postProcess(TrackingInfoCompareRawDto rawDto, List<TrackingInfoCompareDto> result) {
        TrackingInfoCompareDto dto = new TrackingInfoCompareDto();
        dto.setChangeType(trackingKey.getChangeType());
        dto.setProperty("");
        dto.setSelected(JsonUtil.jsonToObj(rawDto.getSelected(), LinkedHashMap.class));
        dto.setCompare(JsonUtil.jsonToObj(rawDto.getCompare(), LinkedHashMap.class));
        result.add(dto);
    }

    @Override
    public boolean isSupported(TrackingKey trackingKey) {
        return trackingKey == this.trackingKey;
    }

}
