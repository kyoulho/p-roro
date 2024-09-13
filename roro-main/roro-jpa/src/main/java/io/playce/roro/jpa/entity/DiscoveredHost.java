package io.playce.roro.jpa.entity;

import io.playce.roro.jpa.entity.pk.DiscoveredHostId;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

@Table(name = "DISCOVERED_HOST")
@Entity
@Getter
@NoArgsConstructor
public class DiscoveredHost {
    @EmbeddedId
    private DiscoveredHostId discoveredHostId;

    @Column(name = "REPLY_TTL")
    private Integer replyTTl;

    @Column(name = "OS_NAME", nullable = false)
    private String osName;

    @Column(name = "DISCOVERED_DATETIME", nullable = false)
    private Date discoveredDatetime;

    @Builder
    public DiscoveredHost(Long scanHistoryId, String ipAddress, Integer replyTTl, String osName, Date discoveredDatetime) {
        this.discoveredHostId = new DiscoveredHostId(scanHistoryId, ipAddress);
        this.replyTTl = replyTTl;
        this.osName = osName;
        this.discoveredDatetime = discoveredDatetime;
    }
}
