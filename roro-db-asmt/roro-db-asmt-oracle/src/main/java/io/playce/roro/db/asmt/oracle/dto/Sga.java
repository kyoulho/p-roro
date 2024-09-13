package io.playce.roro.db.asmt.oracle.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Sga {

    private Long fixedSize;
    private Long variableSize;
    private Long databaseBuffers;
    private Long redoBuffers;
    @JsonProperty("totalSizeMB")
    private Long totalSizeMb;

}
