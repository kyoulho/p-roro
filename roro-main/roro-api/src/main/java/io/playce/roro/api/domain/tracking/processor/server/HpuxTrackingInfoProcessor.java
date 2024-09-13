package io.playce.roro.api.domain.tracking.processor.server;

import io.playce.roro.common.enums.TrackingKey;
import io.playce.roro.common.util.JsonUtil;
import io.playce.roro.jpa.entity.TrackingInfo;
import io.playce.roro.jpa.repository.TrackingInfoRepository;
import io.playce.roro.svr.asmt.dto.ServerAssessmentResult;
import io.playce.roro.svr.asmt.dto.result.HpuxAssessmentResult;
import org.springframework.stereotype.Component;

@Component
public class HpuxTrackingInfoProcessor extends ServerTrackingInfoProcessor {

    public HpuxTrackingInfoProcessor(TrackingInfoRepository trackingInfoRepository) {
        super(trackingInfoRepository);
    }

    @Override
    protected void saveVG(Long inventoryProcessId, ServerAssessmentResult result) {
        TrackingInfo vgs = new TrackingInfo();
        HpuxAssessmentResult assessmentResult = (HpuxAssessmentResult) result;

        vgs.setInventoryProcessId(inventoryProcessId);
        vgs.setTrackingKey(TrackingKey.VG);
        vgs.setContent(JsonUtil.writeValueAsString(assessmentResult.getVgs()));
        trackingInfoRepository.save(vgs);
    }

    @Override
    public boolean isSupported(ServerAssessmentResult result) {
        return result instanceof HpuxAssessmentResult;
    }
}