/*
 * Copyright 2020 The Playce-RoRo Project.
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
 * Jeongho Baek     10월 21, 2020       First Draft.
 */
package io.playce.roro.api.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <pre>
 *  SpringDoc Open API Configuration
 * </pre>
 *
 * @author Jeongho Baek
 * @version 2.0.0
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(title = "Playce-RoRo-the-next",
                description = "RoRo API Documentation",
                version = "3.0",
                contact = @Contact(name = "Open Source Consulting", email = "lab@osci.kr"),
                license = @License(name = "© 2021 Open Source Consulting", url = "http://www.osci.kr")
        )
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer",
        description = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJSb1JvIFVzZXIgSW5mby4iLCJpc3MiOiJodHRwczovL3d3dy5wbGF5LWNlLmlvIiwidXNlciI6eyJ1c2VySWQiOjEsInVzZXJMb2dpbklkIjoiYWRtaW4iLCJ1c2VybmFtZSI6ImFkbWluIiwidXNlck5hbWVLb3JlYW4iOiLqtIDrpqzsnpAiLCJ1c2VyTmFtZUVuZ2xpc2giOiJBZG1pbiIsInVzZXJFbWFpbCI6ImFkbWluQG9zY2kua3IifSwicm9sZXMiOlsiUk9MRV9BRE1JTiJdLCJpYXQiOjE2NzMyMjYyODEsImV4cCI6MTc2NzgzNDI4MX0.lT6znRkSfQKkH4MDmr_TEfQ_2ZNFKCAzTEuy7gfaujoL0XIs1bV5zAjZLmn5RnCxrKSlWrG7nyNnHfkiZUpmBQ"
)
public class OpenApiConfig {

    @Bean
    public GroupedOpenApi projectApi() {
        String[] paths = {"/api/projects/**"};
        return GroupedOpenApi.builder()
                .group("01. Project API")
                .pathsToMatch(paths)
                .pathsToExclude("/api/projects/{projectId}/*/**")
                .build();
    }

    @Bean
    public GroupedOpenApi inventoryApi() {
        String[] paths = {"/api/projects/{projectId}/inventory/**", "/api/projects/{projectId}/third-parties/**"};
        return GroupedOpenApi.builder()
                .group("02. Inventory API")
                .pathsToMatch(paths)
                .build();
    }

    @Bean
    public GroupedOpenApi discoveredApi() {
        String[] paths = {"/api/projects/{projectId}/discovered/**"};
        return GroupedOpenApi.builder()
                .group("03. Discovered API")
                .pathsToMatch(paths)
                .build();
    }

    @Bean
    public GroupedOpenApi assessmentApi() {
        String[] paths = {"/api/projects/{projectId}/assessments/**"};
        return GroupedOpenApi.builder()
                .group("04. Assessment API")
                .pathsToMatch(paths)
                .build();
    }

    @Bean
    public GroupedOpenApi targetCloudApi() {
        String[] paths = {"/api/projects/{projectId}/target-cloud/**"};
        return GroupedOpenApi.builder()
                .group("05. Target Cloud API")
                .pathsToMatch(paths)
                .build();
    }

    @Bean
    public GroupedOpenApi migrationApi() {
        String[] paths = {"/api/projects/{projectId}/migrations/**"};
        return GroupedOpenApi.builder()
                .group("06. Migration API")
                .pathsToMatch(paths)
                .build();
    }

    @Bean
    public GroupedOpenApi networkFilterApi() {
        String[] paths = {"/api/projects/{projectId}/network-filters/**"};
        return GroupedOpenApi.builder()
                .group("07. Network Filter API")
                .pathsToMatch(paths)
                .build();
    }

    @Bean
    public GroupedOpenApi prerequisitesApi() {
        String[] paths = {"/api/projects/{projectId}/prerequisites/**"};
        return GroupedOpenApi.builder()
                .group("08. Prerequisites API")
                .pathsToMatch(paths)
                .build();
    }

    @Bean
    public GroupedOpenApi topology() {

        String[] paths = {"/api/projects/{projectId}/topology/**"};
        return GroupedOpenApi.builder()
                .group("17. Topology API")
                .pathsToMatch(paths)
                .build();
    }

    @Bean
    public GroupedOpenApi dashboardApi() {
        String[] paths = {"/api/projects/{projectId}/dashboard"};
        return GroupedOpenApi.builder()
                .group("09. Dashboard API")
                .pathsToMatch(paths)
                .build();
    }

    @Bean
    public GroupedOpenApi statisticsApi() {
        String[] paths = {"/api/projects/{projectId}/statistics/**"};
        return GroupedOpenApi.builder()
                .group("10. Statistics API")
                .pathsToMatch(paths)
                .build();
    }

    @Bean
    public GroupedOpenApi commonApi() {
        String[] paths = {"/api/common/**"};
        return GroupedOpenApi.builder()
                .group("11. Common API")
                .pathsToMatch(paths)
                .build();
    }

    // @Bean
    // public GroupedOpenApi settingApi() {
    //     String[] paths = {"/api/setting/**"};
    //     return GroupedOpenApi.builder()
    //             .group("12. Setting API")
    //             .pathsToMatch(paths)
    //             .build();
    // }

    @Bean
    public GroupedOpenApi authenticationApi() {
        String[] paths = {"/api/auth/**"};
        return GroupedOpenApi.builder()
                .group("12. Authentication API")
                .pathsToMatch(paths)
                .build();
    }

    @Bean
    public GroupedOpenApi memberApi() {
        String[] paths = {"/api/member/**"};
        return GroupedOpenApi.builder()
                .group("13. Member API")
                .pathsToMatch(paths)
                .build();
    }

    @Bean
    public GroupedOpenApi cloudReadinessApi() {
        String[] paths = {"/api/projects/{projectId}/cloud-readiness/**", "/api/cloud-readiness/**"};
        return GroupedOpenApi.builder()
                .group("14. Cloud-Readiness API")
                .pathsToMatch(paths)
                .build();
    }

    @Bean
    public GroupedOpenApi hostScanApi() {
        String[] paths = {"/api/projects/{projectId}/host-scan/**"};
        return GroupedOpenApi.builder()
                .group("15. Host-Scan API")
                .pathsToMatch(paths)
                .build();
    }

    @Bean
    public GroupedOpenApi insightsApi() {
        String[] paths = {"/api/projects/{projectId}/insights/**"};
        return GroupedOpenApi.builder()
                .group("16. Insights API")
                .pathsToMatch(paths)
                .build();
    }

    @Bean
    public GroupedOpenApi k8sApi() {
        String[] paths = {"/api/projects/{projectId}/k8s-clusters/**"};
        return GroupedOpenApi.builder()
                .group("18. Kubernetes API")
                .pathsToMatch(paths)
                .build();
    }
}
//end of OpenApiConfig.java