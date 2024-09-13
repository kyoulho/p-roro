package io.playce.roro.common.dto.statistics;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class ChartMonitoringResponse {

    private long serverInventoryId;
    private String serverInventoryName;
    private String isExistData;
    private List<ChartData> data;

}
