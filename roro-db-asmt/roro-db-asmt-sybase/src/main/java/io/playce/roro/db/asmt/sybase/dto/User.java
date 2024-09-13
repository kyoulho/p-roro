package io.playce.roro.db.asmt.sybase.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@ToString
public class User {

    @JsonIgnore
    private long suid;
    private String loginName;
    private String dbName;
    private Date lastLoginDate;
    private Date created;
    private String userName;
    private String roles;

}