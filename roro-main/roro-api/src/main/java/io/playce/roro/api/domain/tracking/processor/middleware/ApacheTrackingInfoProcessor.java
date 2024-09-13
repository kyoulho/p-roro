package io.playce.roro.api.domain.tracking.processor.middleware;

import io.playce.roro.jpa.repository.TrackingInfoRepository;
import io.playce.roro.mw.asmt.apache.dto.ApacheAssessmentResult;
import io.playce.roro.mw.asmt.dto.MiddlewareAssessmentResult;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ApacheTrackingInfoProcessor extends AbstractMWTrackingInfoProcessor{
    public ApacheTrackingInfoProcessor(TrackingInfoRepository trackingInfoRepository) {
        super(trackingInfoRepository);
    }

    @Override
    public void saveTrackingInfo(Long inventoryProcessId, MiddlewareAssessmentResult result) {
        var assessmentResult = (ApacheAssessmentResult) result;
        var instance = (ApacheAssessmentResult.Instance) assessmentResult.getInstance();

        Map<String, String> instances = Map.of(instance.getGeneral().getServerName(), getInstanceStatus(instance.getGeneral().getServerName()));
        Map<String, String> configFiles = instance.getConfigFiles().stream().collect(Collectors.toMap(ApacheAssessmentResult.ConfigFile::getPath, ApacheAssessmentResult.ConfigFile::getContent));
        saveMiddlewareConfigs(inventoryProcessId, configFiles);
        saveMiddlewareInstances(inventoryProcessId, instances);
    }

    @Override
    public boolean isSupported(MiddlewareAssessmentResult result) {
        return result instanceof ApacheAssessmentResult;
    }
}
