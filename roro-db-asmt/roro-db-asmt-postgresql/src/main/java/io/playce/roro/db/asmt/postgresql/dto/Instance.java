package io.playce.roro.db.asmt.postgresql.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString
public class Instance {

    private String version;
    private Date startupTime;
    private String searchPath;
    private String archiveCommand;
    private String archiveMode;
    private String archiveTimeout;

}
