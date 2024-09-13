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
public class VGComparePostProcessor implements ITrackingInfoComparePostProcessor {

    private final TrackingKey trackingKey = TrackingKey.VG;

    @SneakyThrows
    @Override
    public void postProcess(TrackingInfoCompareRawDto rawDto, List<TrackingInfoCompareDto> result) {
        Map<String, Map<String, List<Map<String, String>>>> selected = JsonUtil.jsonToObj(rawDto.getSelected(), new TypeReference<>() {
        });
        Map<String, Map<String, List<Map<String, String>>>> compare = JsonUtil.jsonToObj(rawDto.getCompare(), new TypeReference<>() {
        });

        for (String vg : selected.keySet()) {
            if (!compare.containsKey(vg)) {
                TrackingInfoCompareDto dto = new TrackingInfoCompareDto();
                dto.setChangeType(trackingKey.getChangeType());
                dto.setProperty(vg);
                dto.setSelected(JsonUtil.writeValueAsString(selected.get(vg)));
                dto.setCompare(null);
                result.add(dto);
            } else {
                Map<String, List<Map<String, String>>> o1 = selected.get(vg);
                Map<String, List<Map<String, String>>> o2 = compare.get(vg);
                if (!o1.equals(o2)) {
                    TrackingInfoCompareDto dto = new TrackingInfoCompareDto();
                    dto.setChangeType(trackingKey.getChangeType());
                    dto.setProperty(vg);
                    dto.setSelected(o1);
                    dto.setCompare(o2);
                    result.add(dto);
                }
            }
        }

        for (String vg : compare.keySet()) {
            if (!selected.containsKey(vg)) {
                TrackingInfoCompareDto dto = new TrackingInfoCompareDto();
                dto.setChangeType(trackingKey.getChangeType());
                dto.setProperty(vg);
                dto.setSelected(null);
                dto.setCompare(JsonUtil.writeValueAsString(compare.get(vg)));
                result.add(dto);
            }
        }
    }

    @Override
    public boolean isSupported(TrackingKey trackingKey) {
        return trackingKey == this.trackingKey;
    }

}
