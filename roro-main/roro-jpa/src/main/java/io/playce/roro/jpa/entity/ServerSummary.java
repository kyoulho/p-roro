/*
 * Copyright 2022 The Playce-RoRo Project.
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
 * Author            Date                Description
 * ---------------  ----------------    ------------
 * Jaeeon Bae       1월 20, 2022            First Draft.
 */
package io.playce.roro.jpa.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Jaeeon Bae
 * @version 2.0.0
 */
@Entity
@Getter
@Setter
@Table(name = "server_summary")
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServerSummary {

    @Id
    private Long serverInventoryId;

    @Column
    @JsonProperty("hostname")
    private String hostName;

    @Column
    @JsonProperty("vendor")
    private String vendorName;

    @Column
    private Integer cpuCount;

    @Column
    private String cpuModel;

    @Column
    @JsonProperty("cores")
    private Integer cpuCoreCount;

    @Column
    @JsonProperty("sockets")
    private Integer cpuSocketCount;

    @Column
    @JsonProperty("architecture")
    private String cpuArchitecture;

    @Column
    @JsonProperty("kernel")
    private String osKernel;

    @Column
    @JsonProperty("os")
    private String osName;

    @Column
    private String osAlias;

    @Column
    private String osVersion;

    @Column
    @JsonProperty("family")
    private String osFamily;

    @Column
    @JsonProperty("memory")
    private Integer memSize;

    @Column
    @JsonProperty("swap")
    private Integer swapSize;

    @Column
    private String userGroupConfigJson;


}
//end of ServerSummary.java
