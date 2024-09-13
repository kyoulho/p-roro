package io.playce.roro.api.domain.tracking.processor.middleware;

import io.playce.roro.mw.asmt.dto.MiddlewareAssessmentResult;


public interface IMiddlewareTrackingInfoProcessor {

    void saveTrackingInfo(Long inventoryProcessId, MiddlewareAssessmentResult result);

    boolean isSupported(MiddlewareAssessmentResult result);
}
