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

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.Date;

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
@Table(name = "discovered_port_relation_detail")
@ToString
public class DiscoveredPortRelationDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long discoveredPortRelationDetailId;

    @Column
    private Long discoveredPortRelationId;

    @Column
    private Long inventoryProcessId;

    @Column
    private String processCommandUserName;

    @Column
    private String processCommand;

    @Column
    private String connectionStatusTypeCode;

    @Column
    private int localPort;

    @Column
    private int foreignPort;

    @Column
    private Long registUserId;

    @Column
    private Date registDatetime;
}
//end of DiscoveredPortRelationDetail.java
