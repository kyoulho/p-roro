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
 * SangCheon Park   Sep 21, 2022		    First Draft.
 */
package io.playce.roro.mybatis.domain.thirdparty;

import io.playce.roro.common.dto.common.thirdparty.ThirdPartySolutionListResponse;
import io.playce.roro.common.dto.inventory.thirdparty.DiscoveredThirdPartyDto;
import io.playce.roro.common.dto.thirdparty.ThirdPartySearchTypeResponse;
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
public interface ThirdPartyMapper {

    List<ThirdPartySearchTypeResponse> selectThirdPartySearchType();

    List<ThirdPartySolutionListResponse> selectThirdPartyList();

    List<DiscoveredThirdPartyDto> selectProjectDiscoveredThirdParty(@Param("projectId") Long projectId,
                                                                    @Param("serviceIds") List<Long> serviceIds);

    List<DiscoveredThirdPartyDto> selectProjectThirdParties(@Param("projectId") Long projectId,
                                                            @Param("serviceIds") List<Long> serviceIds,
                                                            @Param("thirdPartySolutionId") Long thirdPartySolutionId);

    List<DiscoveredThirdPartyDto> selectServerDiscoveredThirdParty(@Param("projectId") Long projectId,
                                                                   @Param("serverId") Long serverId);

    List<DiscoveredThirdPartyDto> selectServerThirdParties(@Param("projectId") Long projectId,
                                                           @Param("serverId") Long serverId,
                                                           @Param("thirdPartySolutionId") Long thirdPartySolutionId);
}
