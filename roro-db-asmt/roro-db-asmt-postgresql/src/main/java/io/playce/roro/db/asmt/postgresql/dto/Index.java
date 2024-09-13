package io.playce.roro.db.asmt.postgresql.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Index {

    private String schemaName;
    private String tableName;
    private String indexName;
    private String ddlScript;

}
