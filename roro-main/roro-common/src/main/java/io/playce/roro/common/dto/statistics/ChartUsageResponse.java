package io.playce.roro.common.dto.statistics;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ChartUsageResponse {

    private long serverInventoryId;
    private String serverInventoryName;
    private int utilization;
    private String device;
    private String partition;

}
