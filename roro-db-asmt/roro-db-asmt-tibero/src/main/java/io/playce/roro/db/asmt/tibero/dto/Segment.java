package io.playce.roro.db.asmt.tibero.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Segment {

    private long index;
    private long lob;
    private long table;
    private long undo;
    @JsonProperty("totalSizeMB")
    private long totalSizeMb;

}