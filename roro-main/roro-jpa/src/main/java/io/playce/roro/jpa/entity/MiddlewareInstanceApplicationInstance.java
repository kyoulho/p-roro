package io.playce.roro.jpa.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "middleware_instance_application_instance")
@Setter @Getter
public class MiddlewareInstanceApplicationInstance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long middlewareInstanceApplicationInstanceId;
    private Long applicationInstanceId;
    private Long middlewareInstanceId;
    private String contextPath;
    private String autoDeployYn;
    private String reloadableYn;
}