package io.playce.roro.db.asmt.sybase.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@Builder
@ToString
public class SybaseDto {

    private Instance instance;
    private List<Server> servers;
    private List<Memory> memories;
    private List<Device> devices;
    private List<Segment> segments;
    private List<User> users;
    private List<Job> jobs;

    private List<Database> databases;

}
