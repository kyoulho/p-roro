package io.playce.roro.jpa.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Getter
@Setter
@Table(name = "database_summary")
@ToString
public class DatabaseSummary {

    @Id
    private Long databaseInventoryId;

    @Column
    private String hostName;

    @Column
    private String version;

    @Column
    private Long dbSizeMb;

    @Column
    private Date startupDatetime;

}
