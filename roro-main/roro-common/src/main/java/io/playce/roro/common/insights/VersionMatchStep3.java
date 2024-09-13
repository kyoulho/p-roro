package io.playce.roro.common.insights;

import io.playce.roro.common.dto.productlifecycle.ProductLifecycleRulesVersionResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class VersionMatchStep3 implements IVersionMatchStep {
    private final IVersionMatchStep nextStep;

    @Override
    public ProductLifecycleRulesVersionResponse getVersionResponse(String inventoryVersion, List<ProductLifecycleRulesVersionResponse> versionResponseList) {
        Optional<ProductLifecycleRulesVersionResponse> any = versionResponseList.stream()
                .filter(
                        response -> !response.getVersion().trim().contains(StringUtils.SPACE)
                )
                .filter(
                        response -> {
                            String version = response.getVersion();
                            // .으로 시작하지 않고 .으로 끝나는가
                            String regex = "([^.]|)*" + version + ".*";
                            return Pattern.matches(regex, inventoryVersion);
                        }
                ).findAny();
        return any.orElse(nextStep.getVersionResponse(inventoryVersion,versionResponseList));
    }
}
