package io.playce.roro.db.asmt.mssql.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DataFile {

    private long databaseId;
    private long fileId;
    private String typeDesc;
    private long dataSpaceId;
    private String name;
    private String physicalName;
    private String stateDesc;
    @JsonProperty("allocSizeMB")
    private long allocSizeMb;
    @JsonProperty("emptySpaceMB")
    private long emptySpaceMb;
    private long maxSize;
    private long growth;
    private Boolean isMediaReadOnly;
    private Boolean isReadOnly;
    private Boolean isSparse;
    private Boolean isPercentGrowth;
    private Boolean isNameReserved;

}
