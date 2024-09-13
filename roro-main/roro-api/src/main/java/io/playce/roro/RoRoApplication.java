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
 * Jeongho Baek   11월 02, 2021		First Draft.
 */
package io.playce.roro;

import com.logviewer.springboot.LogViewerSpringBootConfig;
import com.logviewer.springboot.LogViewerWebsocketConfig;
import io.playce.roro.common.exception.RoRoException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Import;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Jeongho Baek
 * @version 2.0.0
 */
@SpringBootApplication
@Import({LogViewerSpringBootConfig.class, LogViewerWebsocketConfig.class})
public class RoRoApplication extends SpringBootServletInitializer {


    /**
     * <pre>
     * The entry point of application.
     * </pre>
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        initDatabase();
        SpringApplication.run(RoRoApplication.class, args);
    }

    /**
     * <pre>
     * On startup.
     * </pre>
     *
     * @param servletContext the servlet context
     *
     * @throws ServletException the servlet exception
     */
    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        initDatabase();
        super.onStartup(servletContext);
    }

    /**
     * <pre>
     * Configure spring application builder.
     * </pre>
     *
     * @param builder the builder
     *
     * @return the spring application builder
     */
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(RoRoApplication.class)
                /*.bannerMode(org.springframework.boot.Banner.Mode.CONSOLE)*/;
    }

    /**
     * <pre>
     * initialize database.
     *
     * If run as a web application, main() method will not be invoked.
     * If run as a standalone application, onStartup() method will not be invoked.
     * </pre>
     */
    private static void initDatabase() {
        String jdbcUrl = System.getProperty("spring.datasource.url", "jdbc:mariadb://localhost:3306/rorodb");
        // String driverClass = System.getProperty("spring.datasource.driver-class-name", "net.sf.log4jdbc.sql.jdbcapi.DriverSpy");
        String username = System.getProperty("spring.datasource.username", "playce");
        String password = System.getProperty("spring.datasource.password", "playce");

        try {
            if (jdbcUrl.contains("mysql") || jdbcUrl.contains("maria")) {
                // DB 로깅을 비활성화한다.
                String url = jdbcUrl.replaceAll("log4jdbc:", "");
                String driverClass = null;

                if (url.contains("maria")) {
                    driverClass = "org.mariadb.jdbc.Driver";
                } else {
                    driverClass = "com.mysql.jdbc.Driver";
                }

                Class.forName(driverClass).getDeclaredConstructor().newInstance();
                try (Connection connection = DriverManager.getConnection(url, username, password); Statement stmt = connection.createStatement()) {
                    ResultSet results = stmt.executeQuery("SELECT table_name FROM information_schema.tables WHERE table_schema = '" + connection.getCatalog() + "';");

                    List<String> tableNames = new ArrayList<String>();
                    while (results.next()) {
                        tableNames.add(results.getString("table_name").toUpperCase());
                    }

                    if (!tableNames.contains("PROJECT_MASTER") || !tableNames.contains("INVENTORY_MASTER") || !tableNames.contains("SERVER_MASTER") ||
                            !tableNames.contains("MIDDLEWARE_MASTER") || !tableNames.contains("APPLICATION_MASTER") || !tableNames.contains("DATABASE_MASTER")) {

                        System.out.println(new Date() + " : RoRo database NOT initialized. Starting auto configuration for RoRo database.");

                        System.setProperty("spring.datasource.initialization-mode", "always");
                    } else {
                        System.out.println(new Date() + " : RoRo database already initialized.");
                    }
                }
            } else {
                throw new RoRoException(jdbcUrl + " is unsupported DB or JDBC driver not exist.");
            }
        } catch (Exception e) {
            if (e instanceof java.sql.SQLNonTransientConnectionException ||
                    e instanceof java.sql.SQLInvalidAuthorizationSpecException ||
                    e instanceof java.sql.SQLSyntaxErrorException) {
                System.err.println("\n" + e.getMessage());
                System.err.println("\nRoRo will be terminated. Please check the DB connection information is valid.");
                System.err.println("[Connection URL] : " + jdbcUrl);
                System.err.println("[Username] : " + username);
                System.err.println("[Password] : " + password);
                System.exit(-1);
            } else if (e instanceof RoRoException) {
                System.err.println("\n" + e.getMessage());
                System.err.println("\nRoRo will be terminated. Please use MariaDB over 10.6.0.");
                System.err.println("[Connection URL] : " + jdbcUrl);
                System.exit(-1);
            } else {
                e.printStackTrace();
            }
        }

        System.out.println();
    }
}
//end of RoRoApplication.java