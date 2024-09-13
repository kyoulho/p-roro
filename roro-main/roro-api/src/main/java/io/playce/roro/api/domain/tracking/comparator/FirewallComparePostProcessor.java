package io.playce.roro.api.domain.tracking.comparator;

import io.playce.roro.common.dto.trackinginfo.TrackingInfoCompareDto;
import io.playce.roro.common.dto.trackinginfo.TrackingInfoCompareRawDto;
import io.playce.roro.common.enums.TrackingKey;
import io.playce.roro.common.util.JsonUtil;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FirewallComparePostProcessor implements ITrackingInfoComparePostProcessor {

    private final TrackingKey trackingKey = TrackingKey.FIREWALL;

    @SneakyThrows
    @Override
    public void postProcess(TrackingInfoCompareRawDto rawDto, List<TrackingInfoCompareDto> result) {
        TrackingInfoCompareDto dto = new TrackingInfoCompareDto();
        dto.setChangeType(trackingKey.getChangeType());
        dto.setProperty("");

        String selected = rawDto.getSelected();
        String compare = rawDto.getCompare();
        dto.setSelected(JsonUtil.jsonToObj(selected, Object.class));
        dto.setCompare(JsonUtil.jsonToObj(compare, Object.class));
        result.add(dto);
    }

    @Override
    public boolean isSupported(TrackingKey trackingKey) {
        return trackingKey == this.trackingKey;
    }

}
