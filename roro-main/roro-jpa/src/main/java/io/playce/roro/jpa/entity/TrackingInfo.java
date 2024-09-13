package io.playce.roro.jpa.entity;

import io.playce.roro.common.enums.TrackingKey;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;

@Entity
@Getter
@Setter
public class TrackingInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long trackingInfoId;

    @Enumerated(EnumType.STRING)
    private TrackingKey trackingKey;

    private String content;

    private Long inventoryProcessId;

    public void setContent(String content) {
        if (StringUtils.isEmpty(content) || "null".equals(content) || "\"\"".equals(content)) {
            this.content = StringUtils.EMPTY;
        } else {
            this.content = content;
        }
    }
}
