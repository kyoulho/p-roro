package io.playce.roro.db.asmt.mariadb.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Variable {

    private String variableName;
    private String value;

}
