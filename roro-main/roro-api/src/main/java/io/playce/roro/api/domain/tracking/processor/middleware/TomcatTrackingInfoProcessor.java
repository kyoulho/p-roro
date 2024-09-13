package io.playce.roro.api.domain.tracking.processor.middleware;

import io.playce.roro.jpa.repository.TrackingInfoRepository;
import io.playce.roro.mw.asmt.dto.MiddlewareAssessmentResult;
import io.playce.roro.mw.asmt.tomcat.dto.TomcatAssessmentResult;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

@Component
public class TomcatTrackingInfoProcessor extends AbstractMWTrackingInfoProcessor {
    public TomcatTrackingInfoProcessor(TrackingInfoRepository trackingInfoRepository) {
        super(trackingInfoRepository);
    }

    @Override
    public void saveTrackingInfo(Long inventoryProcessId, MiddlewareAssessmentResult result) {
        var assessmentResult = (TomcatAssessmentResult) result;
        var instance = (TomcatAssessmentResult.Instance) assessmentResult.getInstance();

        Map<String, String> configFiles = instance.getConfigFiles().values().stream().collect(Collectors.toMap(TomcatAssessmentResult.ConfigFile::getPath, TomcatAssessmentResult.ConfigFile::getContents));
        saveMiddlewareConfigs(inventoryProcessId, configFiles);

        String path = instance.getPath();
        String instanceName = "";
        if (path.contains("/")) {
            String[] parts = path.split("/");
            instanceName = parts[parts.length - 1];
        } else if (path.contains("\\")) {
            String[] parts = path.split("\\\\");
            instanceName = parts[parts.length - 1];
        }

        var instances = Map.of(instanceName, getInstanceStatus(instance.getIsRunning()));
        saveMiddlewareInstances(inventoryProcessId, instances);
    }

    @Override
    public boolean isSupported(MiddlewareAssessmentResult result) {
        return result instanceof TomcatAssessmentResult;
    }
}
