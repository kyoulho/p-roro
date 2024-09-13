package io.playce.roro.db.asmt.sybase.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Device {

    private String deviceName;
    private String physicalName;
    private String description;

}