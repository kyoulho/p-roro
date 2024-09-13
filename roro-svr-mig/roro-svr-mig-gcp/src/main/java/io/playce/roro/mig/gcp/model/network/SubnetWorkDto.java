/*
 * Copyright 2022 The playce-roro-v3 Project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Revision History
 * Author			Date				Description
 * ---------------	----------------	------------
 * SangCheon Park   Feb 14, 2022		    First Draft.
 */
package io.playce.roro.mig.gcp.model.network;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
public class SubnetWorkDto {

    @Getter
    @Setter
    public static class SubnetWorks {
        private List<SubnetWork> subnetworks;
    }

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SubnetWork {
        private String id;
        private String creationTimestamp;
        private String name;
        private String description;
        private String network;
        private String ipCidrRange;
        private String gatewayAddress;
        private String region;
        private String selfLink;
        private Boolean privateIpGoogleAccess;
        private String privateIpv6GoogleAccess;
        private String fingerprint;
        private boolean allowSubnetCidrRoutesOverlap;
        private String purpose;
        private String role;
        private String state;
        private String kind;
    }
}
//end of SubnetWorkDto.java