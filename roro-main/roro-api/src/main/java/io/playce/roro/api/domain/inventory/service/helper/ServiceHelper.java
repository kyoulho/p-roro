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
 * Author            Date                Description
 * ---------------  ----------------    ------------
 * Hoon Oh       12월 01, 2021            First Draft.
 */
package io.playce.roro.api.domain.inventory.service.helper;

import io.playce.roro.jpa.entity.ServiceMaster;
import io.playce.roro.jpa.repository.ServiceMasterRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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
@Component
public class ServiceHelper {

    private final ServiceMasterRepository serviceMasterRepository;

    public ServiceHelper(ServiceMasterRepository serviceMasterRepository) {
        this.serviceMasterRepository = serviceMasterRepository;
    }

    public boolean isDuplicateName(Long projectId, Long serviceId, String name) {
        boolean duplicated = false;
        log.debug("project ID : {}, service ID : {}, serviceName : {}", projectId, serviceId, name);

        ServiceMaster service = null;
        if (serviceId == null) {
            service = serviceMasterRepository.findByProjectIdAndServiceNameAndDeleteYn(projectId, name, "N");
        } else {
            // 기존의 서비스 조회
            ServiceMaster originService = serviceMasterRepository.findByProjectIdAndServiceIdAndDeleteYn(projectId, serviceId, "N");

            if (originService.getServiceName().equals(name)) {
                service = serviceMasterRepository.findByProjectIdAndServiceIdAndServiceNameNotAndDeleteYn(projectId, serviceId, name, "N");
            } else {
                service = serviceMasterRepository.findByProjectIdAndServiceIdAndServiceNameAndDeleteYn(projectId, serviceId, name, "N");
            }
        }

        if (service != null) {
            duplicated = true;
        }

        return duplicated;
    }

    public boolean isDuplicateCustomerServiceCode(Long projectId, Long serviceId, String customerServiceCode) {
        boolean isDuplicated;

        ServiceMaster service;

        if (serviceId == null) {
            service = serviceMasterRepository.findByProjectIdAndCustomerServiceCodeAndDeleteYn(projectId, customerServiceCode, "N");
        } else {
            service = serviceMasterRepository.findByProjectIdAndServiceIdNotAndCustomerServiceCodeAndDeleteYn(projectId, serviceId, customerServiceCode, "N");
        }

        if (service == null) {
            isDuplicated = false;
        } else {
            isDuplicated = true;
        }

        return isDuplicated;
    }
}
//end of ServiceHelper.java