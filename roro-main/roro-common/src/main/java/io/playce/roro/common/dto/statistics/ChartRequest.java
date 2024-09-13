package io.playce.roro.common.dto.statistics;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ChartRequest {

    private String measurementType;
    private long startDatetime;
    private long endDatetime;
    private String limitType;
    private Integer limitCount;

}
