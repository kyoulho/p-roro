package io.playce.roro.db.asmt.tibero.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DataFile {

    private int fileId;
    private String fileName;
    private String tablespaceName;
    @JsonProperty("sizeMB")
    private long sizeMb;
    @JsonProperty("freeMB")
    private long freeMb;
    @JsonProperty("maxSizeMB")
    private long maxSizeMb;
    @JsonProperty("maxFreeMB")
    private long maxFreeMb;
    private long freePct;
    private long usedPct;
    private String autoExtensible;
    private long incrementBy;
    private String status;

}