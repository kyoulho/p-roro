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
package io.playce.roro.mig.gcp.common.util;

import com.google.auth.oauth2.GoogleCredentials;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
public class CredentialUtil {

    public static GoogleCredentials convertCredentials(String accountKey, Collection<String> scopes) {
        GoogleCredentials credentials = null;
        try {
            if (accountKey == null) {
                credentials = GoogleCredentials.getApplicationDefault();
            } else {
                try (InputStream is = new ByteArrayInputStream(
                        accountKey.getBytes(StandardCharsets.UTF_8))) {
                    credentials = GoogleCredentials.fromStream(is);

                    if (credentials.createScopedRequired()) {
                        credentials = credentials.createScoped(scopes);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return credentials;
    }
}
//end of CredentialUtil.java