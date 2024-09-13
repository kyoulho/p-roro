package io.playce.roro.common.dto.trackinginfo;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Date;

@Getter
@Setter
public class TrackingInfoWidgetDto {
    private Long inventoryId;
    private String inventoryName;
    private int changeCount;
    private Date lastScanDate;
}
