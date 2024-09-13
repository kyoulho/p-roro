package io.playce.roro.common.dto.common.thirdparty;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@ToString
public class ThirdPartySolutionResponse {

    private Long thirdPartySolutionId;
    private String thirdPartySolutionName;
    private String vendor;
    private String description;
    private String registUserId;
    private Date registDatetime;
    private List<ThirdPartySearchTypeResponse> thirdPartySearchTypes;

    @Getter
    @Setter
    @ToString
    public static class ThirdPartySearchTypeResponse {

        private String searchType;
        private List<ThirdPartySearchTypeDetail> values;

        @JsonIgnore
        private int displayOrder;

    }

    @Getter
    @Setter
    @ToString
    public static class ThirdPartySearchTypeDetail {

        private Long thirdPartySearchTypeId;
        private String searchValue;
        // @Getter(AccessLevel.NONE)
        // private String inventoryTypeCode;
        // @Getter(AccessLevel.NONE)
        // private String windowsYn;

        // public String getInventoryTypeCode() {
        //     if (StringUtils.isEmpty(inventoryTypeCode)) {
        //         return Domain1001.SVR.name();
        //     }
        //
        //     return inventoryTypeCode;
        // }
        //
        // public String getWindowsYn() {
        //     if (StringUtils.isEmpty(windowsYn)) {
        //         return Domain101.N.name();
        //     }
        //
        //     return windowsYn;
        // }
    }

}
