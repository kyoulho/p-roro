package io.playce.roro.common.dto.trackinginfo;

import io.playce.roro.common.enums.TrackingKey;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TrackingInfoCompareRawDto {

    private TrackingKey trackingKey;
    private String selected;
    private String compare;

    public void setTrackingKey(String trackingKey) {
        this.trackingKey = TrackingKey.valueOf(trackingKey);
    }
}
