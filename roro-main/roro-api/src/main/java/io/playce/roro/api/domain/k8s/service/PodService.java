/*
 * Copyright 2023 The playce-roro Project.
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
 * Dong-Heon Han    Jul 28, 2023		First Draft.
 */

package io.playce.roro.api.domain.k8s.service;

import io.playce.roro.common.dto.k8s.PodResponse;
import io.playce.roro.mybatis.domain.k8s.PodMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PodService {
    private final PodMapper podMapper;

    public List<PodResponse> getPods(Long projectId, Long k8sClusterId, Long clusterScanId) {
        return podMapper.selectPods(projectId, k8sClusterId, clusterScanId);
    }
}