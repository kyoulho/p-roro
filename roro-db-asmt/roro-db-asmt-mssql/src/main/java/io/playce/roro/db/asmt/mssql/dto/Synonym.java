package io.playce.roro.db.asmt.mssql.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString
public class Synonym {

    private String databaseName;
    private String schemaName;
    private String synonymName;
    private String typeDesc;
    private String baseObjectName;
    private Boolean isMsShipped;
    private Boolean isPublished;
    private Boolean isSchemaPublished;
    private Date createDate;
    private Date modifyDate;

}
