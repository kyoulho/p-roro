package io.playce.roro.db.asmt.mysql.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class Database {

    private String name;
    private int tableCount;
    @JsonProperty("databaseSizeMB")
    private float databaseSizeMb;

    private List<TableDataUsage> tableDataUsages;
    private List<IndexUsage> indexUsages;
    private List<Table> tables;
    private List<View> views;
    private List<Index> indexes;
    private List<Procedure> procedures;
    private List<Function> functions;
    private List<Trigger> triggers;
    private List<Event> events;

}
