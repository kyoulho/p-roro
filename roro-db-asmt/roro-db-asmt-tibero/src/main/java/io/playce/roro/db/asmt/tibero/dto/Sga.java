package io.playce.roro.db.asmt.tibero.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Sga {

    private String name;
    @JsonProperty("totalSizeMB")
    private Long totalSizeMb;
    @JsonProperty("usedSizeMB")
    private Long usedSizeMb;

}