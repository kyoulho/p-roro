package io.playce.roro.common.dto.statistics;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ChartData {

    @JsonIgnore
    private Long serverInventoryId;

    private int value;
    private Date time;

}
