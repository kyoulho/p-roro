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
 * Hoon Oh       12월 07, 2021            First Draft.
 */
package io.playce.roro.api.domain.common.service;

import io.playce.roro.api.common.util.WebUtil;
import io.playce.roro.common.dto.common.label.Label;
import io.playce.roro.jpa.entity.LabelMaster;
import io.playce.roro.jpa.repository.LabelMasterRepository;
import io.playce.roro.mybatis.domain.common.label.LabelMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * <pre>
 *
 *
 * </pre>
 *
 * @author Hoon Oh
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
public class LabelService {
    private final LabelMapper labelMapper;
    private final LabelMasterRepository labelMasterRepository;

    public List<Label.LabelDetailResponse> getLabelList(String keyword) {
        return labelMapper.selectLabelsByKeyword(keyword);
    }

    @Transactional
    public Label.LabelResponse createLabel(Label.LabelRequest label) {
        LabelMaster labelMaster = labelMasterRepository.findByLabelName(label.getLabelName());

        if (labelMaster == null) {
            labelMaster = new LabelMaster();
            labelMaster.setLabelName(label.getLabelName());
            labelMaster.setRegistUserId(WebUtil.getUserId());
            labelMaster.setRegistDatetime(new Date());
            labelMaster.setModifyUserId(WebUtil.getUserId());
            labelMaster.setModifyDatetime(new Date());

            labelMasterRepository.save(labelMaster);
        }

        Label.LabelResponse response = new Label.LabelResponse();
        response.setLabelId(labelMaster.getLabelId());
        response.setLabelName(labelMaster.getLabelName());

        return response;
    }
}
//end of LabelService.java