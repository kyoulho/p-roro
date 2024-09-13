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
package io.playce.roro.mig.gcp.auth;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.apache.commons.io.IOUtils;

import java.io.File;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
@Slf4j
@Getter
public class GCPCredentials {

    private String accountKey;
    private String projectId;

    public GCPCredentials(String accountKey, String projectId) {
        this.accountKey = accountKey;
        this.projectId = projectId;
    }

    public GCPCredentials(String keyFilePath) throws Exception {
        accountKey = IOUtils.toString(new File(keyFilePath).toURI(), "UTF-8");
        JSONObject json = (JSONObject) JSONValue.parse(accountKey);
        projectId = json.getAsString("project_id");
    }
}
//end of GCPCredentials.java