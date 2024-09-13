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
 * SangCheon Park   Jan 21, 2022		    First Draft.
 */
package io.playce.roro.mybatis.domain.preconfig;

import io.playce.roro.common.dto.preconfig.PreConfigResponse;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
@Repository
public interface PreConfigMapper {

    List<PreConfigResponse> getPreConfigs(@Param("projectId") Long projectId, @Param("serverId") Long serverId);

    PreConfigResponse getPreConfig(@Param("projectId") Long projectId, @Param("serverId") Long serverId, @Param("preConfigId") Long preConfigId);
}
//end of PreConfigMapper.java
