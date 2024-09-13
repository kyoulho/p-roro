package io.playce.roro.db.asmt.tibero.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Function {

    private String functionName;
    private String ddlScript;
    private String status;

}