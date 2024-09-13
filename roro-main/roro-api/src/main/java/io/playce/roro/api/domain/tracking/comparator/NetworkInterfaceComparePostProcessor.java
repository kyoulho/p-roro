package io.playce.roro.api.domain.tracking.comparator;

import com.fasterxml.jackson.core.type.TypeReference;
import io.playce.roro.common.dto.trackinginfo.TrackingInfoCompareDto;
import io.playce.roro.common.dto.trackinginfo.TrackingInfoCompareRawDto;
import io.playce.roro.common.enums.TrackingKey;
import io.playce.roro.common.util.JsonUtil;
import io.playce.roro.svr.asmt.dto.common.interfaces.InterfaceInfo;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;


@Component
public class NetworkInterfaceComparePostProcessor implements ITrackingInfoComparePostProcessor {

    private final TrackingKey trackingKey = TrackingKey.NETWORK_INTERFACE;

    @SneakyThrows
    @Override
    public void postProcess(TrackingInfoCompareRawDto rawDto, List<TrackingInfoCompareDto> result) {
        Map<String, InterfaceInfo> selected = JsonUtil.jsonToObj(rawDto.getSelected(), new TypeReference<>() {
        });
        Map<String, InterfaceInfo> compare = JsonUtil.jsonToObj(rawDto.getCompare(), new TypeReference<>() {
        });

        if (selected.entrySet().size() != compare.entrySet().size()) {
            TrackingInfoCompareDto dto1 = new TrackingInfoCompareDto();
            dto1.setChangeType(trackingKey.getChangeType());
            dto1.setProperty("Interface Count");
            dto1.setSelected(selected.entrySet().size());
            dto1.setCompare(compare.entrySet().size());
            result.add(dto1);
        }

        for (String hostName : selected.keySet()) {
            if (!compare.containsKey(hostName)) {
                TrackingInfoCompareDto dto = new TrackingInfoCompareDto();
                dto.setChangeType(trackingKey.getChangeType());
                dto.setProperty(hostName);
                dto.setSelected(selected.get(hostName));
                dto.setCompare(null);
                result.add(dto);
            } else {
                InterfaceInfo o1 = selected.get(hostName);
                InterfaceInfo o2 = compare.get(hostName);
                if (!o1.equals(o2)) {
                    TrackingInfoCompareDto dto = new TrackingInfoCompareDto();
                    dto.setChangeType(trackingKey.getChangeType());
                    dto.setProperty(hostName);
                    dto.setSelected(o1);
                    dto.setCompare(o2);
                    result.add(dto);
                }
            }
        }

        for (String interfaceName : compare.keySet()) {
            if (!selected.containsKey(interfaceName)) {
                TrackingInfoCompareDto dto = new TrackingInfoCompareDto();
                dto.setChangeType(trackingKey.getChangeType());
                dto.setProperty(interfaceName);
                dto.setSelected(null);
                dto.setCompare(compare.get(interfaceName));
                result.add(dto);
            }
        }
    }

    @Override
    public boolean isSupported(TrackingKey trackingKey) {
        return trackingKey == this.trackingKey;
    }

}
