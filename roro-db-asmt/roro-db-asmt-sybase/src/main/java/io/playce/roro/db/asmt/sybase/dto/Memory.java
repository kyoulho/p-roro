package io.playce.roro.db.asmt.sybase.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Memory {

    private String parameterName;
    private String defaultValue;
    private String memoryUsed;
    private String configValue;
    private String runValue;

}