package io.playce.roro.db.asmt.oracle.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Synonym {

    private String owner;
    private String synonymName;
    private String tableOwner;
    private String tableName;
    private String dbLink;
    private String ddlScript;

}
