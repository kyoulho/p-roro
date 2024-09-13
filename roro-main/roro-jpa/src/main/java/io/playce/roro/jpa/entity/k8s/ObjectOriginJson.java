package io.playce.roro.jpa.entity.k8s;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity @Getter @Setter
public class ObjectOriginJson {
    @Id
    private Long objectId;
    private String json;
}
