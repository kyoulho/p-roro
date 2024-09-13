package io.playce.roro.api.domain.tracking.comparator;

import com.fasterxml.jackson.core.type.TypeReference;
import io.playce.roro.common.dto.trackinginfo.TrackingInfoCompareDto;
import io.playce.roro.common.dto.trackinginfo.TrackingInfoCompareRawDto;
import io.playce.roro.common.enums.TrackingKey;
import io.playce.roro.common.util.JsonUtil;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class InstanceComparePostProcessor implements ITrackingInfoComparePostProcessor {

    private final TrackingKey trackingKey = TrackingKey.INSTANCE;

    @SneakyThrows
    @Override
    public void postProcess(TrackingInfoCompareRawDto rawDto, List<TrackingInfoCompareDto> result) {
        Map<String, String> selected = JsonUtil.jsonToObj(rawDto.getSelected(), new TypeReference<>() {
        });
        Map<String, String> compare = JsonUtil.jsonToObj(rawDto.getCompare(), new TypeReference<>() {
        });

        for (String instanceName : selected.keySet()) {
            if (!compare.containsKey(instanceName)) {
                TrackingInfoCompareDto dto = new TrackingInfoCompareDto();
                dto.setChangeType(trackingKey.getChangeType());
                dto.setProperty(instanceName);
                dto.setSelected(selected.get(instanceName));
                dto.setCompare(null);
                result.add(dto);
            } else {
                String o1 = selected.get(instanceName);
                String o2 = compare.get(instanceName);
                if (!o1.equals(o2)) {
                    TrackingInfoCompareDto dto = new TrackingInfoCompareDto();
                    dto.setChangeType(trackingKey.getChangeType());
                    dto.setProperty(instanceName);
                    dto.setSelected(o1);
                    dto.setCompare(o2);
                    result.add(dto);
                }
            }
        }

        for (String instanceName : compare.keySet()) {
            if (!selected.containsKey(instanceName)) {
                TrackingInfoCompareDto dto = new TrackingInfoCompareDto();
                dto.setChangeType(trackingKey.getChangeType());
                dto.setProperty(instanceName);
                dto.setSelected(null);
                dto.setCompare(compare.get(instanceName));
                result.add(dto);
            }
        }
    }

    @Override
    public boolean isSupported(TrackingKey trackingKey) {
        return trackingKey == this.trackingKey;
    }

}
