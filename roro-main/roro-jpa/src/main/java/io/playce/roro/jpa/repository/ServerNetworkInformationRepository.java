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
package io.playce.roro.jpa.repository;

import io.playce.roro.jpa.entity.ServerNetworkInformation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Jaeeon Bae
 * @version 2.0.0
 */
@Repository
public interface ServerNetworkInformationRepository extends JpaRepository<ServerNetworkInformation, Long>, JpaSpecificationExecutor<ServerNetworkInformation> {

    List<ServerNetworkInformation> findByServerInventoryId(Long id);

    void deleteByServerInventoryId(Long inventoryId);
}
//end of ServerSummaryRepository.java