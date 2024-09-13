package io.playce.roro.common.dto.k8s;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClusterRequest {
    private String name;
    private String config;
}
