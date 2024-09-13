package io.playce.roro.db.asmt.oracle.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class PublicSynonym {

    public String owner;
    public String synonymName;
    public String tableOwner;
    public String tableName;
    public String dbLink;
    public String ddlScript;

}
