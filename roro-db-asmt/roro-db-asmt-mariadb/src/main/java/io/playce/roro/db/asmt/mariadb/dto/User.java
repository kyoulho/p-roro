package io.playce.roro.db.asmt.mariadb.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @JsonIgnore
    private String priv;
}
