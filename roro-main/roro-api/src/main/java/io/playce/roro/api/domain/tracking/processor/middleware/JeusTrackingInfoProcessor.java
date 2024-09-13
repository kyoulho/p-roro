package io.playce.roro.api.domain.tracking.processor.middleware;

import io.playce.roro.jpa.repository.TrackingInfoRepository;
import io.playce.roro.mw.asmt.dto.MiddlewareAssessmentResult;
import io.playce.roro.mw.asmt.jeus.dto.JeusAssessmentResult;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

@Component
public class JeusTrackingInfoProcessor extends AbstractMWTrackingInfoProcessor {
    public JeusTrackingInfoProcessor(TrackingInfoRepository trackingInfoRepository) {
        super(trackingInfoRepository);
    }

    @Override
    public void saveTrackingInfo(Long inventoryProcessId, MiddlewareAssessmentResult result) {
        var assessmentResult = (JeusAssessmentResult) result;
        var instance = (JeusAssessmentResult.Instance) assessmentResult.getInstance();

        Map<String, String> configFiles = instance.getConfigFiles()
                .stream().collect(Collectors.toMap(JeusAssessmentResult.ConfigFile::getPath, JeusAssessmentResult.ConfigFile::getContents));
        saveMiddlewareConfigs(inventoryProcessId, configFiles);

        Map<String, String> instances = instance.getInstances().stream().collect(Collectors.toMap(JeusAssessmentResult.Instances::getName, i -> getInstanceStatus(i.getRunUser())));
        saveMiddlewareInstances(inventoryProcessId, instances);
    }

    @Override
    public boolean isSupported(MiddlewareAssessmentResult result) {
        return result instanceof JeusAssessmentResult;
    }
}
