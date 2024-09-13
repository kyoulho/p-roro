package io.playce.roro.db.asmt.mariadb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TableDataUsage {

    private String databaseName;
    private String tableName;
    @JsonProperty("totalSizeMB")
    private float totalSizeMb;
    @JsonProperty("dataSizeMB")
    private float dataSizeMb;
    @JsonProperty("indexSizeMB")
    private float indexSizeMb;

}
