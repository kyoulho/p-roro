package io.playce.roro.jpa.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
@Table(name = "cluster_scan")
public class ClusterScan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long clusterScanId;

    @Column
    private String scanStatus;

    @Column
    private String scanStatusMessage;

    @Column
    private Date scanDatetime;

    @Column
    private Long k8sClusterId;

    @Column
    private String serverVersion;
}
