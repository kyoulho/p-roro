package io.playce.roro.db.asmt.sybase.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString
public class Job {

    private String jobName;
    private String jobDescription;
    private String jobOwner;
    private String jobUproperties;
    private Date jobCreated;
    private String schedName;
    private String schedDescription;
    private String schedOwner;
    private String schedIntervalUnits;
    private Integer schedInterval;
    private Date schedCreated;
}