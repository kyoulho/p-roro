package io.playce.roro.db.asmt.oracle.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Package {

    private String owner;
    private String packageName;
    private String status;
    private String ddlScript;

}
