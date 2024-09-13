package io.playce.roro.db.asmt.oracle.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString
public class Job {

    private String owner;
    private String jobName;
    private String jobCreator;
    private String programOwner;
    private String programName;
    private String scheduleOwner;
    private String scheduleName;
    private String scheduleType;
    private Date startDate;
    private String eventQueueOwner;
    private String eventQueueName;
    private String eventQueueAgent;
    private String eventCondition;
    private String eventRule;
    private String state;
    private long runCount;
    private long failureCount;
    private Date lastStartDate;
    private String lastRunDuration;
    private String nlsEnv;
    private String ddlScript;

}
