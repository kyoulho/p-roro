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

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.HttpMethod;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;
import io.playce.roro.common.util.CommandUtil;
import io.playce.roro.mig.aws.ec2.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.*;
import java.util.*;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
@Slf4j
public class S3Client {

    private static final long MAX_THREADS = 20L;
    private static final long MIN_PART_SIZE = 10 * 1024 * 1024;
    private static final long MAX_PART_SIZE = 5000000000L;

    private AmazonS3 s3;

    private TransferManager tm;

    public S3Client(AWSCredentials credentials, String region) {
        if (StringUtils.isEmpty(region)) {
            log.warn("Please check 'BUCKET_REGION' or 'roro.migration.bucket.region' property has been set in setenv.sh ");
        }

        //*
        s3 = AmazonS3ClientBuilder.standard()
                .withRegion(Regions.fromName(region))
                .withCredentials(new com.amazonaws.auth.AWSStaticCredentialsProvider(credentials))
                // com.amazonaws.services.s3.model.AmazonS3Exception: S3 Transfer Acceleration is not configured on this bucket
                //.enableAccelerateMode()
                .build();
		/*/
		// Using the Default Credential Provider Chain
		// https://docs.aws.amazon.com/ko_kr/sdk-for-java/v1/developer-guide/credentials.html
		s3 = AmazonS3ClientBuilder.standard()
			.withRegion(Regions.fromName(region))
			.withCredentials(new DefaultAWSCredentialsProviderChain())
			.build();
		//*/

        tm = TransferManagerBuilder.standard()
                .withS3Client(s3)
                .build();
    }

    public static void main(String[] args) {
        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger("org.apache.http");
        root.setLevel(ch.qos.logback.classic.Level.INFO);

        root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger("com.amazonaws");
        root.setLevel(ch.qos.logback.classic.Level.INFO);

        S3Client s3Client = new S3Client(new BasicAWSCredentials("xxxx", "xxxx"), "ap-northeast-2");

        s3Client.putObjectAsMultiPart("roro-bucket-dev", "test", new File("/Users/nices96/Downloads/test.raw"));
        // s3Client.putObjectAsMultiPart("roro-bucket-dev", "test", new File("/Users/nices96/Downloads/image.png"));
    }

    /**
     * <pre>
     * 주어진 버킷이 존재하지 않을 경우 버킷을 신규 생성한다.
     * </pre>
     *
     * @param bucketName
     */
    public void createBucketUnlessExist(String bucketName) {
        if (!s3.doesBucketExistV2(bucketName)) {
            s3.createBucket(bucketName);
        }

		/*
		// Enable Transfer Acceleration for the specified bucket.
		// com.amazonaws.services.s3.model.AmazonS3Exception: S3 Transfer Acceleration is not configured on this bucket
		s3.setBucketAccelerateConfiguration(
			new SetBucketAccelerateConfigurationRequest(bucketName,
				new BucketAccelerateConfiguration(
					BucketAccelerateStatus.Enabled)));
		//*/
    }

