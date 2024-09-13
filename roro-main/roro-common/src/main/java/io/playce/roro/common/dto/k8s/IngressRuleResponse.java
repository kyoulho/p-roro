package io.playce.roro.common.dto.k8s;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IngressRuleResponse {
    private String path;
    private String targetServiceId;
    private String targetServiceName;
}
