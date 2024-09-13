package io.playce.roro.api.domain.tracking.processor.middleware;

import io.playce.roro.jpa.repository.TrackingInfoRepository;
import io.playce.roro.mw.asmt.dto.MiddlewareAssessmentResult;
import io.playce.roro.mw.asmt.webtob.dto.WebToBAssessmentResult;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

@Component
public class WebToBTrackingInfoProcessor extends AbstractMWTrackingInfoProcessor {
    public WebToBTrackingInfoProcessor(TrackingInfoRepository trackingInfoRepository) {
        super(trackingInfoRepository);
    }

    @Override
    public void saveTrackingInfo(Long inventoryProcessId, MiddlewareAssessmentResult result) {
        var assessmentResult = (WebToBAssessmentResult) result;
        var instance = (WebToBAssessmentResult.Instance) assessmentResult.getInstance();
        var engine = (WebToBAssessmentResult.Engine) assessmentResult.getEngine();

        Map<String, String> configFiles = instance.getConfigFiles().stream().collect(Collectors.toMap(WebToBAssessmentResult.ConfigFile::getPath, WebToBAssessmentResult.ConfigFile::getContents));
        saveMiddlewareConfigs(inventoryProcessId, configFiles);

        Map<String, String> instances = instance.getNodes().stream().collect(Collectors.toMap(WebToBAssessmentResult.Node::getName, i -> getInstanceStatus(engine.getRunUser())));
        saveMiddlewareInstances(inventoryProcessId, instances);
    }

    @Override
    public boolean isSupported(MiddlewareAssessmentResult result) {
        return result instanceof WebToBAssessmentResult;
    }
}
