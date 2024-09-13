package io.playce.roro.common.dto.prerequisite;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

public class PrerequisiteDto {
    @Setter
    @Getter
    public static class PrerequisiteResponse {
        @Schema(example = "1", description = "server inventory id")
        private Long serverInventoryId;
        @Schema(example = "Sample Server", description = "server inventory name")
        private String serverInventoryName;
        @Schema(example = "CMPL", description = "inventory process result code")
        private String inventoryProcessResultCode;
        @Schema(example = "ip address", description = "server ip address")
        private String representativeIpAddress;
        @Schema(example = "22", description = "server ssh port")
        private Integer connectionPort;
        private String userName;
        private ServerResult.PrerequisiteJson result;
    }

    @Setter
    @Getter
    public static class PrerequisiteHistoryResponse {
        @Schema(example = "1", description = "inventory process group id")
        private Long inventoryProcessGroupId;
        @Schema(example = "1", description = "inventory process id")
        private Long inventoryProcessId;
        @Schema(example = "SVR", description = "inventory type code")
        private String inventoryTypeCode;
        @Schema(example = "LINUX", description = "inventory detail type code")
        private String inventoryDetailTypeCode;
        @Schema(example = "1", description = "inventory id")
        private Long inventoryId;
        @Schema(example = "Sample", description = "inventory name")
        private String inventoryName;
        @Schema(example = "ip address", description = "ip address")
        private String representativeIpAddress;
        @Schema(example = "22", description = "ssh port")
        private Integer connectionPort;
        private String userName;
        private ServerResult.PrerequisiteJson result;
    }
}
