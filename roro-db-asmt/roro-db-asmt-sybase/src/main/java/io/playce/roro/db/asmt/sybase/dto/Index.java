package io.playce.roro.db.asmt.sybase.dto;

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
    private String indexKeys;
    private String indexDescription;
    private String indexMaxRowsPerPage;
    private String indexFillFactor;
    private String indexReservePageGap;

}