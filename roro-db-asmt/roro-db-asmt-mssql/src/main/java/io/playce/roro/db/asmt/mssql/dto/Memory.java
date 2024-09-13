package io.playce.roro.db.asmt.mssql.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Memory {

    @JsonProperty("totalPhysicalMemoryKB")
    private long totalPhysicalMemoryKb;
    @JsonProperty("availablePhysicalMemoryKB")
    private long availablePhysicalMemoryKb;
    @JsonProperty("totalPageFileKB")
    private long totalPageFileKb;
    @JsonProperty("availablePageFileKB")
    private long availablePageFileKb;
    @JsonProperty("systemCacheKB")
    private long systemCacheKb;
    @JsonProperty("kernelPagedPoolKB")
    private long kernelPagedPoolKb;
    @JsonProperty("kernelNonpagedPoolKB")
    private long kernelNonpagedPoolKb;
    private long systemHighMemorySignalState;
    private long systemLowMemorySignalState;
    private String systemMemoryStateDesc;

}
