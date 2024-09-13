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
 * SangCheon Park   Nov 30, 2021		    First Draft.
 */
package io.playce.roro.mybatis.domain.common.label;

import io.playce.roro.common.dto.common.label.Label;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
@Repository
public interface LabelMapper {

    /**
     * <pre>
     * 서비스에 설정된 Label 이름 목록을 조회한다.
     * </pre>
     *
     * @param serviceId
     *
     * @return
     */
    List<String> getServiceLabelNames(@Param("serviceId") Long serviceId);

    List<Label.LabelResponse> getServiceLabelList(@Param("serviceId") Long serviceId);

    /**
     * <pre>
     * 인벤토리에 설정된 Label 목록을 조회한다.
     * </pre>
     *
     * @param inventoryId
     *
     * @return
     */
    List<Label.LabelResponse> getInventoryLabelList(@Param("inventoryId") Long inventoryId);

    List<Label.LabelDetailResponse> selectLabel(@Param("inventoryId") Long inventoryId);

    List<Label.LabelDetailResponse> selectLabelsByKeyword(@Param("keyword") String keyword);

}
//end of LabelMapper.java
