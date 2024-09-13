package io.playce.roro.db.asmt.sybase.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@ToString
public class Database {

    private long dbid;
    private String name;
    private int dbSize;
    private int dbUsed;
    private int dbUsedPct;
    private String owner;
    private Date created;

    private ObjectSummary objectSummary;
    private List<Table> tables;
    private List<View> views;
    private List<Index> indexes;
    private List<Function> functions;
    private List<Procedure> procedures;
    private List<Trigger> triggers;

}