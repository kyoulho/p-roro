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
 * Hoon Oh       1월 28, 2022            First Draft.
 */
package io.playce.roro.jpa.repository;

import io.playce.roro.jpa.entity.DiscoveredInstanceInterfaceIps;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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
public interface DiscoveredInstanceInterfaceIpsRepository extends JpaRepository<DiscoveredInstanceInterfaceIps, Long>, JpaSpecificationExecutor<DiscoveredInstanceInterfaceIps> {

    List<DiscoveredInstanceInterfaceIps> findByDiscoveredInstanceInterfaceIdAndDiscoveredInstanceInterfaceSeq(Long discoveredInstanceInterfaceId, Long discoveredInstanceInterfaceSeq);

    // No EntityManager with actual transaction available for current thread - cannot reliably process 'remove' call; nested exception is javax.persistence.TransactionRequiredException: No EntityManager with actual transaction available for current thread - cannot reliably process 'remove' call
    @Transactional
    void deleteAllByDiscoveredInstanceInterfaceId(Long discoveredInstanceInterfaceId);

    Optional<DiscoveredInstanceInterfaceIps> findByDiscoveredInstanceInterfaceIdAndDiscoveredInstanceInterfaceSeqAndDiscoveredInstanceInterfaceIpAddress(Long discoveredInstanceInterfaceId, Integer discoveredInstanceInterfaceSeq, String ipAddress);
}
//end of discoveredInstanceInterfaceIps.java