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
 * Hoon Oh       2월 07, 2022            First Draft.
 */
package io.playce.roro.jpa.repository;

import io.playce.roro.jpa.entity.DiscoveredPortRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * <pre>
 *
 *
 * </pre>
 *
 * @author Hoon Oh
 * @version 1.0
 */
@Repository
public interface DiscoveredPortRelationRepository extends JpaRepository<DiscoveredPortRelation, Long>, JpaSpecificationExecutor<DiscoveredPortRelation> {

    List<DiscoveredPortRelation> findByServerInventoryIdAndServicePortAndInventoryConnectionTypeCode(
            Long serverInventoryId, Integer servicePort, String inventoryConnectionTypeCode);

    List<DiscoveredPortRelation> findByServerInventoryIdAndServicePortAndInventoryDirectionPortTypeCodeAndTargetIpAddress(
            Long serverInventoryId, Integer servicePort, String inventoryDirectionPortTypeCode, String targetIpAddress);

    Optional<DiscoveredPortRelation> findByUniqueKey(String uniqueKey);

    List<DiscoveredPortRelation> findBySvrInvIpAddr(String ip);
}
//end of DiscoveredPortRelationRepository.java