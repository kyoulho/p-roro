package io.playce.roro.db.asmt.tibero.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Sequence {

    private String sequenceName;
    private String minValue;
    private String maxValue;
    private long incrementBy;
    private String cycleFlag;
    private String orderFlag;
    private String ifAvail;
    private long cacheSize;
    private String lastNumber;

}