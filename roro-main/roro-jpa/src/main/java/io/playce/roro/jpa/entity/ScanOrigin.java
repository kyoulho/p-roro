package io.playce.roro.jpa.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Setter @Getter
public class ScanOrigin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long scanOriginId;
    private Long clusterScanId;
    private String originData;
    private String commandKey;
}
