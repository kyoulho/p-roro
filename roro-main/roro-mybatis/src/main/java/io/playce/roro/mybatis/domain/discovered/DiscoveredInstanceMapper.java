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
 * SangCheon Park   Nov 30, 2021		    First Draft.
 */
package io.playce.roro.mybatis.domain.discovered;

import io.playce.roro.common.dto.discovered.*;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Hoon Oh
 * @version 3.0
 */
@Repository
public interface DiscoveredInstanceMapper {
    List<DiscoveredServerListResponse.Content> selectDiscoveredServer(@Param("projectId") Long projectId, @Param("pageRequest") PageDiscoveredRequestDto pageRequestDto);

    long selectDiscoveredServerCount(Long projectId, @Param("pageRequest") PageDiscoveredRequestDto pageRequestDto);

    List<DiscoveredDatabaseListResponse> selectDiscoveredDatabaseList(@Param("projectId") Long projectId);

    @Deprecated
    List<DiscoveredServerListExcelResponse> selectDiscoveredServerWithoutPaging(@Param("projectId") Long projectId);

    List<String> selectDiscoveredServerIPList(@Param("projectId") Long projectId);

    DiscoveredDatabaseDetailResponse selectDiscoveredDatabaseDetail(@Param("projectId") Long projectId, @Param("discoveredInstanceId") Long discoveredInstanceId);

    List<Long> selectAllDiscoveredServer(@Param("projectId") Long projectId, @Param("representativeIpAddress") String representativeIpAddress);

    int selectDuplicatedDiscoveredDatabaseCount(@Param("projectId") Long projectId,
                                                @Param("representativeIpAddress") String representativeIpAddress,
                                                @Param("discoveredDetailDivision") String discoveredDetailDivision);
}
//end of DiscoveredPortMapper.java
