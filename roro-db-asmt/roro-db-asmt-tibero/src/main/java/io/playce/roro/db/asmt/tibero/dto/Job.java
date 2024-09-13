package io.playce.roro.db.asmt.tibero.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class Job {

    private String jobName;
    private String jobStyle;
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

}