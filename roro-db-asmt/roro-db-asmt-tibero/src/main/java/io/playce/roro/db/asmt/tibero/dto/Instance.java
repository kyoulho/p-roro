package io.playce.roro.db.asmt.tibero.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString
public class Instance {

    private String hostName;
    private String version;
    private String status;
    private String tipFile;
    private Date startupTime;
    @JsonProperty("dbSizeMB")
    private Long dbSizeMb;
}