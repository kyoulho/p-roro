package io.playce.roro.db.asmt.oracle.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MaterializedView {

    private String owner;
    private String mviewName;
    private String containerName;
    private String updatable;
    private String rewriteEnabled;
    private String rewriteCapability;
    private String refreshMode;
    private String refreshMethod;
    private String buildMode;
    private String fastRefreshable;
    private String staleness;
    private String compileState;
    private String ddlScript;

}
