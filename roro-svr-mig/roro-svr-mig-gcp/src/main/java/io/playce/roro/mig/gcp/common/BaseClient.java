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
 * SangCheon Park   Feb 10, 2022		    First Draft.
 */
package io.playce.roro.mig.gcp.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.cloudresourcemanager.CloudResourceManager;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import io.playce.roro.common.util.JsonUtil;
import io.playce.roro.mig.gcp.common.util.CredentialUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
@Slf4j
public class BaseClient {

    /**
     * The Application name.
     */
    protected final String applicationName = "roro-the-next";
    /**
     * Project id
     */
    protected String projectId;
    /**
     * Region
     */
    protected String region;
    /**
     * Global instance of the HTTP transport.
     */
    protected HttpTransport httpTransport;
    /**
     * The Json factory.
     */
    protected JsonFactory jsonFactory;
    /**
     * The Request initializer.
     */
    protected HttpRequestInitializer requestInitializer;
    /**
     * The Credentials.
     */
    protected GoogleCredentials credentials;

    /**
     * The Resource manager.
     */
    protected CloudResourceManager resourceManager;

    protected String clientEmail;
    protected String clientId;

    /**
     * Instantiates a new Base client.
     */
    public BaseClient() {}

    /**
     * Instantiates a new Base client.
     *
     * @param projectId  the project id
     * @param accountKey the account key
     * @param scopes     the scopes
     */
    public BaseClient(String projectId, String accountKey, Collection<String> scopes) {
        try {
            this.projectId = projectId;

            JsonNode node = JsonUtil.readTree(accountKey);
            clientEmail = node.get("client_email").asText();
            clientId = node.get("client_id").asText();
            credentials = CredentialUtil.convertCredentials(accountKey, scopes);

            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            jsonFactory = JacksonFactory.getDefaultInstance();
            requestInitializer = new HttpCredentialsAdapter(credentials);

        } catch (Exception e) {
            log.error("Unhandled exception occurred while create GCP Client.", e);
        }
    }
}
//end of BaseClient.java