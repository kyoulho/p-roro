package io.playce.roro.db.asmt.tibero.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TableSpace {

    private String tablespaceName;
    private long blockSize;
    private String extentManagement;
    private String allocationType;
    private String segmentSpaceManagement;
    private String status;
    private String ddlScript;

}