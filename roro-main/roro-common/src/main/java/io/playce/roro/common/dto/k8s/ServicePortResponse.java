package io.playce.roro.common.dto.k8s;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServicePortResponse {
    private String name;
    private Integer port;
    private String protocol;
    private String targetPort;
    private Integer nodePort;
}