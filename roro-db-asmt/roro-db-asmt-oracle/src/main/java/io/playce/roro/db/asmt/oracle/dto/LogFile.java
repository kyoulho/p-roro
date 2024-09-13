package io.playce.roro.db.asmt.oracle.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class LogFile {

    private int group;
    private String name;
    private long sizeMb;
    private String status;
    private String archived;
    private String type;
    private String rdf;
    private long sequence;
    private long firstChange;

}
