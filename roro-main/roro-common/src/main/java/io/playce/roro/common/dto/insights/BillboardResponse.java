package io.playce.roro.common.dto.insights;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class BillboardResponse {
    private Map<String,Long> eol;
    private Map<String,Long> eolWithin;
    private Map<String,Long> eos;
    private Map<String,Long> eosWithin;

}
