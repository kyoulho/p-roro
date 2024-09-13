package io.playce.roro.db.asmt.oracle.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Function {

    private String owner;
    private String functionName;
    private String status;
    private String ddlScript;

}
