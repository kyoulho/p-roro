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

import io.playce.roro.jpa.entity.pk.DiscoveredInstanceInterfacePK;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

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
@IdClass(value = DiscoveredInstanceInterfacePK.class)
@Getter
@Setter
@Table(name = "discovered_instance_interface")
@ToString
public class DiscoveredInstanceInterface {
    @Id
    private Long discoveredInstanceInterfaceId;

    @Id
    private Integer discoveredInstanceInterfaceSeq;

    @Column
    private String discoveredInstanceInterfaceDetailTypeCode;

    @Column
    private String descriptorsName;

    @Column
    private String fullDescriptors;
}
//end of DiscoveredInstanceInterface.java