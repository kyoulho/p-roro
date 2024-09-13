package io.playce.roro.db.asmt.oracle.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class PackageBody {

    private String owner;
    private String packageBodyName;
    private String status;
    private String ddlScript;

}
