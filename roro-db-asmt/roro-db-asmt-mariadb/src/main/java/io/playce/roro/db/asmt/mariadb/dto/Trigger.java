package io.playce.roro.db.asmt.mariadb.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Trigger {

    private String databaseName;
    private String triggerName;
    private String triggerTable;
    private String actionTiming;
    private String triggerEvent;
    private String ddlScript;

}
