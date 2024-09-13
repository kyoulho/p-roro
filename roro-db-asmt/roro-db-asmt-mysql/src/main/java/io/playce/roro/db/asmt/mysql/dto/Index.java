package io.playce.roro.db.asmt.mysql.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Index {

    private String databaseName;
    private String tableName;
    private String indexName;
    private String indexColumn;
    private String indexType;
    private String uniqueness;
    private int seqInIndex;

}
