package io.playce.roro.db.asmt.tibero.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Type {

    private String typeName;
    private String typeCode;
    private long attributes;
    private long methods;
    private String predefined;
    private String incomplete;
    private String finalFlag;
    private String instantiable;
    private String ddlScript;

}