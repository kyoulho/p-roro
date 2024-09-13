package io.playce.roro.db.asmt.mysql.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString
public class Function {

    private String databaseName;
    private String functionName;
    private String returnType;
    private String comment;
    private Date created;
    private String ddlScript;

}