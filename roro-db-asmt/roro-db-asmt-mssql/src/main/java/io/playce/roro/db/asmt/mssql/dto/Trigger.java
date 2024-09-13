package io.playce.roro.db.asmt.mssql.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString
public class Trigger {

    private String databaseName;
    private String schemaName;
    private String triggerName;
    private String parentClassDesc;
    private String typeDesc;
    private Boolean isMsShipped;
    private Boolean isDisabled;
    private Boolean isNotForReplication;
    private Boolean isInsteadOfTrigger;
    private Date createDate;
    private Date modifyDate;
    private String ddlScript;

}
