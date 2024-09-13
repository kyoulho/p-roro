package io.playce.roro.api.domain.tracking.processor.server;

import io.playce.roro.svr.asmt.dto.ServerAssessmentResult;


public interface IServerTrackingInfoProcessor {

    void saveTrackingInfo(Long inventoryProcessId, ServerAssessmentResult result);

    boolean isSupported(ServerAssessmentResult result);
}
