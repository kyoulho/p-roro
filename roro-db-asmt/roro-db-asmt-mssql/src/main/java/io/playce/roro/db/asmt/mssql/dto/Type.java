package io.playce.roro.db.asmt.mssql.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Type {

    private String databaseName;
    private String schemaName;
    private String name;
    private String type;
    private Integer maxLength;
    private Integer precision;
    private Integer scale;
    private String collationName;
    private Boolean isNullable;
    private Boolean isAssemblyType;
    private Integer defaultObjectId;
    private Integer ruleObjectId;
    private Boolean isTableType;

}