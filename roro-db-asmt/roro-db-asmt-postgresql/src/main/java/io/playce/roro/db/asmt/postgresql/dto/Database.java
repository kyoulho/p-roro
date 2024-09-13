package io.playce.roro.db.asmt.postgresql.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class Database {

    private String name;
    private String owner;
    private String encoding;
    private String collation;
    private String ctype;
    private String dbSize;
    private String tableSpace;
    private String description;
    private String searchPath;

    private List<Table> tables;
    private List<View> views;
    private List<Index> indexes;
    private List<Procedure> procedures;
    private List<Function> functions;
    private List<Sequence> sequences;
    private List<Trigger> triggers;

}
