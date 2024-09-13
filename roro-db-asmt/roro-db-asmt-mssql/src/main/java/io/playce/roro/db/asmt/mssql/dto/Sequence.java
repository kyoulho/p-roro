package io.playce.roro.db.asmt.mssql.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString
public class Sequence {

    private String databaseName;
    private String schemaName;
    private String sequenceName;
    private String typeDesc;
    private Boolean isMsShipped;
    private Boolean isPublished;
    private Boolean isSchemaPublished;
    private Long startValue;
    private Long increment;
    private Long minimumValue;
    private Long maximumValue;
    private Boolean isCycling;
    private Boolean isCached;
    private Integer cacheSize;
    private Integer systemTypeId;
    private Integer userTypeId;
    private Integer precision;
    private Integer scale;
    private Long currentValue;
    private Boolean isExhausted;
    private Date createDate;
    private Date modifyDate;

}