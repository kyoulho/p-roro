/*
 * Copyright 2023 The playce-roro-v3 Project.
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
 * Jihyun Park      6월 15, 2023            First Draft.
 */
package io.playce.roro.api.domain.topology.service;

import io.playce.roro.api.common.error.exception.ResourceNotFoundException;
import io.playce.roro.common.dto.topology.ExternalConnectionLabelRequest;
import io.playce.roro.jpa.entity.ExternalConnection;
import io.playce.roro.jpa.entity.ExternalConnectionLabel;
import io.playce.roro.jpa.repository.ExternalConnectionLabelRepository;
import io.playce.roro.jpa.repository.ExternalConnectionRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <pre>
 *
 *
 * </pre>
 *
 * @author Jihyun Park
 * @version 1.0
 */
@RequiredArgsConstructor
@Transactional
@Service
public class ExternalConnectionLabelService {

    private final ExternalConnectionRepository externalConnectionRepository;
    private final ExternalConnectionLabelRepository externalConnectionLabelRepository;
    private final ModelMapper modelMapper;

    public void saveExternalConnectionLabel(ExternalConnectionLabelRequest externalConnectionLabelRequest) {
        List<ExternalConnection> list = externalConnectionRepository.findAllByIp(externalConnectionLabelRequest.getIp());
        if(list.size() < 1) {
            throw new ResourceNotFoundException("External Connection Ip : " + externalConnectionLabelRequest.getIp() + " Not Found.");
        }
        ExternalConnectionLabel externalConnectionLabel = modelMapper.map(externalConnectionLabelRequest, ExternalConnectionLabel.class);
        externalConnectionLabelRepository.save(externalConnectionLabel);
    }

    public void deleteExternalConnectionLabel(Long projectId, String ip) {
        externalConnectionLabelRepository.findByProjectIdAndIp(projectId, ip)
                .orElseThrow(() -> new ResourceNotFoundException("External Connection Label IP : " + ip + " Not Found."));

        externalConnectionLabelRepository.deleteExternalConnectionLabelByProjectIdAndIp(projectId, ip);
    }
}
