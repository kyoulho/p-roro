package io.playce.roro.db.asmt.tibero.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Table {

    private String tableName;
    private String tablespaceName;
    private long numRows;
    private long blocks;
    private String partitioned;
    private String ddlScript;
    private String privilege;
    @JsonProperty("sizeMB")
    private float sizeMb;

}