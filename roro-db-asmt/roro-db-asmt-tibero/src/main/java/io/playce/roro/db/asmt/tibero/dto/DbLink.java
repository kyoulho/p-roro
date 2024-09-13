package io.playce.roro.db.asmt.tibero.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class DbLink {

    public String owner;
    public String dbLink;
    public String username;
    public String host;
    public Date created;

}