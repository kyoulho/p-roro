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
 * Hoon Oh       1월 27, 2022            First Draft.
 */
package io.playce.roro.jpa.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.Date;

/**
 * <pre>
 *
 *
 * </pre>
 *
 * @author Hoon Oh
 * @version 1.0
 */
@Entity
@Getter
@Setter
@Table(name = "discovered_instance_master", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"DISCOVERED_IP_ADDRESS", "DISCOVERED_DETAIL_DIVISION"})
})
@Where(clause = "delete_yn='N'")
@ToString
public class DiscoveredInstanceMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long discoveredInstanceId;
    private Long finderInventoryId;
    private Long possessionInventoryId;
    private String inventoryTypeCode;
    private String inventoryDetailTypeCode;
    private String inventoryRegistTypeCode;
    private Long projectId;
    @Column(name = "DISCOVERED_IP_ADDRESS")
    private String discoveredIpAddress;
    @Column(name = "DISCOVERED_DETAIL_DIVISION")
    private String discoveredDetailDivision;
    private Date registDatetime;
    private String deleteYn;
    private Long inventoryProcessId;
}
