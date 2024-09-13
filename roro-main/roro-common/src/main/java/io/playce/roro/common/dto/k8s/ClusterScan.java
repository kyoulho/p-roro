package io.playce.roro.common.dto.k8s;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;


@Getter
@Setter
public class ClusterScan {
    private String status;
    private Date scanTime;
    private Long k8sClusterId;
    private String serverVersion;
}
