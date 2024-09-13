package io.playce.roro.db.asmt.tibero.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Index {

    private String indexName;
    private String indexType;
    private String tableOwner;
    private String tableName;
    private String uniqueness;
    private String tablespaceName;
    private String status;
    private long numRows;
    private String partitioned;
    private String ddlScript;
    @JsonProperty("sizeMB")
    private float sizeMb;

}