package io.playce.roro.common.insights;

import io.playce.roro.common.dto.productlifecycle.ProductLifecycleRulesVersionResponse;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class VersionMatchStep1 implements IVersionMatchStep {
    private final IVersionMatchStep nextStep;

    @Override
    public ProductLifecycleRulesVersionResponse getVersionResponse(String inventoryVersion, List<ProductLifecycleRulesVersionResponse> versionResponseList) {
        for (ProductLifecycleRulesVersionResponse response : versionResponseList) {
            String ruleVersion = response.getVersion();
            if (inventoryVersion != null && inventoryVersion.equals(ruleVersion)) {
                return response;
            }
        }

        return nextStep.getVersionResponse(inventoryVersion, versionResponseList);
    }
}
