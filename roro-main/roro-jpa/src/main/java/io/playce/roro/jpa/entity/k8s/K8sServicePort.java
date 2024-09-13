package io.playce.roro.jpa.entity.k8s;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "SERVICE_PORT")
@Getter
@Setter
public class K8sServicePort {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long servicePortId;
    private String name;
    @Column(name = "NODEPORT")
    private Integer nodePort;
    private Integer port;
    private String protocol;
    private String targetPort;
    private Long objectId;
}
