package io.playce.roro.db.asmt.oracle.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString
public class DbLink {

    public String owner;
    public String dbLink;
    public String username;
    public String host;
    public Date created;
    public String ddlScript;

}
