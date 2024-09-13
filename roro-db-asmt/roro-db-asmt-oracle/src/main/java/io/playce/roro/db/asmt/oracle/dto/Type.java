package io.playce.roro.db.asmt.oracle.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Type {

    private String owner;
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
