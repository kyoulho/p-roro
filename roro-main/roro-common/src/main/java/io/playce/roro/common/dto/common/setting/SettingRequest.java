package io.playce.roro.common.dto.common.setting;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SettingRequest {

    private Long settingId;
    private String propertyValue;

}
