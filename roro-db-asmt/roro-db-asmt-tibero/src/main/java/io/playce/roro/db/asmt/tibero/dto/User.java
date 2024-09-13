package io.playce.roro.db.asmt.tibero.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
public class User {

    private String username;
    private List<String> roles;
    private String accountStatus;
    private Date lockDate;
    private Date expiryDate;
    private String defaultTablespace;
    private String defaultTempTablespace;
    private Date created;
    private String profile;
    private String authenticationType;

}