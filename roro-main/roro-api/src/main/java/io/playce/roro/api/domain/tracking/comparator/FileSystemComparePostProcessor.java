package io.playce.roro.api.domain.tracking.comparator;

import com.fasterxml.jackson.core.type.TypeReference;
import io.playce.roro.common.dto.trackinginfo.TrackingInfoCompareDto;
import io.playce.roro.common.dto.trackinginfo.TrackingInfoCompareRawDto;
import io.playce.roro.common.enums.TrackingKey;
import io.playce.roro.common.util.JsonUtil;
import io.playce.roro.svr.asmt.dto.common.disk.Partition;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class FileSystemComparePostProcessor implements ITrackingInfoComparePostProcessor {

    private final TrackingKey trackingKey = TrackingKey.FILE_SYSTEM;

    @SneakyThrows
    @Override
    public void postProcess(TrackingInfoCompareRawDto rawDto, List<TrackingInfoCompareDto> result) {
        Map<String, Partition> selected = JsonUtil.jsonToObj(rawDto.getSelected(), new TypeReference<>() {
        });
        Map<String, Partition> compare = JsonUtil.jsonToObj(rawDto.getCompare(), new TypeReference<>() {
        });

        for (String partitionName : selected.keySet()) {
            if (!compare.containsKey(partitionName)) {
                TrackingInfoCompareDto dto = new TrackingInfoCompareDto();
                dto.setChangeType(trackingKey.getChangeType());
                dto.setProperty(partitionName);
                dto.setSelected(selected.get(partitionName));
                dto.setCompare(null);
                result.add(dto);
            } else {
                Partition o1 = selected.get(partitionName);
                Partition o2 = compare.get(partitionName);

                if(!o1.getFree().equals(o2.getFree()) || !o1.getSize().equals(o2.getSize())){
                    TrackingInfoCompareDto dto = new TrackingInfoCompareDto();
                    dto.setChangeType(trackingKey.getChangeType());
                    dto.setProperty(partitionName);
                    dto.setSelected(selected.get(partitionName));
                    dto.setCompare(compare.get(partitionName));
                    result.add(dto);
                }

            }
        }

        for (String partitionName : compare.keySet()) {
            if (!selected.containsKey(partitionName)) {
                TrackingInfoCompareDto dto = new TrackingInfoCompareDto();
                dto.setChangeType(trackingKey.getChangeType());
                dto.setProperty(partitionName);
                dto.setSelected(null);
                dto.setCompare(compare.get(partitionName));
                result.add(dto);
            }
        }
    }

    @Override
    public boolean isSupported(TrackingKey trackingKey) {
        return trackingKey == this.trackingKey;
    }

}
