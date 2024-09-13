package io.playce.roro.db.asmt.mariadb.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@Builder
@ToString
public class MariaDbDto {

    private final Instance instance;
    private final List<User> users;
    private final List<DbLink> dbLinks;
    private final List<Database> databases;

}
