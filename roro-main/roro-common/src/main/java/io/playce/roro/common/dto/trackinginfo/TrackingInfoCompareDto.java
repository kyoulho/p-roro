package io.playce.roro.common.dto.trackinginfo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TrackingInfoCompareDto {

    private String changeType;
    private String property;
    private Object selected;
    private Object compare;

}
