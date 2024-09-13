/*
 * Copyright 2021 The playce-roro-v3 Project.
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
 * SangCheon Park   Dec 08, 2021		    First Draft.
 */
package io.playce.roro.api.domain.project.service;

import io.playce.roro.api.common.error.ErrorCode;
import io.playce.roro.api.common.error.exception.ResourceNotFoundException;
import io.playce.roro.api.common.error.exception.RoRoApiException;
import io.playce.roro.api.common.util.WebUtil;
import io.playce.roro.common.code.Domain101;
import io.playce.roro.common.dto.project.ProjectRequest;
import io.playce.roro.common.dto.project.ProjectResponse;
import io.playce.roro.common.dto.project.ProjectSimpleResponse;
import io.playce.roro.jpa.entity.ProjectMaster;
import io.playce.roro.jpa.entity.ServiceMaster;
import io.playce.roro.jpa.repository.ProjectMasterRepository;
import io.playce.roro.jpa.repository.ServiceMasterRepository;
import io.playce.roro.mybatis.domain.project.ProjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

import static io.playce.roro.api.common.CommonConstants.*;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ProjectService {

    private static final String DEFAULT_PROJECT_TYPE_CODE = "PROJECT";

    private final ProjectMasterRepository projectMasterRepository;
    private final ServiceMasterRepository serviceMasterRepository;
    private final ProjectMapper projectMapper;

    /**
     * <pre>
     * 프로젝트 목록 조회
     * </pre>
     *
     * @return
     */
    public List<ProjectResponse> getProjectList() {
        List<ProjectResponse> projectResponseList = projectMapper.getProjects();
        return projectResponseList;
    }

    /**
     * <pre>
     * 프로젝트 상세 조회
     * </pre>
     *
     * @return
     */
    public ProjectResponse getProject(Long projectId) {
        projectMasterRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project ID : " + projectId + " Not Found."));

        ProjectResponse projectResponse = projectMapper.getProject(projectId);
        return projectResponse;
    }

    /**
     * <pre>
     * 프로젝트 등록
     * </pre>
     *
     * @param projectRequest
     *
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ProjectSimpleResponse createProject(ProjectRequest projectRequest) {

        // 프로젝트명 중복체크
        int duplicatedProjectCount = projectMapper.getProjectCountByName(projectRequest.getProjectName(), null);
        if (duplicatedProjectCount > 0) {
            throw new RoRoApiException(ErrorCode.PROJECT_DUPLICATED_NAME);
        }

        ProjectMaster projectMaster = new ProjectMaster();
        projectMaster.setProjectName(projectRequest.getProjectName());
        projectMaster.setProjectTypeCode(DEFAULT_PROJECT_TYPE_CODE);
        projectMaster.setDescription(projectRequest.getDescription());
        projectMaster.setDeleteYn(Domain101.N.name());
        projectMaster.setRegistDatetime(new Date());
        projectMaster.setRegistUserId(WebUtil.getUserId());
        projectMaster.setModifyDatetime(new Date());
        projectMaster.setModifyUserId(WebUtil.getUserId());

        projectMaster = projectMasterRepository.save(projectMaster);

        ServiceMaster serviceMaster = new ServiceMaster();
        serviceMaster.setProjectId(projectMaster.getProjectId());
        serviceMaster.setServiceName(DEFAULT_SERVICE_NAME);
        serviceMaster.setBusinessCategoryCode(DEFAULT_SERVICE_BUSINESS_CATEGORY_CODE);
        serviceMaster.setBusinessCategoryName(DEFAULT_SERVICE_BUSINESS_CATEGORY_NAME);
        serviceMaster.setCustomerServiceCode(DEFAULT_SERVICE_CUSTOMER_SERVICE_CODE);
        serviceMaster.setCustomerServiceName(DEFAULT_SERVICE_CUSTOMER_SERVICE_NAME);
        serviceMaster.setMigrationTargetYn(Domain101.N.name());
        serviceMaster.setDeleteYn(Domain101.N.name());
        serviceMaster.setRegistDatetime(new Date());
        serviceMaster.setRegistUserId(WebUtil.getUserId());
        serviceMaster.setModifyDatetime(new Date());
        serviceMaster.setModifyUserId(WebUtil.getUserId());

        serviceMasterRepository.save(serviceMaster);

        ProjectSimpleResponse response = new ProjectSimpleResponse();
        response.setProjectId(projectMaster.getProjectId());
        response.setProjectName(projectMaster.getProjectName());

        return response;
    }

    /**
     * <pre>
     * 프로젝트 수정
     * </pre>
     *
     * @param projectId
     * @param projectRequest
     *
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ProjectSimpleResponse modifyProject(Long projectId, ProjectRequest projectRequest) {
        ProjectMaster projectMaster = projectMasterRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project ID : " + projectId + " Not Found."));

        // 프로젝트명 중복 체크
        int duplicatedProjectCount = projectMapper.getProjectCountByName(projectRequest.getProjectName(), projectMaster.getProjectName());
        if (duplicatedProjectCount > 0) {
            throw new RoRoApiException(ErrorCode.PROJECT_DUPLICATED_NAME);
        }

        projectMaster.setProjectName(projectRequest.getProjectName());
        projectMaster.setDescription(projectRequest.getDescription());
        projectMaster.setModifyDatetime(new Date());
        projectMaster.setModifyUserId(WebUtil.getUserId());

        ProjectSimpleResponse response = new ProjectSimpleResponse();
        response.setProjectId(projectMaster.getProjectId());
        response.setProjectName(projectMaster.getProjectName());

        return response;
    }

    /**
     * <pre>
     * 프로젝트 삭제
     * </pre>
     *
     * @param projectId
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteProject(Long projectId) {
        ProjectMaster projectMaster = projectMasterRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project ID : " + projectId + " Not Found."));

        projectMaster.setDeleteYn(Domain101.Y.name());
        projectMaster.setModifyDatetime(new Date());
        projectMaster.setModifyUserId(WebUtil.getUserId());
    }

    public String getProjectName(Long projectId) {
        ProjectMaster projectMaster = projectMasterRepository.findById(projectId).orElseGet(ProjectMaster::new);

        if (StringUtils.isEmpty(projectMaster.getProjectName())) {
            return "";
        } else {
            return projectMaster.getProjectName();
        }
    }

}
//end of ProjectService.java