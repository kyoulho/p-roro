package io.playce.roro.jpa.entity.k8s;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Table(name = "OBJECT")
public class K8sObject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long objectId;
    private String kind;
    private String name;
    private String uid;
    private Long clusterScanId;
    private Long parentObjectId;
}
