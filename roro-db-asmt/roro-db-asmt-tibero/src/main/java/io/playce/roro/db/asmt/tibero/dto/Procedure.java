package io.playce.roro.db.asmt.tibero.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Procedure {

    private String procedureName;
    private String ddlScript;
    private String status;

}