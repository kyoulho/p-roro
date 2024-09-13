package io.playce.roro.api.domain.tracking.processor.middleware;

import io.playce.roro.jpa.repository.TrackingInfoRepository;
import io.playce.roro.mw.asmt.dto.MiddlewareAssessmentResult;
import io.playce.roro.mw.asmt.websphere.dto.WebSphereAssessmentResult;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

@Component
public class WebSphereTrackingInfoProcessor extends AbstractMWTrackingInfoProcessor {
    public WebSphereTrackingInfoProcessor(TrackingInfoRepository trackingInfoRepository) {
        super(trackingInfoRepository);
    }

    @Override
    public void saveTrackingInfo(Long inventoryProcessId, MiddlewareAssessmentResult result) {
        var assessmentResult = (WebSphereAssessmentResult) result;
        var instance = (WebSphereAssessmentResult.Instance) assessmentResult.getInstance();

        Map<String, String> configFiles = instance.getGeneral().getProfiles().stream().collect(Collectors.toMap(WebSphereAssessmentResult.Profile::getPath, WebSphereAssessmentResult.Profile::getName));
        saveMiddlewareConfigs(inventoryProcessId, configFiles);

        Map<String, String> instances = instance.getInstances().stream()
                .collect(Collectors.toMap(WebSphereAssessmentResult.Instances::getName, i -> getInstanceStatus(i.getRunUser())));
        saveMiddlewareInstances(inventoryProcessId, instances);
    }

    @Override
    public boolean isSupported(MiddlewareAssessmentResult result) {
        return result instanceof WebSphereAssessmentResult;
    }
}
