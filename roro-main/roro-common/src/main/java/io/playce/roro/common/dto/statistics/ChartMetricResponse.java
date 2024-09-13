package io.playce.roro.common.dto.statistics;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ChartMetricResponse {

    private String metric;
    private int count;

}
