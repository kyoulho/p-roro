package io.playce.roro.db.asmt.mysql.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class View {

    private String databaseName;
    private String viewName;
    private String checkOption;
    private String isUpdatable;
    private String securityType;
    private String ddlScript;

}
