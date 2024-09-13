package io.playce.roro.jpa.entity.pk;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DiscoveredHostId implements Serializable {
    private static final long serialVersionUID = 7917086383966639528L;

    @Column(name = "SCAN_HISTORY_ID", nullable = false)
    private Long scanHistoryId;

    @Column(name = "IP_ADDRESS", nullable = false)
    private String ipAddress;
}
