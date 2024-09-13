package io.playce.roro.db.asmt.mssql.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString
public class User {

    private String name;
    private String typeDesc;
    private String defaultDatabaseName;
    private String defaultLanguageName;
    private String classDesc;
    private String permissionName;
    private String stateDesc;
    private String serverRoleName;
    private Date createDate;

}
