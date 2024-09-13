package io.playce.roro.api.domain.tracking.processor.middleware;

import io.grpc.netty.shaded.io.netty.util.internal.StringUtil;
import io.playce.roro.common.enums.TrackingKey;
import io.playce.roro.common.util.JsonUtil;
import io.playce.roro.jpa.entity.TrackingInfo;
import io.playce.roro.jpa.repository.TrackingInfoRepository;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public abstract class AbstractMWTrackingInfoProcessor implements IMiddlewareTrackingInfoProcessor {
    private final TrackingInfoRepository trackingInfoRepository;


    protected void saveMiddlewareConfigs(Long inventoryProcessId, Map<String, String> configFiles) {
        TrackingInfo trackingInfo = new TrackingInfo();
        trackingInfo.setInventoryProcessId(inventoryProcessId);
        trackingInfo.setTrackingKey(TrackingKey.CONFIG_FILE);
        trackingInfo.setContent(JsonUtil.writeValueAsString(configFiles));
        trackingInfoRepository.save(trackingInfo);
    }

    protected void saveMiddlewareInstances(Long inventoryProcessId, Map<String, String> instances) {
        TrackingInfo trackingInfo = new TrackingInfo();
        trackingInfo.setInventoryProcessId(inventoryProcessId);
        trackingInfo.setTrackingKey(TrackingKey.INSTANCE);
        trackingInfo.setContent(JsonUtil.writeValueAsString(instances));
        trackingInfoRepository.save(trackingInfo);
    }

    protected String getInstanceStatus(String value) {
        if (StringUtil.isNullOrEmpty(value) || value.equals("false")) {
            return "stopped";
        }
        return "running";
    }
}
