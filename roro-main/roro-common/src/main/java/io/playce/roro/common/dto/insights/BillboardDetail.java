package io.playce.roro.common.dto.insights;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BillboardDetail {
        private String solutionName;
        @JsonIgnore
        private String inventoryTypeCode;
        private int total;
        private int eolVersions;
        private int eolWithin30Days;
        private int eolWithin60Days;
        private int eolWithin90Days;
        private int eolWithinNext1;
        private int eolWithinNext3;
        private int eolNext3More;
        private int eolUnknown;
        private int eosVersions;
        private int eosWithin30Days;
        private int eosWithin60Days;
        private int eosWithin90Days;
        private int eosWithinNext1;
        private int eosWithinNext3;
        private int eosNext3More;
        private int eosUnknown;

}
