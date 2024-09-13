/*
 * Copyright 2022 The playce-roro-v3 Project.
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
 * SangCheon Park   Sep 21, 2022		    First Draft.
 */
package io.playce.roro.jpa.repository;

import io.playce.roro.jpa.entity.ThirdPartySolution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
@Repository
public interface ThirdPartySolutionRepository extends JpaRepository<ThirdPartySolution, Long>, JpaSpecificationExecutor<ThirdPartySolution> {

    @Query(value = "SELECT * FROM third_party_solution WHERE third_party_solution_name = :thirdPartySolutionName AND delete_yn = 'N'", nativeQuery = true)
    ThirdPartySolution findByThirdPartySolutionName(String thirdPartySolutionName);

}
