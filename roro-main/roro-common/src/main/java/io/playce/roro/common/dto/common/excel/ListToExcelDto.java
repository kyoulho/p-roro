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
 * SangCheon Park   Dec 07, 2021		    First Draft.
 */
package io.playce.roro.common.dto.common.excel;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 * List 형태의 Data를 Excel로 export 하기 위한 DTO
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
@Getter
@Setter
public class ListToExcelDto {

    private List<String> headerItemList = new ArrayList<>();
    private List<RowItem> rowItemList = new ArrayList<>();

    @Getter
    @Setter
    public static class RowItem {
        private List<Object> cellItemList = new ArrayList<>();
    }
}
//end of ListToExcelDto.java