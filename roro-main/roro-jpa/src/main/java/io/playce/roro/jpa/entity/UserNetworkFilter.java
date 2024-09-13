package io.playce.roro.jpa.entity;

import io.playce.roro.jpa.entity.pk.UserNetworkFilterId;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "user_network_filter")
@Setter @Getter
public class UserNetworkFilter {
    @EmbeddedId
    private UserNetworkFilterId id;

    @Lob
    @Column(name = "WHITELIST")
    private String whitelist;

    @Lob
    @Column(name = "BLACKLIST")
    private String blacklist;

    @Lob
    @Column(name = "HIDE_NODES")
    private String hideNodes;

    @Column(name = "NETWORK_FILTER_ID", nullable = false)
    private Long networkFilterId;
}