package io.playce.roro.db.asmt.mariadb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Table {

    private String databaseName;
    private String tableName;
    private String engine;
    private long tableRows;
    @JsonProperty("tableSizeMB")
    private float tableSizeMb;
    private String ddlScript;

}
