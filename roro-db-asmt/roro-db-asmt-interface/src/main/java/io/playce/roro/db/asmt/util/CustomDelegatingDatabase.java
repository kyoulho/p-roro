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
 * Jeongho Baek   9월 06, 2021		First Draft.
 */
package io.playce.roro.db.asmt.util;

import org.springframework.jdbc.datasource.DelegatingDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Jeongho Baek
 * @version 2.0.0
 */
public class CustomDelegatingDatabase extends DelegatingDataSource {

    private final String catalogName;

    public CustomDelegatingDatabase(final String catalogName, final DataSource dataSource) {
        super(dataSource);
        this.catalogName = catalogName;
    }

    @Override
    public Connection getConnection() throws SQLException {
        final Connection cnx = super.getConnection();
        cnx.setCatalog(this.catalogName);
        return cnx;
    }

}
//end of DelegatingDatabase.java