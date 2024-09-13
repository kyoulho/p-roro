package io.playce.roro.common.dto.inventory.database;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DatabaseInstanceApplicationResponseDto {

    private Long projectId;
    private Long applicationInventoryId;
    private String applicationName;
    private String deployPath;
    private String contextPath;
    private String reloadableYn;
    private String autoDeployYn;

}
