package io.playce.roro.db.asmt.oracle.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Trigger {

    private String owner;
    private String triggerName;
    private String triggerType;
    private String triggeringEvent;
    private String tableOwner;
    private String baseObjectType;
    private String tableName;
    private String referencingNames;
    private String whenClause;
    private String actionType;
    private String ddlScript;

}
