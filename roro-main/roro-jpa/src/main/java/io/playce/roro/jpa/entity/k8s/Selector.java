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
public class Selector {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long selectorId;
    private String name;
    private String value;
    private Long objectId;
}
