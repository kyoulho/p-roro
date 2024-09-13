package io.playce.roro.db.asmt.tibero.dto;

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
    private String openMode;
    private String logMode;
    private Date createDate;

    private ObjectSummary objectSummary;
    private List<Table> tables;
    private List<View> views;
    private List<MaterializedView> materializedViews;
    private List<Index> indexes;
    private List<Procedure> procedures;
    private List<Package> packages;
    private List<PackageBody> packageBodies;
    private List<Function> functions;
    private List<Queue> queues;
    private List<Trigger> triggers;
    private List<Type> types;
    private List<Sequence> sequences;
    private List<Synonym> synonyms;
    private List<Job> jobs;

}