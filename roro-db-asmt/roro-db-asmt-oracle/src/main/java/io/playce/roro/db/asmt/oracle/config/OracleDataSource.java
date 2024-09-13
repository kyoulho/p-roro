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
package io.playce.roro.db.asmt.oracle.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.playce.roro.common.dto.assessment.DatabaseDto;
import io.playce.roro.common.util.GeneralCipherUtil;
import io.playce.roro.db.asmt.constant.DBConstants;
import io.playce.roro.db.asmt.factory.DataSourceFactory;
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
@Component
public class OracleDataSource implements DataSourceFactory {

    @Override
    public DataSource getDataSource(DatabaseDto database) {
        if (database.getDatabaseType().equals(DBConstants.DATABASE_TYPE_ORACLE)) {
            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setDriverClassName(DBConstants.ORACLE_DRIVER_CLASS_NAME);
            hikariConfig.setJdbcUrl(database.getJdbcUrl());
            hikariConfig.setUsername(database.getUserName());
            hikariConfig.setPassword(GeneralCipherUtil.decrypt(database.getPassword()));

            return new HikariDataSource(hikariConfig);
        } else {
            return null;
        }
    }
}
//end of OracleDataSourceFactory.java