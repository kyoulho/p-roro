package io.playce.roro.db.asmt.oracle.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ObjectSummary {

    private String owner;
    private String objectType;
    private Integer objectCount;

}
