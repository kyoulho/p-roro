package io.playce.roro.db.asmt.tibero.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class LogFile {

    private int group;
    private String name;
    @JsonProperty("sizeMB")
    private long sizeMb;
    private String status;
    private String archived;
    private String type;
    private long sequence;
    private long firstChange;
    private Date firstTime;

}