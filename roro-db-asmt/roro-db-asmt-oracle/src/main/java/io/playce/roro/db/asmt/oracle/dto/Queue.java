package io.playce.roro.db.asmt.oracle.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Queue {

    private String owner;
    private String name;
    private String queueTable;
    private String queueType;
    private long maxRetries;
    private String enqueueEnabled;
    private String dequeueEnabled;
    private long retention;
    private String userComment;
    private String ddlScript;

}
