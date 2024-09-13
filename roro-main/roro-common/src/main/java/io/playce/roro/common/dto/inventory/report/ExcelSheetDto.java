package io.playce.roro.common.dto.inventory.report;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExcelSheetDto {
    private String sheetName;
    private boolean excluded;
}
