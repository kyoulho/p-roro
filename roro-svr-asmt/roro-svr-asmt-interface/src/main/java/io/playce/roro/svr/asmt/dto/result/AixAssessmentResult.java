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
 * Author			Date				Description
 * ---------------	----------------	------------
 * Hoon Oh          11월 10, 2021		First Draft.
 */
package io.playce.roro.svr.asmt.dto.result;

import io.playce.roro.svr.asmt.dto.ServerAssessmentResult;
import io.playce.roro.svr.asmt.dto.aix.ExtraPartition;
import io.playce.roro.svr.asmt.dto.aix.FileSystem;
import io.playce.roro.svr.asmt.dto.aix.Security;
import io.playce.roro.svr.asmt.dto.aix.VolumeGroup;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Map;

/**
 * <pre>
 *
 *
 * </pre>
 *
 * @author Hoon Oh
 * @version 1.0
 */
@Getter
@SuperBuilder(toBuilder = true)
@ToString
public class AixAssessmentResult extends ServerAssessmentResult {
    private final String distributionVersion;
    private final String firmwareVersion;

    private final Security security;

    private final Map<String, VolumeGroup> vgs;
    private final Map<String, FileSystem> fileSystems;

    private final Map<String, String> kernelParameters;

    // private final Map<String, Map<String, String>> ulimits;

    private final Map<String, List<ExtraPartition>> extraPartitions;
}
//end of AixAssessmentResult.java