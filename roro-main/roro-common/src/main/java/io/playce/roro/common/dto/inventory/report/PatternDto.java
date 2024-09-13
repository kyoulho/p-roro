/*
 * Copyright 2022 The Playce-RoRo Project.
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
 * Jaeeon Bae       1월 21, 2022            First Draft.
 */
package io.playce.roro.common.dto.inventory.report;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Jaeeon Bae
 * @version 3.0
 */
@Getter
@Setter
@ToString
public class PatternDto {

    /**
     * The Category.
     */
    @NotNull
    @Schema(title = "inventoryTypeCode", example = "SERV / SVR / MW / APP / DBMS 중 택일", description = "인벤토리 타입 코드", required = true)
    private String inventoryTypeCode;

    /**
     * The Name Patterns.
     */
    @Schema(title = "File Name Patterns", type = "array", example = "[\"TYPE\", \"ID\", \"NAME\", \"SCANNED_DATE\"]", description = "사용자가 지정한 파일 명 패턴 목록 (배열으로 나열)", required = true)
    private List<Pattern> patterns;
}