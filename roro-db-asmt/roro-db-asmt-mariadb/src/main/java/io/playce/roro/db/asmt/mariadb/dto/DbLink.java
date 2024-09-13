package io.playce.roro.db.asmt.mariadb.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DbLink {

    private String serverName;
    private String host;
    private String dbName;
    private String userName;
    private Integer port;
    private String wrapper;

}
