
package io.playce.roro.api.domain.tracking.comparator;

import io.playce.roro.common.dto.trackinginfo.TrackingInfoCompareDto;
import io.playce.roro.common.dto.trackinginfo.TrackingInfoCompareRawDto;
import io.playce.roro.common.enums.TrackingKey;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DefaultComparePostProcessor implements ITrackingInfoComparePostProcessor {


    @SneakyThrows
    @Override
    public void postProcess(TrackingInfoCompareRawDto rawDto, List<TrackingInfoCompareDto> result) {
        String selected = rawDto.getSelected();
        String compare = rawDto.getCompare();

        if (!selected.equals(compare)) {
            TrackingKey trackingKey = rawDto.getTrackingKey();

            TrackingInfoCompareDto dto = new TrackingInfoCompareDto();
            dto.setChangeType(trackingKey.getChangeType());
            dto.setProperty(trackingKey.getProperty());
            dto.setSelected(selected);
            dto.setCompare(compare);
            result.add(dto);
        }
    }

    @Override
    public boolean isSupported(TrackingKey trackingKey) {
        List<TrackingKey> list = List.of(TrackingKey.CPU_MODEL, TrackingKey.CPU_CORES, TrackingKey.CPU_COUNT, TrackingKey.MEMORY_SIZE, TrackingKey.KERNEL_VERSION, TrackingKey.OS_VERSION, TrackingKey.BIOS_VERSION);
        return list.contains(trackingKey);
    }

}
