package io.playce.roro.common.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
public class ChartInterval {

    private Long serverId;
    private Date datetime;

}
