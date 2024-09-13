package io.playce.roro.common.dto.inventory.thirdparty;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class DiscoveredThirdPartyDto {

    private Long thirdPartySolutionId;
    private String thirdPartySolutionName;
    private Long projectId;
    private Long inventoryId;
    private String inventoryTypeCode;
    private String sourceName;
    private Long serverInventoryId;
    private String serverName;
    private String serverIp;
    private String searchType;
    private String findContents;
    private Date modifyDatetime;

}
