package io.playce.roro.common.dto.common.thirdparty;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class ThirdPartySolutionRequest {

    private String thirdPartySolutionName;
    private String vendor;
    private String description;
    private List<ThirdPartySearchTypeRequest> thirdPartySearchTypes;

    @Getter
    @Setter
    @ToString
    public static class ThirdPartySearchTypeRequest {

        private String searchType;
        private String searchValue;
        // @Getter(AccessLevel.NONE)
        // private String inventoryTypeCode;
        // @Getter(AccessLevel.NONE)
        // private String windowsYn;
        
        // public String getInventoryTypeCode() {
        //     if (Domain1201.FILE.name().equals(searchType) && StringUtils.isEmpty(inventoryTypeCode)) {
        //         return Domain1001.SVR.name();
        //     }
        //
        //     return inventoryTypeCode;
        // }
        //
        // public String getWindowsYn() {
        //     if (Domain1201.FILE.name().equals(searchType) && StringUtils.isEmpty(windowsYn)) {
        //         return Domain101.N.name();
        //     }
        //
        //     return windowsYn;
        // }
    }

}
