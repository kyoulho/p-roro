package io.playce.roro.common.dto.insights;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Resource {
    private Long inventoryId;
    private String resourceName;
    private String resourceType;
    private String resourceIp;
}