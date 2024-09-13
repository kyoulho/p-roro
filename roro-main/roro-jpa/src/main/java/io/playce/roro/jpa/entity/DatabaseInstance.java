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
@Table(name = "database_instance")
@ToString
public class DatabaseInstance {

    @Id
    private Long databaseInstanceId;

    @Column
    private String databaseServiceName;

    @Column
    private String jdbcUrl;

    @Column
    private String userName;

    @Column
    private int tableCount;

    @Column
    private int viewCount;

    @Column
    private int functionCount;

    @Column
    private int procedureCount;

    @Column
    private Long registUserId;

    @Column
    private Date registDatetime;

}
