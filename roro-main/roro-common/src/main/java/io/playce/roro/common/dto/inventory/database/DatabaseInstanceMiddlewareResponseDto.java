package io.playce.roro.common.dto.inventory.database;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DatabaseInstanceMiddlewareResponseDto {

    private Long projectId;
    private Long middlewareInventoryId;
    private Long middlewareInstanceId;
    private String middlewareInstanceName;
    private String middlewareInstancePath;
    private String middlewareInstanceServicePort;
    private String runningUser;
    private String activeYn;

}
