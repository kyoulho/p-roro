package io.playce.roro.jpa.entity.k8s;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Setter @Getter
public class IngressRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ingressRuleId;
    private String host;
    private String secretName;
    private Long objectId;
}
