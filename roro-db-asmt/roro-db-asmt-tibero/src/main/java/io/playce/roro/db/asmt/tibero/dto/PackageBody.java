package io.playce.roro.db.asmt.tibero.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PackageBody {

    private String packageBodyName;
    private String ddlScript;
    private String status;

}