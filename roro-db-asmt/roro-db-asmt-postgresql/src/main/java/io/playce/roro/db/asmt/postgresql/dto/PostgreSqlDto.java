package io.playce.roro.db.asmt.postgresql.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@Builder
@ToString
public class PostgreSqlDto {

    private final Instance instance;
    private final List<User> users;
    private final List<Database> databases;

}
