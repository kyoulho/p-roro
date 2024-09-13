package io.playce.roro.db.asmt.oracle.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Table {

    private String owner;
    private String tableName;
    private String tablespaceName;
    private String status;
    private long numRows;
    private long blocks;
    private String partitioned;
    private String ddlScript;
    private String privilege;
    @JsonProperty("sizeMB")
    private float sizeMb;

}