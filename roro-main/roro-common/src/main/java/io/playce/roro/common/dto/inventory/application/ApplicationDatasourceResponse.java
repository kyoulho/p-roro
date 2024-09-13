package io.playce.roro.common.dto.inventory.application;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ApplicationDatasourceResponse {

    private Long projectId;
    private Long databaseInventoryId;
    private Long databaseInstanceId;
    private String datasourceName;
    private String connectionUrl;
    private String userName;

}
