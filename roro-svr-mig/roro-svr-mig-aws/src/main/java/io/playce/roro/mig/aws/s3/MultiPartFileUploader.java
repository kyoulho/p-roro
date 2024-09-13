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

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PartETag;
import com.amazonaws.services.s3.model.UploadPartRequest;

/**
 * <pre>
 * S3 멀티파트 업로드를 지원하기 위한 클래스
 * </pre>
 *
 * @author Sang-cheon Park
 * @version 1.0
 */
public class MultiPartFileUploader extends Thread {
    private UploadPartRequest uploadRequest;
    private PartETag partETag;
    private AmazonS3 s3;

    public MultiPartFileUploader(UploadPartRequest uploadRequest, AmazonS3 s3) {
        this.uploadRequest = uploadRequest;
        this.s3 = s3;
    }

    @Override
    public void run() {
        partETag = s3.uploadPart(uploadRequest).getPartETag();
    }

    public PartETag getPartETag() {
        return partETag;
    }

    public void upload() {
        start();
    }
}
//end of MultiPartFileUploader.java