package io.playce.roro.jpa.entity;

import io.playce.roro.jpa.entity.pk.TopologyNodePositionId;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "topology_node_position")
public class TopologyNodePosition {
    @EmbeddedId
    private TopologyNodePositionId id;

    @Lob
    @Column(name = "CONFIG_CONTENTS")
    private String configContents;

    @Column(name = "USER_ID")
    private Long userId;

}