package io.playce.roro.db.asmt.mssql.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Table {

    private String databaseName;
    private String schemaName;
    private String tableName;
    private int rowCount;
    @JsonProperty("totalSpaceKB")
    private float totalSpaceKb;
    @JsonProperty("totalSpaceMB")
    private float totalSpaceMb;
    @JsonProperty("usedSpaceKB")
    private float usedSpaceKb;
    @JsonProperty("usedSpaceMB")
    private float usedSpaceMb;
    @JsonProperty("unusedSpaceKB")
    private float unusedSpaceKb;
    @JsonProperty("unusedSpaceMB")
    private float unusedSpaceMb;

}