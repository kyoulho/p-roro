package io.playce.roro.db.asmt.tibero.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Trigger {

    private String triggerName;
    private String triggerType;
    private String triggeringEvent;
    private String tableOwner;
    private String tableName;
    private String referencingNames;
    private String ddlScript;
    private String whenClause;
    private String status;

}
