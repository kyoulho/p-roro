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
 * Dong-Heon Han    Nov 22, 2021		    First Draft.
 */

package io.playce.roro.mybatis.domain.inventory.process;

import io.playce.roro.common.dto.assessment.PageAssessmentRequestDto;
import io.playce.roro.common.dto.inventory.process.*;
import io.playce.roro.common.dto.migration.MigrationJobDetailResponseDto.*;
import io.playce.roro.common.dto.migration.MigrationJobDto;
import io.playce.roro.common.dto.migration.PageMigrationRequestDto;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Dong-Heon Han
 * @version 3.0
 */
@Repository
public interface InventoryProcessMapper {
    List<LatestInventoryProcess> selectLatestInventoryProcessByInventoryProcessType(@Param("projectId") Long projetId, @Param("inventoryProcessTypeCode") String inventoryProcessTypeCode);

    List<InventoryProcessHistory> selectInventoryProcessByInventoryProcessTypeAndDate(@Param("projectId") Long projectId, @Param("inventoryProcessTypeCode") String inventoryProcessTypeCode,
                                                                                      @Param("from") String from, @Param("to") String to);

    InventoryProcess.Result selectLastInventoryProcess(@Param("inventoryId") Long inventoryId, @Param("processType") String processType);

    InventoryProcess.CompleteScan selectLastCompleteInventoryProcess(@Param("inventoryId") Long inventoryId, @Param("processType") String processType);

    InventoryProcessDetailResponse getInventoryProcessDetail(@Param("projectId") Long projectId, @Param("inventoryProcessId") Long inventoryProcessId);

    InventoryProcessListResponse.Summary selectInventoryProcessSummary(@Param("projectId") Long projectId, @Param("inventoryId") Long inventoryId
            , @Param("inventoryProcessTypeCode") String inventoryProcessTypeCod);

    List<InventoryProcessListResponse.Content> selectInventoryProcessList(@Param("projectId") Long projectId, @Param("inventoryId") Long inventoryId
            , @Param("pageRequest") PageAssessmentRequestDto pageRequestDto, @Param("inventoryProcessTypeCode") String inventoryProcessTypeCode);

    long selectInventoryProcessCount(@Param("projectId") Long projectId, @Param("inventoryId") Long inventoryId,
                                     @Param("pageRequest") PageAssessmentRequestDto pageRequestDto, @Param("inventoryProcessTypeCode") String inventoryProcessTypeCode);

    List<InventoryProcessQueueItem> selectInventoryProcessQueueItems(@Param("domain1003") String domain1003, @Param("domain1002") String domain1002);

    InventoryProcessResponse selectInventoryProcessById(@Param("inventoryProcessId") Long inventoryProcessId);

    InventoryProcessResponse selectLastCompletedScanByInventoryId(@Param("inventoryTypeCode") String inventoryTypeCode,
                                                                  @Param("inventoryProcessTypeCode") String inventoryProcessTypeCode,
                                                                  @Param("inventoryId") Long inventoryId);

    Long selectMigrationServerDetailCount(@Param("projectId") long projectId, @Param("inventoryId") Long inventoryId);

    List<MigrationJobDto> selectMigrationServerDetailList(@Param("projectId") long projectId, @Param("inventoryId") Long inventoryId);

    Long selectMigrationServerCount(@Param("projectId") long projectId,
                                    @Param("pageRequest") PageMigrationRequestDto pageMigrationRequestDto);

    List<MigrationJobDto> selectMigrationServerList(@Param("projectId") long projectId,
                                                    @Param("pageRequest") PageMigrationRequestDto pageMigrationRequestDto);

    Date selectMaxInventoryProcess(@Param("serviceId") Long serviceId);

    Date selectMaxInventoryProcessInProject(@Param("projectId") Long projectId);

    Detail selectMigrationJob(@Param("projectId") long projectId, @Param("migrationId") long migrationId);

    SourceServer selectMigrationSourceServer(@Param("projectId") long projectId, @Param("migrationId") long migrationId);

    Map<String, String> selectExistLinux(@Param("projectId") long projectId, @Param("migrationId") long migrationId);

    TargetServer selectMigrationTargetServer(@Param("projectId") long projectId, @Param("migrationId") long migrationId);

    List<Volume> selectMigrationVolumes(@Param("projectId") long projectId, @Param("migrationId") long migrationId);

    List<Tag> selectMigrationTags(@Param("projectId") long projectId, @Param("migrationId") long migrationId);

    String selectInventoryProcessLastSuccessComplete(@Param("serverInventoryId") Long windowsServerId);

    int selectSuccessCompleteCount(Long inventoryId);
}
//end of InventoryProcessMapper.java