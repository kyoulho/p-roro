package io.playce.roro.db.asmt.oracle.dto;

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
    private String databaseStatus;
    private String instanceRole;
    private Date startupTime;
    private Long dbSizeMb;

}
