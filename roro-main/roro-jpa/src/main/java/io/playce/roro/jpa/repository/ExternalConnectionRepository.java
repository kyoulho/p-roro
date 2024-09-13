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
 * SangCheon Park   Jan 1, 2023		First Draft.
 */
package io.playce.roro.jpa.repository;

import io.playce.roro.jpa.entity.ExternalConnection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ExternalConnectionRepository extends JpaRepository<ExternalConnection, Long>, JpaSpecificationExecutor<ExternalConnection> {

    // No EntityManager with actual transaction available for current thread - cannot reliably process 'remove' call
    @Transactional
    void deleteByApplicationInventoryId(Long applicationInventoryId);

    List<ExternalConnection> findAllByIp(String ip);
}
//end of ExternalConnectionRepository.java