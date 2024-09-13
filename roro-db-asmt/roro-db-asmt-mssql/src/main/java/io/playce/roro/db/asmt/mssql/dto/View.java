package io.playce.roro.db.asmt.mssql.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString
public class View {

    private String databaseName;
    private String schemaName;
    private String viewName;
    private Boolean isMsShipped;
    private Boolean isPublished;
    private Boolean isSchemaPublished;
    private Boolean isReplicated;
    private Boolean hasReplicationFilter;
    private Boolean hasOpaqueMetadata;
    private Boolean hasUncheckedAssemblyData;
    private Boolean withCheckOption;
    private Boolean isDateCorrelationView;
    private Boolean isTrackedByCdc;
    private Date createDate;
    private Date modifyDate;
    private String ddlScript;

}