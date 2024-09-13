package io.playce.roro.common.dto.common.thirdparty;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@ToString
public class ThirdPartySolutionListResponse {

    private Long thirdPartySolutionId;
    private String thirdPartySolutionName;
    private List<DiscoveryType> discoveryTypes;
    private String vendor;
    private Date registDatetime;

    @JsonIgnore
    private String discoveryType;

    @AllArgsConstructor
    @Getter
    public static class DiscoveryType {
        private String searchType;
        private String count;
    }
}
