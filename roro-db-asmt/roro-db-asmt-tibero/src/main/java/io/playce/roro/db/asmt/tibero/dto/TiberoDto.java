package io.playce.roro.db.asmt.tibero.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@Builder
@ToString
public class TiberoDto {

    private final Instance instance;
    private final List<Sga> sga;
    private final List<DataFile> dataFiles;
    private final List<ControlFile> controlFiles;
    private final List<LogFile> logFiles;
    private final List<TableSpace> tableSpaces;
    private final List<Parameter> parameters;
    private final Segment segment;
    private final List<User> users;
    private final List<PublicSynonym> publicSynonyms;
    private final List<DbLink> dbLinks;

    private final List<Database> databases;
}
