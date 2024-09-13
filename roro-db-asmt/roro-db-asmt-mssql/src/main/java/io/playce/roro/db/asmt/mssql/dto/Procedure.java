package io.playce.roro.db.asmt.mssql.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString
public class Procedure {

    private String databaseName;
    private String schemaName;
    private String procedureName;
    private String typeDesc;
    private Boolean isMsShipped;
    private Boolean isPublished;
    private Boolean isSchemaPublished;
    private Boolean isAutoExecuted;
    private Boolean isExecutionReplicated;
    private Boolean isReplSerializableOnly;
    private Boolean skipsReplConstraints;
    private Date createDate;
    private Date modifyDate;
    private String ddlScript;

}
