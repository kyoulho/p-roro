package io.playce.roro.api.domain.tracking.processor.middleware;

import io.playce.roro.jpa.repository.TrackingInfoRepository;
import io.playce.roro.mw.asmt.dto.MiddlewareAssessmentResult;
import io.playce.roro.mw.asmt.weblogic.dto.WebLogicAssessmentResult;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

@Component
public class WebLogicTrackingInfoProcessor extends AbstractMWTrackingInfoProcessor {
    public WebLogicTrackingInfoProcessor(TrackingInfoRepository trackingInfoRepository) {
        super(trackingInfoRepository);
    }

    @Override
    public void saveTrackingInfo(Long inventoryProcessId, MiddlewareAssessmentResult result) {
        var assessmentResult = (WebLogicAssessmentResult) result;
        var instance = (WebLogicAssessmentResult.Instance) assessmentResult.getInstance();

        Map<String, String> configFiles = instance.getConfigFiles().stream().collect(Collectors.toMap(WebLogicAssessmentResult.ConfigFile::getPath, WebLogicAssessmentResult.ConfigFile::getContents));
        saveMiddlewareConfigs(inventoryProcessId, configFiles);

        Map<String, String> instances = instance.getInstances().stream().collect(Collectors.toMap(WebLogicAssessmentResult.Instances::getName, i -> getInstanceStatus(i.getRunUser())));
        saveMiddlewareInstances(inventoryProcessId, instances);
    }

    @Override
    public boolean isSupported(MiddlewareAssessmentResult result) {
        return result instanceof WebLogicAssessmentResult;
    }
}
