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

import com.google.api.services.compute.model.AttachedDisk;
import com.google.api.services.compute.model.Disk;
import com.google.api.services.compute.model.Image;
import com.google.api.services.compute.model.Instance;
import io.playce.roro.common.dto.migration.MigrationProcessDto;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
public class GCPRequestGenerator {

    public static class ImageRequest {
        public static Image generateCreateDiskImageRequest(Image.RawDisk rawDisk, String diskImageName) {
            Image requestBody = new Image();
            requestBody.setName(diskImageName);
            requestBody.setRawDisk(rawDisk);
            return requestBody;
        }

        public static Image generateDefaultLinuxImageRequest() {
            Image requestBody = new Image();
            return requestBody;
        }
    }

    public static class DiskRequest {

        public static Disk generateDiskRequest(GCPVolume gcpVolume, String diskName) {
            Disk disk = new Disk();
            disk.setName(diskName);
            disk.setRegion(gcpVolume.getRegion());
            disk.setSizeGb(gcpVolume.getVol().getVolumeSize());
            if (gcpVolume.getDiskImage() != null) {
                // From disk image
                disk.setSourceImage(gcpVolume.getDiskImage());
            } else if (gcpVolume.getDiskUrl() != null) {
                // From disk copy
                disk.setSourceDisk(gcpVolume.getDiskUrl());
            }
            return disk;
        }

        public static Image.RawDisk generateRawDiskRequest(String bucketName, String migrationId, String fileName) {
            Image.RawDisk rawDisk = new Image.RawDisk();
            rawDisk.setSource("https://storage.cloud.google.com/" + bucketName + "/" + migrationId + "/" + fileName);
            return rawDisk;
        }

        public static AttachedDisk generateAttachedDiskRequest(GCPVolume gcpVolume) {
            AttachedDisk attachedDisk = new AttachedDisk();
            attachedDisk.setSource(gcpVolume.getDiskUrl());

            // AttachedDiskInitializeParams params = new AttachedDiskInitializeParams();
            // params.setDiskName(gcpVolume.getImageName());
            // params.setSourceImage("global/images/" + gcpVolume.getImageName());
            // params.setDiskSizeGb(gcpVolume.getVol().getSize());

            if ("Y".equals(gcpVolume.getVol().getRootYn())) {
                attachedDisk.setBoot(true);
            }

            // attachedDisk.setInitializeParams(params);

            return attachedDisk;
        }
    }
    

    public static class InstanceRequest {
        public static Instance generateInstanceRequest(MigrationProcessDto migration) {
            Instance instance = new Instance();
            instance.setName(migration.getInstanceName());
            instance.setMachineType("zones/" + migration.getAvailabilityZone() + "/machineTypes/" + migration.getInstanceType());
            // instance.setNetworkInterfaces(netList);
            // instance.setDisks(attachedDiskList);
            return instance;
        }
    }
}
//end of GCPRequestGenerator.java