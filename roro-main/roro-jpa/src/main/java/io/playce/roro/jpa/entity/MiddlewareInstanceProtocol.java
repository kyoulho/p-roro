package io.playce.roro.jpa.entity;

import io.playce.roro.jpa.entity.pk.MiddlewareInstanceProtocolPK;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

@Entity
@Table(name = "middleware_instance_protocol")
@IdClass(MiddlewareInstanceProtocolPK.class)
@Setter @Getter
public class MiddlewareInstanceProtocol {
    @Id
    private Long middlewareInstanceId;
    @Id
    private Integer middlewareInstanceServicePort;
    private String protocol;
}