package io.playce.roro.db.asmt.mssql.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@Builder
@ToString
public class MsSqlDto {

    private final Instance instance;
    private final List<Memory> memories;
    private final List<DataFile> dataFiles;
    private final List<User> users;
    private final List<DbLink> dbLinks;

    private final List<Database> databases;

}
