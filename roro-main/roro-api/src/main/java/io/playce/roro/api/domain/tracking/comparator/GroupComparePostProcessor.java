package io.playce.roro.api.domain.tracking.comparator;

import com.fasterxml.jackson.core.type.TypeReference;
import io.playce.roro.common.dto.trackinginfo.TrackingInfoCompareDto;
import io.playce.roro.common.dto.trackinginfo.TrackingInfoCompareRawDto;
import io.playce.roro.common.enums.TrackingKey;
import io.playce.roro.common.util.JsonUtil;
import io.playce.roro.svr.asmt.dto.user.Group;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class GroupComparePostProcessor implements ITrackingInfoComparePostProcessor {

    private final TrackingKey trackingKey = TrackingKey.GROUP;

    @SneakyThrows
    @Override
    public void postProcess(TrackingInfoCompareRawDto rawDto, List<TrackingInfoCompareDto> result) {
        Map<String, Group> selected = JsonUtil.jsonToObj(rawDto.getSelected(), new TypeReference<>() {
        });
        Map<String, Group> compare = JsonUtil.jsonToObj(rawDto.getCompare(), new TypeReference<>() {
        });

        for (String group : selected.keySet()) {
            if (!compare.containsKey(group)) {
                TrackingInfoCompareDto dto = new TrackingInfoCompareDto();
                dto.setChangeType(trackingKey.getChangeType());
                dto.setProperty(group);
                dto.setSelected(selected.get(group));
                dto.setCompare(null);
                result.add(dto);
            } else {
                Group o1 = selected.get(group);
                Group o2 = compare.get(group);
                if (!o1.equals(o2)) {
                    TrackingInfoCompareDto dto = new TrackingInfoCompareDto();
                    dto.setChangeType(trackingKey.getChangeType());
                    dto.setProperty(group);
                    dto.setSelected(o1);
                    dto.setCompare(o2);
                    result.add(dto);
                }
            }
        }

        for (String group : compare.keySet()) {
            if (!selected.containsKey(group)) {
                TrackingInfoCompareDto dto = new TrackingInfoCompareDto();
                dto.setChangeType(trackingKey.getChangeType());
                dto.setProperty(group);
                dto.setSelected(null);
                dto.setCompare(compare.get(group));
                result.add(dto);
            }
        }
    }

    @Override
    public boolean isSupported(TrackingKey trackingKey) {
        return trackingKey == this.trackingKey;
    }

}
