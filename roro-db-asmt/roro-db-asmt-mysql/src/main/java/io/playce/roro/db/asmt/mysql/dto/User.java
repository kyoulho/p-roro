package io.playce.roro.db.asmt.mysql.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class User {

    private String host;
    private String username;
    private String authType;

}
