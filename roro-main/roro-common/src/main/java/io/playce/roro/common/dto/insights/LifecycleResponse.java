package io.playce.roro.common.dto.insights;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LifecycleResponse {
    private Date gaDatetime;
    private Date eolDatetime;
    private Date eosDatetime;
}
