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
package io.playce.roro.db.asmt.constant;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
public class DBConstants {
    public static final int CONNECTION_VALID_TIME_OUT = 5;

    public static final String DATABASE_TYPE_ORACLE = "ORACLE";
    public static final String DATABASE_TYPE_MYSQL = "MYSQL";
    public static final String DATABASE_TYPE_MARIADB = "MARIADB";
    public static final String DATABASE_TYPE_TIBERO = "TIBERO";
    public static final String DATABASE_TYPE_MSSQL = "MSSQL";
    public static final String DATABASE_TYPE_POSTGRESQL = "POSTGRE";
    public static final String DATABASE_TYPE_DB2 = "DB2";
    public static final String DATABASE_TYPE_SYBASE = "SYBASE";

    public static final String ORACLE_DRIVER_CLASS_NAME = "oracle.jdbc.OracleDriver";
    public static final String MYSQL_DRIVER_CLASS_NAME = "com.mysql.cj.jdbc.Driver";
    public static final String MARIADB_DRIVER_CLASS_NAME = "org.mariadb.jdbc.Driver";
    public static final String TIBERO_DRIVER_CLASS_NAME = "com.tmax.tibero.jdbc.TbDriver";
    public static final String MSSQL_DRIVER_CLASS_NAME = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    public static final String POSTGRESQL_DRIVER_CLASS_NAME = "org.postgresql.Driver";
    public static final String DB2_DRIVER_CLASS_NAME = "com.ibm.db2.jcc.DB2Driver";
    public static final String SYBASE_DRIVER_CLASS_NAME = "com.sybase.jdbc4.jdbc.SybDriver";
}
//end of DBConstants.java