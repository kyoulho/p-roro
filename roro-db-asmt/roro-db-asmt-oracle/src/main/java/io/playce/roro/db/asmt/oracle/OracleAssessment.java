/*
 * Copyright 2021 The playce-roro-v3 Project.
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
 * SangCheon Park   Nov 22, 2021		    First Draft.
 */
package io.playce.roro.db.asmt.oracle;

import io.playce.roro.common.dto.assessment.DatabaseDto;
import io.playce.roro.common.exception.ConnectionErrorException;
import io.playce.roro.db.asmt.AbstractDBAssessment;
import io.playce.roro.db.asmt.oracle.config.OracleDataSource;
import io.playce.roro.db.asmt.oracle.service.OracleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
@Slf4j
@Component("ORACLEAssessment")
@RequiredArgsConstructor
public class OracleAssessment extends AbstractDBAssessment {

    private final OracleDataSource dataSourceFactory;

    private final OracleService oracleService;

    @Override
    protected DataSource getDataSource(DatabaseDto database) {
        try {
            return dataSourceFactory.getDataSource(database);
        } catch (Exception e) {
            throw new ConnectionErrorException(e.getMessage());
        }
    }

    @Override
    public Object assessment(DatabaseDto database) {
        final DataSource dataSource = getDataSource(database);

        return oracleService.getAssessment(dataSource, database);
    }
}
//end of OracleDBAssessment.java