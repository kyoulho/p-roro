package io.playce.roro.db.asmt.mssql.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    private String windowsVersion;
    private String windowsServicePackLevel;
    private String windowsSku;
    private String version;
    private Date startupTime;
    @JsonProperty("dbSizeMB")
    private Long dbSizeMb;

    @JsonIgnore
    private String productVersion;

}
