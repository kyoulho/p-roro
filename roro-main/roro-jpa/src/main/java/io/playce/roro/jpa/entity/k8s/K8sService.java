package io.playce.roro.jpa.entity.k8s;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "SERVICE")
@Getter
@Setter
public class K8sService {
    @Id
    private Long objectId;
    private String clusterIp;
    private String type;
    private String name;
}
