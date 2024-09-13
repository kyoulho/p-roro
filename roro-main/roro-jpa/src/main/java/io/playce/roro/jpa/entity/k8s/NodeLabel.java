package io.playce.roro.jpa.entity.k8s;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Getter
@Setter
public class NodeLabel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long nodeLabelId;
    private String name;
    private String value;
    private Long nodeId;
}
