package io.playce.roro.jpa.entity.k8s;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
@Table(name = "k8s_cluster")
public class  Cluster {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long k8sClusterId;

    @Column
    private Long projectId;

    @Column
    private String name;

    @Column
    private String config;

    @Column
    private Long lastClusterScanId;

    @Column
    private Long registerUserId;

    @Column
    private String registerUserLoginId;

    @Column
    private Date registerDatetime;

    @Column
    private Date modifyDatetime;

    @Column
    private Long modifyUserId;

    @Column
    private String modifyUserLoginId;
}
