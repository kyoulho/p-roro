package io.playce.roro.db.asmt.postgresql.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Function {

    private String schemaName;
    private String functionName;
    private String args;
    private String returnType;
    private String ddlScript;

}
