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
 * SangCheon Park   Feb 09, 2022		    First Draft.
 */
package io.playce.roro.api.domain.targetcloud.service;

import io.playce.roro.api.common.error.ErrorCode;
import io.playce.roro.api.common.error.exception.ResourceNotFoundException;
import io.playce.roro.api.common.error.exception.RoRoApiException;
import io.playce.roro.api.common.util.WebUtil;
import io.playce.roro.common.code.Domain1009;
import io.playce.roro.common.code.Domain101;
import io.playce.roro.common.dto.targetcloud.CredentialDto.CredentialRequest;
import io.playce.roro.common.dto.targetcloud.CredentialDto.CredentialResponse;
import io.playce.roro.common.dto.targetcloud.CredentialDto.CredentialSimpleResponse;
import io.playce.roro.common.property.CommonProperties;
import io.playce.roro.common.util.AES256Util;
import io.playce.roro.jpa.entity.CredentialMaster;
import io.playce.roro.jpa.repository.CredentialMasterRepository;
import io.playce.roro.mig.aws.auth.BasicAWSCredentials;
import io.playce.roro.mig.aws.ec2.EC2Client;
import io.playce.roro.mig.gcp.auth.GCPCredentials;
import io.playce.roro.mig.gcp.compute.ComputeClient;
import io.playce.roro.mybatis.domain.common.credential.CredentialMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CredentialService {

    private final CredentialMapper credentialMapper;

    private final CredentialMasterRepository credentialMasterRepository;

    /**
     * <pre>
     * 등록된 Credential 목록을 조회한다.
     * </pre>
     *
     * @param projectId
     * @param credentialTypeCode
     *
     * @return
     */
    public List<CredentialResponse> getCredentials(Long projectId, String credentialTypeCode) {
        // credentialTypeCode는 AWS / GCP만 지원한다.
        if (!Domain1009.AWS.name().equals(credentialTypeCode) && !Domain1009.GCP.name().equals(credentialTypeCode)) {
            throw new RoRoApiException(ErrorCode.TC_UNSUPPORTED_TYPE, credentialTypeCode);
        }

        List<CredentialResponse> credentialList = credentialMapper.getCredentials(projectId, credentialTypeCode);

        // credentialTypeCode가 AWS인 경우 secretKey를 masking 처리한다.
        if (Domain1009.AWS.name().equals(credentialTypeCode)) {
            for (CredentialResponse credential : credentialList) {
                if (StringUtils.isNotEmpty(credential.getAccessKey())) {
                    credential.setAccessKey(AES256Util.decrypt(credential.getAccessKey()));
                }
                if (StringUtils.isNotEmpty(credential.getSecretKey())) {
                    credential.setSecretKey(AES256Util.decrypt(credential.getSecretKey()));
                }

                if (StringUtils.isNotEmpty(credential.getSecretKey())) {
                    credential.setSecretKey(MaskString(credential.getSecretKey(), 5, credential.getSecretKey().length(), '*'));
                }
            }
        }

        return credentialList;
    }

    /**
     * <pre>
     * Credential 상세 정보를 조회한다.
     * </pre>
     *
     * @param projectId
     * @param credentialId
     *
     * @return
     */
    public CredentialResponse getCredential(Long projectId, Long credentialId) {
        CredentialMaster credentialMaster = credentialMasterRepository.findByProjectIdAndCredentialId(projectId, credentialId);

        if (credentialMaster == null) {
            throw new ResourceNotFoundException("Credential ID : " + credentialId + " Not Found in Project ID : " + projectId);
        }

        if (!Domain1009.AWS.name().equals(credentialMaster.getCredentialTypeCode()) && !Domain1009.GCP.name().equals(credentialMaster.getCredentialTypeCode())) {
            throw new RoRoApiException(ErrorCode.TC_UNSUPPORTED_TYPE, credentialMaster.getCredentialTypeCode());
        }

        CredentialResponse credential = credentialMapper.getCredential(projectId, credentialId);

        // credentialTypeCode가 AWS인 경우 secretKey를 masking 처리한다.
        if (Domain1009.AWS.name().equals(credential.getCredentialTypeCode())) {
            if (StringUtils.isNotEmpty(credential.getAccessKey())) {
                credential.setAccessKey(AES256Util.decrypt(credential.getAccessKey()));
            }
            if (StringUtils.isNotEmpty(credential.getSecretKey())) {
                credential.setSecretKey(AES256Util.decrypt(credential.getSecretKey()));
            }

            if (StringUtils.isNotEmpty(credential.getSecretKey())) {
                credential.setSecretKey(MaskString(credential.getSecretKey(), 5, credential.getSecretKey().length(), '*'));
            }
        }

        return credential;
    }

    /**
     * <pre>
     * 신규 Credential을 등록한다.
     * </pre>
     *
     * @param projectId
     * @param credentialRequest
     * @param keyFile
     *
     * @return
     */
    @SneakyThrows(IOException.class)
    @Transactional(rollbackFor = Exception.class)
    public CredentialSimpleResponse createCredential(Long projectId, CredentialRequest credentialRequest, MultipartFile keyFile) {
        // credentialTypeCode는 AWS / GCP만 지원한다.
        if (!Domain1009.AWS.name().equals(credentialRequest.getCredentialTypeCode()) && !Domain1009.GCP.name().equals(credentialRequest.getCredentialTypeCode())) {
            throw new RoRoApiException(ErrorCode.TC_UNSUPPORTED_TYPE, credentialRequest.getCredentialTypeCode());
        }

        CredentialMaster credentialMaster = new CredentialMaster();
        credentialMaster.setCredentialName(credentialRequest.getCredentialName());
        credentialMaster.setProjectId(projectId);
        credentialMaster.setCredentialTypeCode(credentialRequest.getCredentialTypeCode());
        if (StringUtils.isNotEmpty(credentialRequest.getAccessKey())) {
            credentialMaster.setAccessKey(AES256Util.encrypt(credentialRequest.getAccessKey()));
        }
        if (StringUtils.isNotEmpty(credentialRequest.getSecretKey())) {
            credentialMaster.setSecretKey(AES256Util.encrypt(credentialRequest.getSecretKey()));
        }
        credentialMaster.setDeleteYn(Domain101.N.name());
        credentialMaster.setRegistUserId(WebUtil.getUserId());
        credentialMaster.setRegistDatetime(new Date());
        credentialMaster.setModifyUserId(WebUtil.getUserId());
        credentialMaster.setModifyDatetime(new Date());

        // GCP용 키 파일이 존재하는 경우
        if (Domain1009.GCP.name().equals(credentialRequest.getCredentialTypeCode())) {
            if (keyFile != null) {
                String fileNameExtension = FilenameUtils.getExtension(keyFile.getOriginalFilename());

                String hashFileName = RandomStringUtils.randomAlphanumeric(32);

                File destinationFile = new File(getCredentialFilePath() + hashFileName + "." + fileNameExtension);

                credentialMaster.setKeyFileName(keyFile.getOriginalFilename());
                credentialMaster.setKeyFilePath(destinationFile.getAbsolutePath());

                FileUtils.forceMkdirParent(destinationFile);
                keyFile.transferTo(destinationFile);

                try {
                    GCPCredentials gcpCredentials = new GCPCredentials(credentialMaster.getKeyFilePath());
                    new ComputeClient(gcpCredentials.getProjectId(), gcpCredentials.getAccountKey());
                } catch (Exception e) {
                    throw new RoRoApiException(ErrorCode.TC_INVALID_KEY_FILE);
                }
            } else {
                throw new RoRoApiException(ErrorCode.TC_KEY_FILE_NOT_FOUND);
            }
        } else {
            try {
                BasicAWSCredentials credential = new BasicAWSCredentials(credentialMaster.getAccessKey(), credentialMaster.getSecretKey());
                EC2Client ec2 = new EC2Client(credential, "us-west-1");
                ec2.getAvailabilityZoneList();
            } catch (Exception e) {
                throw new RoRoApiException(ErrorCode.TC_INVALID_CREDENTIAL);
            }
        }

        credentialMaster = credentialMasterRepository.save(credentialMaster);

        CredentialSimpleResponse response = new CredentialSimpleResponse();
        response.setCredentialId(credentialMaster.getCredentialId());
        response.setCredentialName(credentialMaster.getCredentialName());
        response.setCredentialTypeCode(credentialMaster.getCredentialTypeCode());

        return response;
    }

    /**
     * <pre>
     * Credential 정보를 수정한다.
     * </pre>
     *
     * @param projectId
     * @param credentialId
     * @param credentialRequest
     * @param keyFile
     *
     * @return
     */
    @SneakyThrows(IOException.class)
    @Transactional(rollbackFor = Exception.class)
    public CredentialSimpleResponse modifyCredential(Long projectId, Long credentialId, CredentialRequest credentialRequest, MultipartFile keyFile) {
        CredentialMaster credentialMaster = credentialMasterRepository.findByProjectIdAndCredentialId(projectId, credentialId);

        if (credentialMaster == null) {
            throw new ResourceNotFoundException("Credential ID : " + credentialId + " Not Found in Project ID : " + projectId);
        }

        if (!Domain1009.AWS.name().equals(credentialMaster.getCredentialTypeCode()) && !Domain1009.GCP.name().equals(credentialMaster.getCredentialTypeCode())) {
            throw new RoRoApiException(ErrorCode.TC_UNSUPPORTED_TYPE, credentialMaster.getCredentialTypeCode());
        }

        if (!Domain1009.AWS.name().equals(credentialRequest.getCredentialTypeCode()) && !Domain1009.GCP.name().equals(credentialRequest.getCredentialTypeCode())) {
            throw new RoRoApiException(ErrorCode.TC_UNSUPPORTED_TYPE, credentialRequest.getCredentialTypeCode());
        }

        credentialMaster.setCredentialName(credentialRequest.getCredentialName());
        credentialMaster.setProjectId(projectId);
        credentialMaster.setCredentialTypeCode(credentialRequest.getCredentialTypeCode());
        credentialMaster.setModifyUserId(WebUtil.getUserId());
        credentialMaster.setModifyDatetime(new Date());

        if (Domain1009.AWS.name().equals(credentialRequest.getCredentialTypeCode())) {
            if (StringUtils.isNotEmpty(credentialRequest.getAccessKey())) {
                credentialMaster.setAccessKey(AES256Util.encrypt(credentialRequest.getAccessKey()));
            }

            if (StringUtils.isNotEmpty(credentialRequest.getSecretKey())) {
                credentialMaster.setSecretKey(AES256Util.encrypt(credentialRequest.getSecretKey()));
            }
            credentialMaster.setKeyFileName(null);
            credentialMaster.setKeyFilePath(null);
        }

        // GCP용 키 파일이 존재하는 경우
        if (Domain1009.GCP.name().equals(credentialRequest.getCredentialTypeCode())) {
            if (keyFile != null) {
                String fileNameExtension = FilenameUtils.getExtension(keyFile.getOriginalFilename());

                String hashFileName = RandomStringUtils.randomAlphanumeric(32);

                File destinationFile = new File(getCredentialFilePath() + hashFileName + "." + fileNameExtension);

                credentialMaster.setAccessKey(null);
                credentialMaster.setSecretKey(null);
                credentialMaster.setKeyFileName(keyFile.getOriginalFilename());
                credentialMaster.setKeyFilePath(destinationFile.getAbsolutePath());

                FileUtils.forceMkdirParent(destinationFile);
                keyFile.transferTo(destinationFile);

                try {
                    GCPCredentials gcpCredentials = new GCPCredentials(credentialMaster.getKeyFilePath());
                    new ComputeClient(gcpCredentials.getProjectId(), gcpCredentials.getAccountKey());
                } catch (Exception e) {
                    throw new RoRoApiException(ErrorCode.TC_INVALID_KEY_FILE);
                }
            }
        } else {
            try {
                BasicAWSCredentials credential = new BasicAWSCredentials(credentialMaster.getAccessKey(), credentialMaster.getSecretKey());
                EC2Client ec2 = new EC2Client(credential, "us-west-1");
                ec2.getAvailabilityZoneList();
            } catch (Exception e) {
                throw new RoRoApiException(ErrorCode.TC_INVALID_CREDENTIAL);
            }
        }

        credentialMasterRepository.save(credentialMaster);

        CredentialSimpleResponse response = new CredentialSimpleResponse();
        response.setCredentialId(credentialMaster.getCredentialId());
        response.setCredentialName(credentialMaster.getCredentialName());
        response.setCredentialTypeCode(credentialMaster.getCredentialTypeCode());

        return response;
    }

    /**
     * <pre>
     * Credential 상세 정보를 삭제한다.
     * </pre>
     *
     * @param projectId
     * @param credentialId
     *
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteCredential(Long projectId, Long credentialId) {
        CredentialMaster credentialMaster = credentialMasterRepository.findByProjectIdAndCredentialId(projectId, credentialId);

        if (credentialMaster == null) {
            throw new ResourceNotFoundException("Credential ID : " + credentialId + " Not Found in Project ID : " + projectId);
        }

        if (!Domain1009.AWS.name().equals(credentialMaster.getCredentialTypeCode()) && !Domain1009.GCP.name().equals(credentialMaster.getCredentialTypeCode())) {
            throw new RoRoApiException(ErrorCode.TC_UNSUPPORTED_TYPE, credentialMaster.getCredentialTypeCode());
        }

        credentialMaster.setDeleteYn(Domain101.Y.name());
        credentialMaster.setModifyDatetime(new Date());
        credentialMaster.setModifyUserId(WebUtil.getUserId());
    }

    /**
     * <pre>
     * credentialTypeCode가 AWS인 경우 secretKey를 masking 처리한다.
     * </pre>
     *
     * @param strText
     * @param start
     * @param end
     * @param maskChar
     *
     * @return
     */
    private String MaskString(String strText, int start, int end, char maskChar) {
        if (StringUtils.isEmpty(strText)) {
            return "";
        }

        if (start < 0) {
            start = 0;
        }

        if (end > strText.length()) {
            end = strText.length();
        }

        int maskLength = end - start;

        if (maskLength == 0) {
            return strText;
        }

        String strMaskString = StringUtils.repeat(maskChar, maskLength);

        return StringUtils.overlay(strText, strMaskString, start, end);
    }

    /**
     * <pre>
     * GCP 용 key file이 저장될 경로
     * </pre>
     *
     * @return
     */
    private String getCredentialFilePath() {
        return CommonProperties.getWorkDir() + File.separator + "credentials" + File.separator;
    }
}
//end of CredentialService.java