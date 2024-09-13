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
package io.playce.roro.mig.gcp.model.firewall;

import io.playce.roro.mig.gcp.model.network.AllowedPort;
import io.playce.roro.mig.gcp.model.network.DeniedPort;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
@Getter
@Setter
public class FirewallDetail {
    private String id;
    private String name;
    private String description;
    private String networkName;
    private String direction;
    private String priority;
    private Boolean disabled;
    private String actionType;

    private String targetType;
    private String targetServiceAccount;
    private String targetTag;

    /* INGRESS*/
    private List<String> sourceRanges;
    private String sourceServiceAccount;
    private List<String> sourceTags;

    private List<String> secondarySourceRanges;
    private String secondarySourceServiceAccount;
    private List<String> secondarySourceTags;

    /*EGRESS*/
    private List<String> destinationTags;
    private List<String> destinationRanges;
    private String destinationServiceAccount;

    private List<AllowedPort> allowed;
    private List<DeniedPort> denied;
}
//end of FirewallDetail.java