package io.playce.roro.common.dto.k8s;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class Cluster {
    private Long k8sClusterId;
    private String name;
    private String scanStatus;
    private Long lastClusterScanId;
    private Long registerUserId;
    private String registerUserLoginId;
    private Date registerDatetime;

    private Date modifyDatetime;
    private Long modifyUserId;
    private String modifyUserLoginId;
}
