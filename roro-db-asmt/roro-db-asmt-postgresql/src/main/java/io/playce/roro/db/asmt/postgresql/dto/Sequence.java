package io.playce.roro.db.asmt.postgresql.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Sequence {

    private String schemaName;
    private String sequenceName;
    private String minimumValue;
    private String maximumValue;
    private Long increment;

}
