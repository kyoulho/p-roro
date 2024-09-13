/*
 * Copyright 2021 The playce-roro-v3} Project.
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
 * Dong-Heon Han    Nov 12, 2021		    First Draft.
 */

package io.playce.roro.common.dto.common.label;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Dong-Heon Han
 * @version 3.0
 */
public class Label {
    @Getter
    @Setter
    public static class LabelRequest {
        private String labelName;
    }

    @Getter
    @Setter
    public static class LabelResponse extends LabelRequest {
        private Long labelId;
    }

    @Getter
    @Setter
    public static class LabelDetailResponse extends LabelResponse {
        private Date registDatetime;
        private String registUserLoginId;
        private Date modifyDatetime;
        private String modifyUserLoginId;
    }


}
//end of Label.java