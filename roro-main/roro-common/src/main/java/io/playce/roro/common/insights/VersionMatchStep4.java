package io.playce.roro.common.insights;

import io.playce.roro.common.dto.productlifecycle.ProductLifecycleRulesVersionResponse;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class VersionMatchStep4 implements IVersionMatchStep{
    @Override
    public ProductLifecycleRulesVersionResponse getVersionResponse(String inventoryVersion, List<ProductLifecycleRulesVersionResponse> versionResponseList) {
        Optional<ProductLifecycleRulesVersionResponse> any = versionResponseList.stream()
                .filter(
                        response -> !response.getVersion().trim().contains(StringUtils.SPACE)
                )
                .filter(
                        response -> {
                            // .으로 시작하지 않고 .으로 끝나는가
                            String regex = "([^.]|)*" + inventoryVersion + ".0.*";
                            return Pattern.matches(regex, response.getVersion());
                        }
                ).findAny();
        return any.orElse(new ProductLifecycleRulesVersionResponse());
    }
}
