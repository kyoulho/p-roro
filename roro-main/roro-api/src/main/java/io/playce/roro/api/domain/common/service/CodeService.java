/*
 * Copyright 2021 The playce-roro-v3} Project.
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
 * Dong-Heon Han    Nov 24, 2021		    First Draft.
 */

package io.playce.roro.api.domain.common.service;

import io.playce.roro.common.dto.common.code.CodeDomain;
import io.playce.roro.mybatis.domain.common.code.CodeDomainMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Dong-Heon Han
 * @version 3.0
 */
@Service
@RequiredArgsConstructor
public class CodeService {
    private final CodeDomainMapper codeDomainMapper;

    public List<CodeDomain> getCodeDomains(String keyword) {
        return codeDomainMapper.selectCodeDomains(keyword);
    }
}
//end of CodeService.java