package io.playce.roro.db.asmt.postgresql.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Trigger {

    private String schemaName;
    private String tableName;
    private String triggerName;
    private String event;
    private String condition;
    private String ddlScript;

}
