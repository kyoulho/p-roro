package io.playce.roro.common.dto.inventory.thirdparty;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.playce.roro.common.dto.inventory.service.Service;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@ToString
public class DiscoveredThirdPartyResponse {

    private String thirdPartySolutionName;
    private int discoveredThirdSolutionCount;
    private List<DiscoveredThirdPartyInventory> discoveredThirdPartyInventories;

    @Getter
    @Setter
    @ToString
    public static class DiscoveredThirdPartyInventory {
        private Long projectId;
        private Long inventoryId;
        private String inventoryTypeCode;
        private String sourceName;
        private Long serverInventoryId;
        private String serverName;
        private String serverIp;
        private List<Service> services;
        private Date discoveredDatetime;
        private List<DiscoveryType> discoveryTypes;
    }

    @Getter
    @Setter
    @ToString
    public static class DiscoveryType {
        private String searchType;
        private List<String> findContents;

        @JsonIgnore
        private int displayOrder;
    }
}
