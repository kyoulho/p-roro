package io.playce.roro.db.asmt.postgresql.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Table {

    private String schemaName;
    private String tableName;
    private String tableType;
    private Integer rowCount;
    private String totalTableSize;
    private String ddlScript;

}
