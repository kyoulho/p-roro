package io.playce.roro.common.dto.hostscan;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
public class RegisteredServerDto {
    @Schema(title = "Server_Inventory ID", description = "서버_인벤토리 ID")
    private Long serverInventoryId;
    @Schema(title = "Inventory Name", description = "인벤토리 이름")
    private String inventoryName;
    @Schema(title = "Connection Port", description = "연결 포트")
    private Integer connectionPort;
}
