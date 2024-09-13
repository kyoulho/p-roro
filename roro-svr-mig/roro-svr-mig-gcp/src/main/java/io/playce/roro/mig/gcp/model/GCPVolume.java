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
 * SangCheon Park   Mar 15, 2022		    First Draft.
 */
package io.playce.roro.mig.gcp.model;

import com.google.api.services.compute.model.Operation;
import io.playce.roro.common.dto.migration.MigrationProcessDto;
import io.playce.roro.mig.MigrationManager;
import io.playce.roro.mig.gcp.enums.ResourceType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.File;

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
@ToString
public class GCPVolume {

    private String gcpProjectId;
    private String region;
    private String zone;
    private String migrationId;

    private File tarGz;
    private MigrationProcessDto.Volume vol;

    // private String imageName;
    private String diskName;
    private String diskUrl;
    private String diskImage;

    private Operation operation;
    private ResourceType resourceType;

    public GCPVolume(String gcpProjectId, String migrationId) {
        this.gcpProjectId = gcpProjectId;
        this.migrationId = migrationId;
        this.region = MigrationManager.getBucketRegion();
    }

    public String getLocation() {
        if (resourceType.equals(ResourceType.REGIONAL)) {
            return region;
        } else if (resourceType.equals(ResourceType.ZONAL)) {
            return zone;
        }
        return null;
    }
}
//end of GCPVolume.java