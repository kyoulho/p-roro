package io.playce.roro.db.asmt.oracle.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Segment {

    private long cluster;
    private long index;
    private long indexPartition;
    private long lobPartition;
    private long lobIndex;
    private long lobSegment;
    private long nestedTable;
    private long rollback;
    private long table;
    private long tablePartition;
    private long tableSubPartition;
    private long type2Undo;
    private long totalSizeMb;

}
