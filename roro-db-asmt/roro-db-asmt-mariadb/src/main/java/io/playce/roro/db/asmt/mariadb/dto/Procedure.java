package io.playce.roro.db.asmt.mariadb.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString
public class Procedure {

    private String databaseName;
    private String procedureName;
    private String returnType;
    private String comment;
    private Date created;
    private String ddlScript;

}
