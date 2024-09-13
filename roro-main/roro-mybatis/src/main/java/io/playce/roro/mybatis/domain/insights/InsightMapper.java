/*
 * Copyright 2023 The playce-roro-v3 Project.
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
 * SangCheon Park   Jan 11, 2023		    First Draft.
 */
package io.playce.roro.mybatis.domain.insights;

import io.playce.roro.common.dto.insights.*;
import io.playce.roro.common.dto.productlifecycle.ProductLifecycleRulesResponse;
import io.playce.roro.common.dto.productlifecycle.ProductLifecycleRulesVersionResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Mapper
public interface InsightMapper {


    List<ProductLifecycleRulesResponse> selectProductLifecycleRulesAndVersions();

    ProductLifecycleRulesResponse selectProductLifecycleRuleAndVersion(Long productLifecycleRulesId);

    List<ProductLifecycleRulesVersionResponse> selectProductLifecycleRulesVersionsBySolutionName(String solutionName);

    List<BillboardDetail> selectBillboardDetails(Long projectId, Date currentDate);

    Optional<LifecycleResponse> selectLifecycleResponse(@Param("projectId") Long projectId, @Param("inventoryId") Long inventoryId, String type);

    Map<String, String> selectVendorAndOpensourceYnBySolutionName(String solutionName);

    List<InsightDto> selectInsights(Long projectId, Integer within, Date currentDate, List<String> serviceIds, boolean toExcel);
}
