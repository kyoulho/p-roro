package io.playce.roro.db.asmt.postgresql.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class View {

    private String schemaName;
    private String viewName;
    private String ddlScript;

}
