package io.playce.roro.common.insights;

import io.playce.roro.common.dto.productlifecycle.ProductLifecycleRulesVersionResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class VersionMatchStep2 implements IVersionMatchStep {
    private final IVersionMatchStep nextStep;

    @Override
    public ProductLifecycleRulesVersionResponse getVersionResponse(String inventoryVersion, List<ProductLifecycleRulesVersionResponse> versionResponseList) {
        Optional<ProductLifecycleRulesVersionResponse> any = versionResponseList.stream()
                .filter(
                        response -> response.getVersion().trim().contains(StringUtils.SPACE)
                )
                .filter(
                        response -> {
                            boolean result = true;
                            String[] strings = response.getVersion().split(StringUtils.SPACE);
                            for (String string : strings) {
                                if (inventoryVersion != null && !inventoryVersion.contains(string)) {
                                    result = false;
                                    break;
                                }
                            }
                            return result;
                        }
                ).findAny();

        return any.orElse(nextStep.getVersionResponse(inventoryVersion, versionResponseList));
    }
}
