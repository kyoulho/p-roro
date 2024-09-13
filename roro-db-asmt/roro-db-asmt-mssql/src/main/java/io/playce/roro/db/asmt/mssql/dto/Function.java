package io.playce.roro.db.asmt.mssql.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString
public class Function {

    private String databaseName;
    private String schemaName;
    private String functionName;
    private String typeDesc;
    private Boolean isMsShipped;
    private Boolean isPublished;
    private Boolean isSchemaPublished;
    private Date createDate;
    private Date modifyDate;
    private String ddlScript;

}