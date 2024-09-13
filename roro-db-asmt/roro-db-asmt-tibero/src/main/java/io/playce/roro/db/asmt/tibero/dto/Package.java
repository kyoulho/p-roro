package io.playce.roro.db.asmt.tibero.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Package {

    private String packageName;
    private String ddlScript;
    private String status;

}