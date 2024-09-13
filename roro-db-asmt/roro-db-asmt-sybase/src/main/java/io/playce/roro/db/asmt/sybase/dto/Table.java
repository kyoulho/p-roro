package io.playce.roro.db.asmt.sybase.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString
public class Table {

    private String databaseName;
    private String owner;
    private String tableName;
    private int rowCount;
    private int pageSize;
    @JsonProperty("pageSizeKB")
    private int pageSizeKb;
    private Date created;

}