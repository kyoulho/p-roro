package io.playce.roro.common.insights;

import io.playce.roro.common.dto.productlifecycle.ProductLifecycleRulesVersionResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LifecycleVersionManager {
    private final IVersionMatchStep step = new VersionMatchStep1(new VersionMatchStep2(new VersionMatchStep3(new VersionMatchStep4())));

    public ProductLifecycleRulesVersionResponse getVersionResponse(String inventoryVersion, List<ProductLifecycleRulesVersionResponse> versionResponseList) {
        return step.getVersionResponse(inventoryVersion, versionResponseList);
    }

}
