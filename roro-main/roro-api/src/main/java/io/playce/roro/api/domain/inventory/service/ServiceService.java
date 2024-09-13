package io.playce.roro.api.domain.inventory.service;/*
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
 * Hoon Oh          11월 30, 2021		First Draft.
 */

import io.playce.roro.api.common.CommonConstants;
import io.playce.roro.api.common.error.ErrorCode;
import io.playce.roro.api.common.error.exception.ResourceNotFoundException;
import io.playce.roro.api.common.error.exception.RoRoApiException;
import io.playce.roro.api.common.util.WebUtil;
import io.playce.roro.api.domain.inventory.service.helper.ServiceCreateExcel;
import io.playce.roro.api.domain.inventory.service.helper.ServiceHelper;
import io.playce.roro.common.dto.common.excel.ListToExcelDto;
import io.playce.roro.common.dto.common.label.Label;
import io.playce.roro.common.dto.inventory.application.ApplicationResponse;
import io.playce.roro.common.dto.inventory.database.DatabaseEngineListResponseDto;
import io.playce.roro.common.dto.inventory.manager.Manager;
import io.playce.roro.common.dto.inventory.middleware.MiddlewareResponse;
import io.playce.roro.common.dto.inventory.server.ServerResponse;
import io.playce.roro.common.dto.inventory.service.*;
import io.playce.roro.common.util.ExcelUtil;
import io.playce.roro.jpa.entity.ServiceLabel;
import io.playce.roro.jpa.entity.ServiceManager;
import io.playce.roro.jpa.entity.ServiceMaster;
import io.playce.roro.jpa.repository.*;
import io.playce.roro.mybatis.domain.common.label.LabelMapper;
import io.playce.roro.mybatis.domain.inventory.application.ApplicationMapper;
import io.playce.roro.mybatis.domain.inventory.inventory.InventoryMapper;
import io.playce.roro.mybatis.domain.inventory.service.ServiceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.playce.roro.api.common.CommonConstants.YES;
import static io.playce.roro.common.util.support.DistinctByKey.distinctByKey;

