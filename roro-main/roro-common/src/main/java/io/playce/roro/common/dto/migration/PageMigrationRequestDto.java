package io.playce.roro.common.dto.migration;

import io.playce.roro.common.dto.common.PageRequestDto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class PageMigrationRequestDto extends PageRequestDto {

    private String startDate;
    private String endDate;
    private String targetPlatform;
    private String migrationStatus;

}
