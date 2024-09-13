/*
 * Copyright 2023 The playce-roro Project.
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
 * Dong-Heon Han    May 11, 2023		First Draft.
 */

package io.playce.roro.jpa.entity.pk;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Setter @Getter
public class ExcludedExternalConnectionId implements Serializable {
    @Column(name = "PROJECT_ID", nullable = false)
    private Long projectId;
    @Column(name = "IP", nullable = false)
    private String ip;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        ExcludedExternalConnectionId entity = (ExcludedExternalConnectionId) o;
        return Objects.equals(this.projectId, entity.projectId) && Objects.equals(this.ip, entity.ip);
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectId, ip);
    }
}