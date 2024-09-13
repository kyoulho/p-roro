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
package io.playce.roro.common.dto.targetcloud;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
@Getter
@Setter
public class CredentialDto {

    @Getter
    @Setter
    public static class CredentialRequest {
        private String credentialName;
        private String credentialTypeCode;
        private String accessKey;
        private String secretKey;
    }

    @Getter
    @Setter
    public static class CredentialResponse {
        private Long credentialId;
        private String credentialName;
        private Long projectId;
        private String credentialTypeCode;
        private String accessKey;
        private String secretKey;
        private String userName;
        private String userPassword;
        private String keyFileName;
        private String keyFilePath;
        private String keyFileContent;
        private Date registDatetime;
        private Long registUserId;
        private String registUserLoginId;
        private Date modifyDatetime;
        private Long modifyUserId;
        private String modifyUserLoginId;
    }
    
    @Getter
    @Setter
    public static class CredentialSimpleResponse {

        public CredentialSimpleResponse(CredentialResponse credential) {
            if (credential != null) {
                this.credentialId = credential.getCredentialId();
                this.credentialName = credential.getCredentialName();
                this.credentialTypeCode = credential.getCredentialTypeCode();
            }
        }

        public CredentialSimpleResponse() {

        }

        private Long credentialId;
        private String credentialName;
        private String credentialTypeCode;
    }
}
//end of CredentialDto.java