    /**
     * <pre>
     * 해당 버킷에 폴더를 생성한다.
     * </pre>
     *
     * @param bucketName
     * @param folderName
     */
    public void createFolder(String bucketName, String folderName) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(0);
        InputStream emptyContent = new ByteArrayInputStream(new byte[0]);
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, folderName + "/", emptyContent, metadata);
        s3.putObject(putObjectRequest);
    }

    /**
     * <pre>
     * 버킷에 오브젝트를 업로드한다.
     * </pre>
     *
     * @param bucketName
     * @param key
     * @param file
     *
     * @throws Exception
     */
    public void cp(String bucketName, String key, File file) throws Exception {
        String result = null;
        ByteArrayOutputStream baos = null;

        try {
            DefaultExecutor executor = new DefaultExecutor();
            baos = new ByteArrayOutputStream();
            PumpStreamHandler streamHandler = new PumpStreamHandler(baos);
            executor.setStreamHandler(streamHandler);

            CommandLine cl = CommandUtil.getCommandLine(CommandUtil.findCommand("sudo"),
                    CommandUtil.findCommand("aws"),
                    "s3",
                    "cp",
                    file.getAbsolutePath(),
                    "s3://" + bucketName + "/" + key);

            /*
            CommandLine cl = new CommandLine(CommandUtil.findCommand("sudo"))
                    .addArguments(CommandUtil.findCommand("aws"))
                    .addArguments("s3")
                    .addArguments("cp")
                    .addArguments(file.getAbsolutePath())
                    .addArguments("s3://" + bucketName + "/" + key);
            */

            log.debug("cp()'s CommandLine : {}", cl);

            int exitCode = executor.execute(cl);

            if (exitCode == 0) {
                result = baos.toString();
            } else {
                throw new Exception(baos.toString());
            }

            log.debug("cp()'s result : {}", result);
        } catch (Exception e) {
            log.error("Error while executing cp(). Error Log => [{}]", baos.toString());
            throw e;
        } finally {
            IOUtils.closeQuietly(baos);
        }
    }

    /**
     * <pre>
     * 버킷에 오브젝트를 업로드한다.
     * </pre>
     *
     * @param bucketName
     * @param key
     * @param file
     */
    public void putObject(String bucketName, String key, File file) {
        /*
         * Put the object in S3
         */
        try {
            log.debug("[S3 Put Object] bucketName : {}, key : {}, file : {}", new Object[]{bucketName, key, file.getAbsolutePath()});

            s3.putObject(bucketName, key, file);
        } catch (AmazonServiceException ase) {
            log.error("Caught an AmazonServiceException, which means your request made it " +
                    "to Amazon S3, but was rejected with an error response for some reason.");
            log.error("Error Message:    [{}]", ase.getMessage());
            log.error("HTTP Status Code: [{}]", ase.getStatusCode());
            log.error("AWS Error Code:   [{}]", ase.getErrorCode());
            log.error("Error Type:       [{}]", ase.getErrorType());
            log.error("Request ID:       [{}]", ase.getRequestId());

            throw ase;
        } catch (AmazonClientException ace) {
            log.error("Caught an AmazonClientException, which means the client encountered " +
                    "an internal error while trying to communicate with S3, such as not being able to access the network.");
            log.error("Error Message: [{}]", ace.getMessage());

            throw ace;
        }
    }

    /**
     * <pre>
     * 버킷에 오브젝트를 업로드한다.
     * </pre>
     *
     * @param bucketName
     * @param key
     * @param contents
     */
    public void putObject(String bucketName, String key, String contents) {
        /*
         * Obtain the Content length of the contents for S3 header
         */
        Long contentLength = Long.valueOf(contents.getBytes().length);

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(contentLength);
        metadata.setContentType("text/plain; charset=utf-8");

        /*
         * obtain the contents as input stream
         */
        InputStream inputStream = new ByteArrayInputStream(contents.getBytes());

        /*
         * Put the object in S3
         */
        try {
            s3.putObject(new PutObjectRequest(bucketName, key, inputStream, metadata));
        } catch (AmazonServiceException ase) {
            log.error("Caught an AmazonServiceException, which means your request made it " +
                    "to Amazon S3, but was rejected with an error response for some reason.");
            log.error("Error Message:    [{}]", ase.getMessage());
            log.error("HTTP Status Code: [{}]", ase.getStatusCode());
            log.error("AWS Error Code:   [{}]", ase.getErrorCode());
            log.error("Error Type:       [{}]", ase.getErrorType());
            log.error("Request ID:       [{}]", ase.getRequestId());

            throw ase;
        } catch (AmazonClientException ace) {
            log.error("Caught an AmazonClientException, which means the client encountered " +
                    "an internal error while trying to communicate with S3, such as not being able to access the network.");
            log.error("Error Message: [{}]", ace.getMessage());

            throw ace;
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    /**
     * <pre>
     * 버킷에 오브젝트를 멀티파트로 업로드한다.
     * </pre>
     *
     * @param bucketName
     * @param folderName
     * @param file
     */
    public void putObjectAsMultiPart(String bucketName, String folderName, File file) {
        /*
        long partSize = file.length() / MAX_THREADS;

        if (partSize < MIN_PART_SIZE) {
            partSize = MIN_PART_SIZE;
        }

        // https://docs.aws.amazon.com/AmazonS3/latest/userguide/upload-objects.html
        // With a single PUT operation, you can upload a single object up to 5 GB in size.
        if (partSize > MAX_PART_SIZE) {
            partSize = MAX_PART_SIZE;
        }

        putObjectAsMultiPart(bucketName, folderName, file, partSize);
        /*/
        try {
            log.info("Upload will be start for file " + "\"" + folderName + "/" + file.getName() + "\".");

            // TransferManager processes all transfers asynchronously,
            // so this call returns immediately.
            //tm.getConfiguration().setMinimumUploadPartSize(50 * 1024 * 1024);
            Upload upload = tm.upload(bucketName, folderName + "/" + file.getName(), file);

            String migrationId = file.getParentFile().getParentFile().getName();
            log.info("Migration ID : [{}], bucketName : [{}]", migrationId, bucketName);
            log.info("Migration ID : [{}], folderName : [{}]", migrationId, folderName);
            log.info("Migration ID : [{}], file.getAbsolutePath() : [{}]", migrationId, file.getAbsolutePath());
            log.info("Migration ID : [{}], file.length() : [{}]", migrationId, file.length());

            upload.addProgressListener(new UploadProgressListener(file, upload));

            // Optionally, wait for the upload to finish before continuing.
            upload.waitForCompletion();
            log.info("Upload completed for file " + "\"" + folderName + "/" + file.getName() + "\"" + ", " + file.length()
                    + " bytes data has been transferred.");
        } catch (Exception e) {
            log.info("Upload failed for file " + "\"" + folderName + "/" + file.getName() + "\".");
        }
        //*/
    }

    /**
     * <pre>
     * 버킷에 오브젝트를 멀티파트로 업로드한다.
     * </pre>
     *
     * @param bucketName
     * @param folderName
     * @param file
     * @param partSize
     */
    @Deprecated
    private void putObjectAsMultiPart(String bucketName, String folderName, File file, long partSize) {
        List<PartETag> partETags = new ArrayList<PartETag>();
        List<MultiPartFileUploader> uploaders = new ArrayList<MultiPartFileUploader>();

        // Step 1: Initialize.
        InitiateMultipartUploadRequest initRequest = new InitiateMultipartUploadRequest(bucketName, folderName + "/" + file.getName());
        InitiateMultipartUploadResult initResponse = s3.initiateMultipartUpload(initRequest);
        long contentLength = file.length();

        try {
            createFolder(bucketName, folderName);

            // Step 2: Upload parts.
            long filePosition = 0;
            for (int i = 1; filePosition < contentLength; i++) {
                // Last part can be less than part size. Adjust part size.
                partSize = Math.min(partSize, (contentLength - filePosition));

                // Create request to upload a part.
                UploadPartRequest uploadRequest = new UploadPartRequest().withBucketName(bucketName)
                        .withKey(folderName + "/" + file.getName()).withUploadId(initResponse.getUploadId()).withPartNumber(i)
                        .withFileOffset(filePosition).withFile(file).withPartSize(partSize);

                uploadRequest.setGeneralProgressListener(new UploadProgressListener(file, i, partSize));

                // Upload part and add response to our list.
                MultiPartFileUploader uploader = new MultiPartFileUploader(uploadRequest, s3);
                uploaders.add(uploader);
                uploader.upload();

                filePosition += partSize;
            }

            for (MultiPartFileUploader uploader : uploaders) {
                uploader.join();
                partETags.add(uploader.getPartETag());
            }

            String migrationId = file.getParentFile().getParentFile().getName();

            log.info("[{}] buckerName : [{}]", migrationId, bucketName);
            log.info("[{}] folderName : [{}]", migrationId, folderName);
            log.info("[{}] file.getAbsolutePath() : [{}]", migrationId, file.getAbsolutePath());
            log.info("[{}] file.length() : [{}]", migrationId, file.length());

            // Step 3: complete.
            CompleteMultipartUploadRequest compRequest = new CompleteMultipartUploadRequest(bucketName, folderName + "/" + file.getName(),
                    initResponse.getUploadId(), partETags);

            s3.completeMultipartUpload(compRequest);
        } catch (Throwable t) {
            log.error("Unable to put object as multipart to Amazon S3 for file " + folderName + "/" + file.getName(), t);
            s3.abortMultipartUpload(new AbortMultipartUploadRequest(bucketName, folderName + "/" + file.getName(), initResponse.getUploadId()));
        }
    }

    /**
     * <pre>
     * 해당 오브젝트에 대한 PresignedUrl을 생성한다.
     * </pre>
     *
     * @param bucketName
     * @param key
     *
     * @return
     */
    public String generatePresignedUrl(String bucketName, String key) {
        return generatePresignedUrl(bucketName, key, null);
    }

    /**
     * <pre>
     * 해당 오브젝트에 대한 PresignedUrl을 생성한다.
     * </pre>
     *
     * @param bucketName
     * @param key
     * @param method
     *
     * @return
     */
    public String generatePresignedUrl(String bucketName, String key, HttpMethod method) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.add(Calendar.DATE, 7);
        Date signedUrlExpiration = cal.getTime();

        if (method == null) {
            return s3.generatePresignedUrl(bucketName, key, signedUrlExpiration).toString();
        } else {
            return s3.generatePresignedUrl(bucketName, key, signedUrlExpiration, method).toString();
        }
    }

    /**
     * <pre>
     * ec2-import-instance, ec2-import-volume시 사용될 S3 오브젝트에 대한 manifest.xml 파일 내용을 작성한다.
     * </pre>
     *
     * @param bucketName
     * @param folderName
     * @param file
     * @param volumeSize
     *
     * @return
     */
    public String getManifest(String bucketName, String folderName, File file, long volumeSize) {
        StringBuilder prefix = new StringBuilder();
        prefix.append(folderName + "/");
        prefix.append(file.getName());

        String signedUrl = generatePresignedUrl(bucketName, prefix.toString(), HttpMethod.DELETE);

        Manifest manifest = new Manifest();
        manifest.setVersion("2012-03-13");
        manifest.setFileFormat("RAW");
        manifest.setSelfDestructUrl(signedUrl);

        Importer importer = new Importer();
        importer.setName(file.getName());
        importer.setVersion("1.0.0");
        importer.setRelease("2012-03-13");
        manifest.setImporter(importer);

        Import impo = new Import();
        impo.setSize(file.length());
        impo.setVolumeSize(volumeSize);

        Parts parts = new Parts();
        parts.setCount(1);

        Part part = new Part();
        part.setIndex(0);
        part.setKey(folderName + "/" + file.getName());
        part.setHeadUrl(generatePresignedUrl(bucketName, prefix.toString(), HttpMethod.HEAD));
        part.setGetUrl(generatePresignedUrl(bucketName, prefix.toString(), HttpMethod.GET));
        part.setDeleteUrl(generatePresignedUrl(bucketName, prefix.toString(), HttpMethod.DELETE));

        ByteRange range = new ByteRange();
        range.setStart(0L);
        range.setEnd(file.length());
        part.setByteRange(range);

        parts.getPart().add(part);
        impo.setParts(parts);
        manifest.setImport(impo);

        StringWriter sw = null;
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance("io.playce.roro.mig.aws.ec2.entity");
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            sw = new StringWriter();
            jaxbMarshaller.marshal(manifest, sw);
        } catch (Exception e) {
            log.error("marshalling exception occurred.", e);
        }

        String migrationId = file.getParentFile().getParentFile().getName();
        log.info("[{}] manifest : [{}]", migrationId, sw.toString());

        return sw.toString();
    }

    public void deleteObject(String bucketName, String keyName) {
        s3.deleteObject(new DeleteObjectRequest(bucketName, keyName));
    }

    public void deleteFolder(String bucketName, String folderName) {
        ListObjectsRequest listObjectsRequest = new ListObjectsRequest().withBucketName(bucketName).withPrefix(folderName);

        ObjectListing objectListing = s3.listObjects(listObjectsRequest);

        List<S3ObjectSummary> summaryList = objectListing.getObjectSummaries();

        for (int i = summaryList.size() - 1; i >= 0; i--) {
            log.debug("[{}] will be delete in S3 bucket({})", summaryList.get(i).getKey(), bucketName);
            deleteObject(bucketName, summaryList.get(i).getKey());
        }
    }

    public void deleteBucketIfEmpty(String bucketName) {
        ObjectListing objectListing = s3.listObjects(bucketName);

        if (objectListing.getObjectSummaries().size() <= 0) {
            log.debug("S3 bucket({}) will be delete.", bucketName);
            s3.deleteBucket(bucketName);
        }
    }
}
//end of S3Client.java