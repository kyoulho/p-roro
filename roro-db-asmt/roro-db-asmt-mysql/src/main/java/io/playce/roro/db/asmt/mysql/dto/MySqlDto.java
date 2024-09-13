package io.playce.roro.db.asmt.mysql.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@Builder
@ToString
public class MySqlDto {

    private final Instance instance;
    private final List<User> users;
    private final List<DbLink> dbLinks;
    private final List<Database> databases;

}
