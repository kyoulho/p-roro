package io.playce.roro.db.asmt.oracle.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Parameter {

    private String name;
    private String type;
    private Object value;
    private Object displayValue;
    private String description;

}
