package io.playce.roro.common.dto.k8s;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class ClusterResponse {
    private Long k8sClusterId;
    private String name;
    private String serverVersion;
    private String scanStatus;
    private String scanStatusMessage;
    private Long lastClusterScanId;

    private Long registerUserId;
    private String registerUserLoginId;
    private Date registerDatetime;

    private Date modifyDatetime;
    private String modifyUserLoginId;
    private Long modifyUserId;
}
