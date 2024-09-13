package io.playce.roro.api.domain.tracking.processor.middleware;

import io.playce.roro.jpa.repository.TrackingInfoRepository;
import io.playce.roro.mw.asmt.dto.MiddlewareAssessmentResult;
import io.playce.roro.mw.asmt.jboss.dto.JbossAssessmentResult;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

@Component
public class JbossTrackingInfoProcessor extends AbstractMWTrackingInfoProcessor {
    public JbossTrackingInfoProcessor(TrackingInfoRepository trackingInfoRepository) {
        super(trackingInfoRepository);
    }

    @Override
    public void saveTrackingInfo(Long inventoryProcessId, MiddlewareAssessmentResult result) {
        var assessmentResult = (JbossAssessmentResult) result;
        var instance = (JbossAssessmentResult.Instance) assessmentResult.getInstance();

        Map<String, String> configFiles = instance.getConfigFiles().values().stream().collect(Collectors.toMap(JbossAssessmentResult.ConfigFile::getPath, JbossAssessmentResult.ConfigFile::getContents));
        Map<String, String> instances = instance.getInstances().stream().collect(Collectors.toMap(JbossAssessmentResult.Instances::getName, i -> getInstanceStatus(i.getIsRunning().toString())));

        saveMiddlewareConfigs(inventoryProcessId,configFiles);
        saveMiddlewareInstances(inventoryProcessId,instances);
    }

    @Override
    public boolean isSupported(MiddlewareAssessmentResult result) {
        return result instanceof JbossAssessmentResult;
    }
}
