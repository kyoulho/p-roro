package io.playce.roro.db.asmt.oracle.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DataFile {

    private int fileId;
    private String fileName;
    private long sizeMb;
    private String tablespaceName;
    private long freeMb;
    private long maxSizeMb;
    private long maxFreeMb;
    private long freePct;
    private long usedPct;
    private String autoExtensible;
    private long incrementBy;
    private String status;
    private String onlineStatus;

}
