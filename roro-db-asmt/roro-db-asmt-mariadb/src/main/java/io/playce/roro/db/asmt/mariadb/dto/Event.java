package io.playce.roro.db.asmt.mariadb.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString
public class Event {

    private String databaseName;
    private String eventName;
    private String eventDefinition;
    private String eventType;
    private String interval;
    private Date executeTime;
    private Date starts;
    private Date ends;
    private String status;
    private Date created;

}
