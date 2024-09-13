package io.playce.roro.db.asmt.mssql.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Index {

    private String databaseName;
    private String schemaName;
    private String tableName;
    private String indexName;
    private String typeDesc;
    @JsonProperty("reservedSizeMB")
    private float reservedSizeMb;
    private int rowCount;
    private String datafileName;

}
