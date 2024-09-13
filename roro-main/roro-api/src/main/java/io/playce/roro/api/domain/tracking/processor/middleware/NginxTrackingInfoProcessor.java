package io.playce.roro.api.domain.tracking.processor.middleware;

import io.playce.roro.jpa.repository.TrackingInfoRepository;
import io.playce.roro.mw.asmt.dto.MiddlewareAssessmentResult;
import io.playce.roro.mw.asmt.nginx.dto.NginxAssessmentResult;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

@Component
public class NginxTrackingInfoProcessor extends AbstractMWTrackingInfoProcessor {
    public NginxTrackingInfoProcessor(TrackingInfoRepository trackingInfoRepository) {
        super(trackingInfoRepository);
    }

    @Override
    public void saveTrackingInfo(Long inventoryProcessId, MiddlewareAssessmentResult result) {
        var assessmentResult = (NginxAssessmentResult) result;
        var instance = (NginxAssessmentResult.Instance) assessmentResult.getInstance();
        var engine = (NginxAssessmentResult.Engine) assessmentResult.getEngine();

        Map<String, String> configFiles = instance.getConfigFiles().stream().collect(Collectors.toMap(NginxAssessmentResult.ConfigFile::getPath, NginxAssessmentResult.ConfigFile::getContent));
        saveMiddlewareConfigs(inventoryProcessId, configFiles);

        Map<String, String> instances = Map.of(engine.getName(), getInstanceStatus(instance.getGeneral().getUser()));
        saveMiddlewareInstances(inventoryProcessId, instances);
    }

    @Override
    public boolean isSupported(MiddlewareAssessmentResult result) {
        return result instanceof NginxAssessmentResult;
    }
}
