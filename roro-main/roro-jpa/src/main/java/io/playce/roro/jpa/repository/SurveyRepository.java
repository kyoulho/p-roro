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
 * Jaeeon Bae       5월 19, 2022            First Draft.
 */
package io.playce.roro.jpa.repository;

import io.playce.roro.jpa.entity.Survey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Jaeeon Bae
 * @version 3.0
 */
@Repository
public interface SurveyRepository extends JpaRepository<Survey, Long>, JpaSpecificationExecutor<Survey> {

    Survey findTopByOrderBySurveyIdDesc();
}