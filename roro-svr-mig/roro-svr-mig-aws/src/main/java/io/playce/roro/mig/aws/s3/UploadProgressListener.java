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
 * SangCheon Park   Mar 14, 2022		    First Draft.
 */
package io.playce.roro.mig.aws.s3;

import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.services.s3.transfer.Upload;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

/**
 * <pre>
 * S3 업로드 상태를 조회하기 위한 리스너
 * </pre>
 *
 * @author Sang-cheon Park
 * @version 1.0
 */
@Slf4j
public class UploadProgressListener implements ProgressListener {

    private File file;
    private int partNo;
    private long partLength;
    private Upload upload;
    private long lastLogTime;

    public UploadProgressListener(File file, Upload upload) {
        this.file = file;
        this.partNo = 1;
        this.upload = upload;
    }

    public UploadProgressListener(File file, int partNo) {
        this(file, partNo, 0);
    }

    public UploadProgressListener(File file, int partNo, long partLength) {
        this.file = file;
        this.partNo = partNo;
        this.partLength = partLength;
    }

    @Override
    public void progressChanged(ProgressEvent progressEvent) {
        if (upload != null) {
            switch (progressEvent.getEventType()) {
                case TRANSFER_STARTED_EVENT:
                    log.info("Upload started for file " + "\"" + file.getName() + "\"");
                    break;
                case TRANSFER_COMPLETED_EVENT:
                    log.info("Upload completed for file " + "\"" + file.getName() + "\"" + ", " + file.length()
                            + " bytes data has been transferred");
                    break;
                case TRANSFER_FAILED_EVENT:
                    log.info("Upload failed for file " + "\"" + file.getName() + "\"" + ", "
                            + progressEvent.getBytesTransferred() + " bytes data has been transferred");
                    break;
                case TRANSFER_CANCELED_EVENT:
                    log.info("Upload cancelled for file " + "\"" + file.getName() + "\"" + ", "
                            + progressEvent.getBytesTransferred() + " bytes data has been transferred");
                    break;
                case TRANSFER_PART_STARTED_EVENT:
                    // log.info("Upload started at " + partNo++ + ". part for file " + "\"" + file.getName() + "\"");
                    // break;
                case TRANSFER_PART_COMPLETED_EVENT:
                    if (System.currentTimeMillis() - lastLogTime > 60 * 1000) {
                        log.info(upload.getDescription() + ", "
                                + upload.getProgress().getBytesTransferred() + "(" + String.format("%.2f", upload.getProgress().getPercentTransferred()) + "%)"
                                + " bytes has been transferred");

                        lastLogTime = System.currentTimeMillis();
                    }
                    break;
                default:
            }
        } else {
            switch (progressEvent.getEventType()) {
                case TRANSFER_STARTED_EVENT:
                    log.info("Upload started for file " + "\"" + file.getName() + "\"");
                    break;
                case TRANSFER_COMPLETED_EVENT:
                    log.info("Upload completed for file " + "\"" + file.getName() + "\"" + ", " + file.length()
                            + " bytes has been transferred");
                    break;
                case TRANSFER_FAILED_EVENT:
                    log.info("Upload failed for file " + "\"" + file.getName() + "\"" + ", "
                            + progressEvent.getBytesTransferred() + " bytes has been transferred");
                    break;
                case TRANSFER_CANCELED_EVENT:
                    log.info("Upload cancelled for file " + "\"" + file.getName() + "\"" + ", "
                            + progressEvent.getBytesTransferred() + " bytes has been transferred");
                    break;
                case TRANSFER_PART_STARTED_EVENT:
                    log.info("Upload started at " + partNo + ". part for file " + "\"" + file.getName() + "\"");
                    break;
                case TRANSFER_PART_COMPLETED_EVENT:
                    log.info("Upload completed at " + partNo + ". part for file " + "\"" + file.getName() + "\"" + ", "
                            + (partLength > 0 ? partLength : progressEvent.getBytesTransferred())
                            + " bytes has been transferred");
                    break;
                case TRANSFER_PART_FAILED_EVENT:
                    log.info("Upload failed at " + partNo + ". part for file " + "\"" + file.getName() + "\"" + ", "
                            + progressEvent.getBytesTransferred() + " bytes has been transferred");
                    break;
                default:
            }
        }
    }
}
//end of UploadProgressListener.java