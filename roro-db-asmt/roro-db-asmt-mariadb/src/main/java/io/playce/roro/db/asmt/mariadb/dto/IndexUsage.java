package io.playce.roro.db.asmt.mariadb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class IndexUsage {

    private String databaseName;
    private String tableName;
    private String indexName;
    @JsonProperty("indexSizeMB")
    private float indexSizeMb;

}
