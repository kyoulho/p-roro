package io.playce.roro.db.asmt.oracle.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TableSpace {

    private String tablespaceName;
    private long blockSize;
    private String extentManagement;
    private String allocationType;
    private String segmentSpaceManagement;
    private String status;
    private String ddlScript;

}
