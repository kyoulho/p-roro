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
package io.playce.roro.mig.gcp.storage;

import com.google.api.gax.paging.Page;
import com.google.api.services.storage.StorageScopes;
import com.google.cloud.WriteChannel;
import com.google.cloud.storage.*;
import io.playce.roro.mig.gcp.common.BaseClient;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
@Slf4j
public class StorageClient extends BaseClient {
    private Storage storage;

    public StorageClient(String projectId, String accountKey, String region) {
        super(projectId, accountKey, StorageScopes.all());
        this.region = region;

        storage = StorageOptions.newBuilder().setCredentials(credentials)
                .setProjectId(projectId).build().getService();
        //    storage = new com.google.api.services.storage.Storage.Builder(httpTransport, jsonFactory, requestInitializer)
        //        .setApplicationName("roro-application")
        //        .build();
    }

    public Bucket getBucket(String bucketName) {
        return storage.get(bucketName);
    }

    public Bucket createBucket(String bucketName, String folderName) {
        Bucket bucket = getBucket(bucketName);

        if (bucket == null) {
            bucket = storage.create(
                    BucketInfo.newBuilder(bucketName)
                            .setStorageClass(StorageClass.STANDARD)
                            .setLocation(region)
                            .build());

            log.debug("Created bucket "
                    + bucket.getName()
                    + " in "
                    + bucket.getLocation()
                    + " with storage class "
                    + bucket.getStorageClass());

        }

        Blob folderCreated = bucket.create(folderName + "/", "".getBytes());
        log.debug("Created folder {}", bucket.getName() + "/" + folderCreated.getName());
        return bucket;
    }

    public void uploadObjectDirect(Bucket bucket, String filePath) {
        Path path = Paths.get(filePath);
        try {
            BlobId blobId = BlobId.of(bucket.getName(), path.getFileName().toFile().toString());
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("application/x-tar").build();

            File file = new File(filePath);
            if (file.length() < 1_000_000) {
                byte[] bytes = Files.readAllBytes(file.toPath());
                storage.create(blobInfo, bytes);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void uploadObject(String bucketName, String folderName, File file) {
        try {
            log.debug("Start upload file " + file.getAbsolutePath() + " to " + bucketName + "/" + folderName + "/" + file.getName());
            BlobId blobId = BlobId.of(bucketName, folderName + "/" + file.getName());
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();

            // For big files:
            // When content is not available or large (1MB or more) it is recommended to write it in chunks via the blob's channel writer.
            try (WriteChannel writer = storage.writer(blobInfo)) {

                byte[] buffer = new byte[102400];
                int limit = 0;
                InputStream input = Files.newInputStream(file.toPath());

                while ((limit = input.read(buffer)) >= 0) {
                    writer.write(ByteBuffer.wrap(buffer, 0, limit));
                }
            } catch (Exception e) {
                log.debug("Unhandled error while upload archive file", e.getMessage());
                throw e;
            }

            log.debug("Upload done " + file.getAbsolutePath());
//      storage.create(blobInfo, Files.readAllBytes(path),Files.readAllBytes(path).length,Files.readAllBytes(path).length);
//      System.out.println(
//          "File " + filePath + " uploaded to bucket " + bucket.getName() + " as " + path.getFileName().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteFolder(String bucketName, String folderName) {
        try {
//      StorageBatch batch = storage.batch();
            Page<Blob> blobPage = storage.list(bucketName, Storage.BlobListOption.prefix(folderName + "/"));
            for (Blob blob : blobPage.iterateAll()) {
                blob.delete();
//          batch.delete(blob.getBlobId());
            }
//        batch.submit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteBucketIfEmpty(String bucketName) {
        try {
            Iterable<Blob> blobs = storage.list(bucketName,
                    Storage.BlobListOption.prefix("")).iterateAll();

            if (!blobs.iterator().hasNext()) {
                storage.delete(bucketName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
//end of StorageClient.java