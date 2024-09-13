package io.playce.roro.jpa.entity.k8s;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Setter
@Getter
public class IngressRuleTarget {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ingressRuleHostId;
    private String serviceName;
    private String servicePortName;
    private Integer servicePortNumber;
    private String path;
    private String pathType;
    private Long ingressRuleId;
}
