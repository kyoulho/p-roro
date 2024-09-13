package io.playce.roro.db.asmt.oracle.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Sequence {

    private String owner;
    private String sequenceName;
    private String minValue;
    private String maxValue;
    private long incrementBy;
    private String cycleFlag;
    private String orderFlag;
    private long cacheSize;
    private String lastNumber;
    private String ddlScript;

}
