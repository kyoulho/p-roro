package io.playce.roro.db.asmt.mssql.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString
public class Queue {

    private String databaseName;
    private String schemaName;
    private String queueName;
    private String typeDesc;
    private Boolean isMsShipped;
    private Boolean isPublished;
    private Boolean isSchemaPublished;
    private Integer maxReaders;
    private Boolean isActivationEnabled;
    private Boolean isReceiveEnabled;
    private Boolean isEnqueueEnabled;
    private Boolean isRetentionEnabled;
    private Boolean isPoisonMessageHandlingEnabled;
    private Date createDate;
    private Date modifyDate;

}
