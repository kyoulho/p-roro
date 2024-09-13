package io.playce.roro.db.asmt.postgresql.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class User {

    private String username;
    private String roles;
    private String useconfig;

}
