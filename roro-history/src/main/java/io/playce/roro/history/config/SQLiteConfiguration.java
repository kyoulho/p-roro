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
 * Dong-Heon Han    Apr 06, 2023		First Draft.
 */

package io.playce.roro.history.config;

import io.playce.roro.common.config.RoRoProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
public class SQLiteConfiguration {
    private final RoRoProperties roRoProperties;

    private DataSource sqliteDataSource() {
        SQLiteConfig config = new SQLiteConfig();
//        config.
        SQLiteDataSource ds = new SQLiteDataSource(config);
        ds.setUrl("jdbc:sqlite:" + roRoProperties.getWorking().getDirPath() + "/.registry");
        return ds;
    }

    @Bean
    public JdbcTemplate sqliteJdbcTemplate() {
        return new JdbcTemplate(sqliteDataSource());
    }
}