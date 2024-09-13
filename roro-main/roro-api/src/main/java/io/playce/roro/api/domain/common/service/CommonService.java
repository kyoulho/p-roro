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
 * Author            Date                Description
 * ---------------  ----------------    ------------
 * Jaeeon Bae       11월 24, 2021            First Draft.
 */
package io.playce.roro.api.domain.common.service;

import io.playce.roro.common.dto.targetcloud.CredentialDto.CredentialResponse;
import io.playce.roro.mybatis.domain.common.credential.CredentialMapper;
import io.playce.roro.mybatis.domain.common.label.LabelMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Jaeeon Bae
 * @version 3.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CommonService {

    private final LabelMapper labelMapper;
    private final CredentialMapper credentialMapper;

    public List<String> getLabelNames(Long inventoryId) {
        return labelMapper.getServiceLabelNames(inventoryId);
    }

    public CredentialResponse getCredential(Long projectId, Long credentialId) {
        return credentialMapper.getCredential(projectId, credentialId);
    }
}
//end of CommonService.java