package io.playce.roro.db.asmt.tibero.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Queue {

    private String name;
    private String queueTable;
    private String queueType;
    private long maxRetries;
    private String enqueueEnabled;
    private String dequeueEnabled;
    private long retention;
    private String userComment;

}