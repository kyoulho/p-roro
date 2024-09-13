package io.playce.roro.db.asmt.oracle.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@ToString
public class User {

    private String username;
    private List<String> roles;
    private String accountStatus;
    private Date lockDate;
    private Date expiryDate;
    private String defaultTablespace;
    private String temporaryTablespace;
    private Date created;
    private String profile;

}
