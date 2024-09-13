/*
 * Copyright 2023 The playce-roro-v3 Project.
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
 * Jihyun Park      6월 14, 2023            First Draft.
 */
package io.playce.roro.jpa.entity;

import io.playce.roro.jpa.entity.pk.ExternalConnectionLabelPK;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * <pre>
 *
 *
 * </pre>
 *
 * @author Jihyun Park
 * @version 1.0
 */
@Entity
@IdClass(ExternalConnectionLabelPK.class)
@Getter
@Setter
@Table(name = "external_connection_label")
public class ExternalConnectionLabel {

    @Id
    private String ip;

    @Id
    private Long projectId;

    @Column(nullable = false)
    private String label;

}