package io.playce.roro.db.asmt.mssql.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ObjectSummary {

    private String databaseName;
    private String schemaName;
    private String objectName;
    private int objectCount;

}
