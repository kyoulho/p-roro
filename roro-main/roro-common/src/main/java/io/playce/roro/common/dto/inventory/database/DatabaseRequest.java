package io.playce.roro.common.dto.inventory.database;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class DatabaseRequest {

    private String databaseInventoryName;
    private String customerInventoryCode;
    private String customerInventoryName;
    private List<Long> serviceIds;
    private Long serverInventoryId;

    private String vendor;
    private String inventoryDetailTypeCode;  // Engine Name
    private String engineVersion;
    private Integer connectionPort;
    private String databaseServiceName;
    private String jdbcUrl;
    private String allScanYn;
    private String userName;
    private String password;
    private String databaseAccessControlSystemSolutionName;
    private List<Long> labelIds;
    private String description;

    private Long discoveredInstanceId;

}