/**
 * <pre>
 *
 *
 * </pre>
 *
 * @author Hoon Oh
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceService {

    private final ServerService serverService;
    private final MiddlewareService middlewareService;
    private final ApplicationService applicationService;
    private final DatabaseService databaseService;

    private final ModelMapper modelMapper;
    private final ServiceMapper serviceMapper;
    private final LabelMapper labelMapper;
    private final InventoryMapper inventoryMapper;
    private final ApplicationMapper applicationMapper;

    private final ServiceHelper serviceHelper;

    private final ServiceCreateExcel serviceCreateExcel;

    private final ServiceMasterRepository serviceMasterRepository;
    private final LabelMasterRepository labelMasterRepository;
    private final UserMasterRepository userMasterRepository;
    private final ServiceLabelRepository serviceLabelRepository;
    private final ServiceManagerRepository serviceManagerRepository;


    public List<ServiceResponse> getServiceList(long projectId) {
        List<ServiceDetail> serviceMasters = serviceMapper.selectServiceList(projectId);

        List<ServiceResponse> serviceResponses = CollectionUtils.emptyIfNull(serviceMasters)
                .stream().map(i -> modelMapper.map(i, ServiceResponse.class))
                .collect(Collectors.toList());

        getServiceLabels(serviceResponses);
        getServiceManagers(serviceResponses);
        getResourceTotalCounts(serviceResponses);

        return serviceResponses;
    }


    public ServiceDetailResponse getService(long projectId, long serviceId) {
        ServiceDetail service = serviceMapper.selectService(projectId, serviceId);

        if (service == null) {
            throw new RoRoApiException(ErrorCode.INVENTORY_SERVICE_NOT_FOUND);
        }

        ServiceDetailResponse serviceDetailResponse = modelMapper.map(service, ServiceDetailResponse.class);
        getServiceLabel(serviceDetailResponse);
        getServiceManager(serviceDetailResponse);
        getResourceTotalCount(serviceDetailResponse);
        return serviceDetailResponse;

    }

    /**
     * https://cloud-osci.atlassian.net/browse/PCR-5593
     * 이중 서브밋 방지를 위한 방어코드로 @Transactional 애노테이션에는 synchronized가 동작하기 않기 때문에
     * 별도의 synchronized 메소드 내에서 @Transactional 메소드를 호출한다.
     */
    public synchronized ServiceMaster createService(long projectId, ServiceCreateRequest serviceCreateRequest) {
        return createServiceInternal(projectId, serviceCreateRequest);
    }

    @Transactional
    public ServiceMaster createServiceInternal(long projectId, ServiceCreateRequest serviceCreateRequest) {
        if (serviceHelper.isDuplicateName(projectId, null, serviceCreateRequest.getServiceName())) {
            throw new RoRoApiException(ErrorCode.INVENTORY_SERVICE_DUPLICATE_NAME);
        }

        if (serviceHelper.isDuplicateCustomerServiceCode(projectId, null, serviceCreateRequest.getCustomerServiceCode())) {
            throw new RoRoApiException(ErrorCode.INVENTORY_CUSTOMER_CODE_DUPLICATE, serviceCreateRequest.getCustomerServiceCode());
        }

        ServiceMaster serviceMaster = modelMapper.map(serviceCreateRequest, ServiceMaster.class);
        serviceMaster.setProjectId(projectId);
        serviceMaster.setRegistDatetime(new Date());
        serviceMaster.setRegistUserId(WebUtil.getUserId());
        serviceMaster.setModifyDatetime(new Date());
        serviceMaster.setModifyUserId(WebUtil.getUserId());
        serviceMaster.setDeleteYn(CommonConstants.NO);

        serviceMasterRepository.save(serviceMaster);

        updateServiceLabels(serviceCreateRequest, serviceMaster);
        updateServiceManagers(serviceCreateRequest, serviceMaster);

        return serviceMaster;
    }

    @Transactional
    public ServiceMaster modifyService(long projectId, long serviceId, ServiceCreateRequest serviceCreateRequest) {
        ServiceMaster serviceMaster = serviceMasterRepository.findByProjectIdAndServiceId(projectId, serviceId);

        if (serviceMaster == null) {
            throw new RoRoApiException(ErrorCode.INVENTORY_SERVICE_NOT_FOUND);
        }

        if (serviceHelper.isDuplicateName(projectId, serviceId, serviceCreateRequest.getServiceName())) {
            throw new RoRoApiException(ErrorCode.INVENTORY_SERVICE_DUPLICATE_NAME);
        }

        if (serviceHelper.isDuplicateCustomerServiceCode(projectId, serviceId, serviceCreateRequest.getCustomerServiceCode())) {
            throw new RoRoApiException(ErrorCode.INVENTORY_CUSTOMER_CODE_DUPLICATE, serviceCreateRequest.getCustomerServiceCode());
        }


        serviceLabelRepository.deleteAllByServiceId(serviceId);
        updateServiceLabels(serviceCreateRequest, serviceMaster);

        serviceManagerRepository.deleteAllByServiceId(serviceId);
        updateServiceManagers(serviceCreateRequest, serviceMaster);

        modelMapper.map(serviceCreateRequest, serviceMaster);
        serviceMaster.setModifyUserId(WebUtil.getUserId());
        serviceMaster.setModifyDatetime(new Date());
        serviceMasterRepository.save(serviceMaster);

        return serviceMaster;
    }


    @Transactional
    public void deleteService(long projectId, long serviceId) {
        ServiceMaster serviceMaster = serviceMasterRepository.findByProjectIdAndServiceId(projectId, serviceId);

        if (serviceMaster == null) {
            throw new RoRoApiException(ErrorCode.INVENTORY_SERVICE_NOT_FOUND);
        }

        ServiceResourceCount serviceResourceCount = getInventoryCountByServiceId(projectId, serviceId);

        if (serviceResourceCount.getApplicationCount() == 0
                && serviceResourceCount.getDatabaseCount() == 0
                && serviceResourceCount.getMiddlewareCount() == 0
                && serviceResourceCount.getServerCount() == 0) {
            serviceMaster.setDeleteYn(YES);
            serviceMaster.setModifyUserId(WebUtil.getUserId());
            serviceMasterRepository.save(serviceMaster);

            // List<Assessment> assessmentList = assessmentRepository.findAll(
            //         CommonSpec.getSpecification(new SearchCriteria("service", serviceId, SearchOperation.EQUAL)));
            // for (Assessment assessment : assessmentList) {
            //     assessment.setDeleteYn("Y");
            //     assessment.setUpdateUser(WebUtil.getId());
            //     assessmentRepository.save(assessment);
            // }
        } else {
            String[] exceptionParameter = new String[]{
                    String.valueOf(serviceResourceCount.getApplicationCount()),
                    String.valueOf(serviceResourceCount.getDatabaseCount()),
                    String.valueOf(serviceResourceCount.getMiddlewareCount()),
                    String.valueOf(serviceResourceCount.getServerCount())};
            throw new RoRoApiException(ErrorCode.INVENTORY_SERVICE_DELETED_FAIL, exceptionParameter);
        }

    }

    public ByteArrayInputStream getServiceListInputStream(long projectId) {
        List<ServiceMaster> serviceMasters = serviceMasterRepository.findAllByProjectIdAndDeleteYn(projectId, "N");

        List<ServiceResponse> serviceResponsesList = new ArrayList<>();
        for (ServiceMaster s : serviceMasters) {
            ServiceResponse response = modelMapper.map(s, ServiceResponse.class);
            serviceResponsesList.add(response);
        }

        getServiceManagers(serviceResponsesList);
        getResourceTotalCounts(serviceResponsesList);

        ListToExcelDto listToExcelDto = new ListToExcelDto();
        listToExcelDto.setHeaderItemList(serviceCreateExcel.generateServiceHeaderRow());
        listToExcelDto.setRowItemList(serviceCreateExcel.generateServiceBodyRow(serviceResponsesList));

        try {
            ByteArrayOutputStream out = ExcelUtil.listToExcel("Services", listToExcelDto);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            log.error("Unhandled exception occurred while create service excel list.", e);
            throw new RoRoApiException(ErrorCode.EXCEL_CREATE_FAILED, e.getMessage());
        }
    }


    private void getServiceLabels(List<ServiceResponse> serviceResponses) {
        serviceResponses.forEach(this::getServiceLabel);
    }

    private <T extends ServiceResponse> void getServiceLabel(T service) {
        List<Label.LabelResponse> labels = getLabelsByServiceId(service.getServiceId());

        service.setLabelList(labels);
    }

    private List<Label.LabelResponse> getLabelsByServiceId(long serviceId) {
        return labelMapper.getServiceLabelList(serviceId);
    }

    private void getResourceTotalCounts(List<ServiceResponse> serviceResponses) {
        serviceResponses.forEach(this::getResourceTotalCount);
    }

    private <T extends ServiceResponse> void getResourceTotalCount(T service) {
        ServiceResourceCount serviceResourceCount = getInventoryCountByServiceId(service.getProjectId(), service.getServiceId());
        service.setServerCount(serviceResourceCount.getServerCount());
        service.setMiddlewareCount(serviceResourceCount.getMiddlewareCount());
        service.setApplicationCount(serviceResourceCount.getApplicationCount());
        service.setDatabaseCount(serviceResourceCount.getDatabaseCount());
        service.setDatasourceCount(serviceResourceCount.getDatasourceCount());
    }


    private ServiceResourceCount getInventoryCountByServiceId(Long projectId, Long serviceId) {
        ServiceResourceCount serviceResourceCount = new ServiceResourceCount();
        List<ServerResponse> serverList = serverService.getServers(projectId, serviceId, false);
        List<MiddlewareResponse> middlewareList = middlewareService.getMiddlewares(projectId, serviceId, null);
        List<ApplicationResponse> applicationList = applicationService.getApplications(projectId, serviceId, null);
        List<DatabaseEngineListResponseDto> databaseList = databaseService.getDatabaseEngines(projectId, serviceId, null);
        List<ServiceDatasourceResponse> datasourceList = getDatasources(projectId, serviceId);

        serviceResourceCount.setServerCount(serverList.size());
        serviceResourceCount.setMiddlewareCount(middlewareList.size());
        serviceResourceCount.setApplicationCount(applicationList.size());
        serviceResourceCount.setDatabaseCount(databaseList.size());
        serviceResourceCount.setDatasourceCount(datasourceList.size());

        return serviceResourceCount;
    }

    private void getServiceManagers(List<ServiceResponse> serviceResponses) {
        serviceResponses.forEach(this::getServiceManager);
    }

    private <T extends ServiceResponse> void getServiceManager(T service) {
        List<ServiceResponse.ManagerResponse> managers = getManagersByServiceId(service.getServiceId());

        service.setManagers(managers);
    }

    private List<ServiceResponse.ManagerResponse> getManagersByServiceId(long serviceId) {
        List<Manager> managers = serviceMapper.selectServiceManagerList(serviceId);
        return CollectionUtils.emptyIfNull(managers)
                .stream().map(i -> modelMapper.map(i, ServiceResponse.ManagerResponse.class))
                .collect(Collectors.toList());
    }

    public ServiceResponse getServiceByServiceId(Long serviceId) {
        return serviceMapper.selectServiceByServiceId(serviceId);
    }

    public void updateServiceManagers(ServiceCreateRequest serviceCreateRequest, ServiceMaster serviceMaster) {
        if (serviceCreateRequest.getServiceManagers() != null) {
            for (Manager manager : serviceCreateRequest.getServiceManagers()) {
                userMasterRepository.findById(manager.getUserId()).orElseThrow(() -> new ResourceNotFoundException("User ID : " + manager.getUserId() + " Not Found."));

                ServiceManager serviceManager = new ServiceManager();
                serviceManager.setUserId(manager.getUserId());
                serviceManager.setServiceId(serviceMaster.getServiceId());
                serviceManager.setManagerTypeCode(manager.getManagerTypeCode());

                serviceManagerRepository.save(serviceManager);
            }
        }
    }

    public void updateServiceLabels(ServiceCreateRequest serviceCreateRequest, ServiceMaster serviceMaster) {
        if (serviceCreateRequest.getLabelIds() != null) {
            for (Long labelId : serviceCreateRequest.getLabelIds()) {
                labelMasterRepository.findById(labelId).orElseThrow(() -> new ResourceNotFoundException("Label ID : " + labelId + " Not Found."));

                ServiceLabel serviceLabel = new ServiceLabel();
                serviceLabel.setLabelId(labelId);
                serviceLabel.setServiceId(serviceMaster.getServiceId());

                serviceLabelRepository.save(serviceLabel);
            }
        }
    }

    /**
     * <pre>
     * 서비스 생성/수정 시 customerServiceCode에 대한 중복 체크 수행
     * </pre>
     *
     * @param serviceMaster
     *
     * @return
     */
    public synchronized ServiceMaster saveServiceMaster(ServiceMaster serviceMaster) {
        Long projectId = serviceMaster.getProjectId();
        Long serviceId = serviceMaster.getServiceId();
        String customerServiceCode = serviceMaster.getCustomerServiceCode();

        List<ServiceMaster> serviceMasterList = serviceMasterRepository.findByProjectIdAndCustomerServiceCode(projectId, customerServiceCode);

        if (serviceMasterList.size() == 1) {
            if (serviceId != null) {
                if (!serviceMasterList.get(0).getServiceId().equals(serviceId)) {
                    throw new RoRoApiException(ErrorCode.INVENTORY_CUSTOMER_CODE_DUPLICATE, customerServiceCode);
                }
            } else {
                throw new RoRoApiException(ErrorCode.INVENTORY_CUSTOMER_CODE_DUPLICATE, customerServiceCode);
            }
        } else if (serviceMasterList.size() > 1) {
            throw new RoRoApiException(ErrorCode.INVENTORY_CUSTOMER_CODE_DUPLICATE, customerServiceCode);
        }

        return serviceMasterRepository.save(serviceMaster);
    }

    /**
     * <pre>
     * 서비스 내 Datasource 목록 조회
     * </pre>
     *
     * @param projectId
     * @param serviceId
     *
     * @return
     */
    public List<ServiceDatasourceResponse> getDatasources(long projectId, long serviceId) {
        List<ServiceDatasourceResponse> serviceDatasourceResponses = new ArrayList<>();

        List<Map<String, Object>> datasourceList = serviceMapper.getDatasources(projectId, serviceId);

        for (Map<String, Object> resultMap : datasourceList) {
            ServiceDatasourceResponse serviceDatasourceResponse = new ServiceDatasourceResponse();
            serviceDatasourceResponse.setProjectId(projectId);
            serviceDatasourceResponse.setServiceId(serviceId);

            serviceDatasourceResponse.setUserName((String) resultMap.get("user_name"));
            serviceDatasourceResponse.setDatasourceName((String) resultMap.get("descriptors_name"));
            serviceDatasourceResponse.setConnectionUrl((String) resultMap.get("full_descriptors"));

            if (StringUtils.isEmpty((String) resultMap.get("server_ip")) && StringUtils.isEmpty((String) resultMap.get("service_name"))) {
                Map<String, Object> databaseInstanceMap = applicationMapper.selectApplicationDatabaseInstance(projectId, (String) resultMap.get("server_ip"), (String) resultMap.get("service_name"));

                if (databaseInstanceMap != null) {
                    serviceDatasourceResponse.setProjectId((Long) databaseInstanceMap.get("project_id"));
                    serviceDatasourceResponse.setDatabaseInventoryId((Long) databaseInstanceMap.get("database_inventory_id"));
                    serviceDatasourceResponse.setDatabaseInstanceId((Long) databaseInstanceMap.get("database_instance_id"));
                    serviceDatasourceResponse.setUserName((String) databaseInstanceMap.get("user_name"));
                }
            }

            serviceDatasourceResponses.add(serviceDatasourceResponse);
        }

        // 중복제거
        serviceDatasourceResponses = serviceDatasourceResponses.stream()
                .filter(distinctByKey(f -> f.getDatabaseInventoryId() + ":" + f.getDatabaseInstanceId() + ":" + f.getDatasourceName() + ":" + f.getConnectionUrl() + ":" + f.getUserName()))
                .collect(Collectors.toList());

        return serviceDatasourceResponses;
    }
}
//end of ServiceService.java