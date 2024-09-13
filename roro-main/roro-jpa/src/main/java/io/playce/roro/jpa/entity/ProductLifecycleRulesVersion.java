/*
 * Copyright 2021 The Playce-RoRo Project.
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
 * SangCheon Park   Jan 1, 2022		First Draft.
 */
package io.playce.roro.jpa.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
@Table(name = "product_lifecycle_rules_version")
public class ProductLifecycleRulesVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productLifecycleRulesVersionId;

    @Column
    private String version;

    @Column
    private Date gaDatetime;

    @Column
    private Date eolDatetime;

    @Column
    private Date eosDatetime;

    @Column
    private Long productLifecycleRulesId;

}
//end of ProductLifecycleRulesVersion.java