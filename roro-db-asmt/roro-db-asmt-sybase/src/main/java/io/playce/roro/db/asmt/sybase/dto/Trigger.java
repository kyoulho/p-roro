package io.playce.roro.db.asmt.sybase.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString
public class Trigger {

    private String databaseName;
    private long id;
    private String name;
    private String ddlScript;
    private Date created;

}