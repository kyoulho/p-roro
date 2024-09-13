package io.playce.roro.jpa.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Getter
@Setter
public class InventoryLifecycleVersionLink {
    @Id
    private Long inventoryId;
    private Long productVersionId;
    private Long javaVersionId;

}
