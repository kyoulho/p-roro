package io.playce.roro.api.domain.prerequisite.service;

import com.google.gson.Gson;
import io.playce.roro.api.common.CommonConstants;
import io.playce.roro.api.common.error.ErrorCode;
import io.playce.roro.api.common.error.exception.RoRoApiException;
import io.playce.roro.api.domain.inventory.service.InventoryProcessService;
import io.playce.roro.common.code.Domain1002;
import io.playce.roro.common.dto.common.excel.ListToExcelDto;
import io.playce.roro.common.dto.inventory.process.InventoryProcessHistory;
import io.playce.roro.common.dto.inventory.process.LatestInventoryProcess;
import io.playce.roro.common.dto.prerequisite.CheckStatus;
import io.playce.roro.common.dto.prerequisite.PrerequisiteDto;
import io.playce.roro.common.dto.prerequisite.ServerResult;
import io.playce.roro.common.util.ExcelUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PrerequisiteService {
    private final InventoryProcessService inventoryProcessService;
    private final ModelMapper modelMapper;
    private final Gson gson;

    public List<PrerequisiteDto.PrerequisiteResponse> getPrerequisites(Long projectId) {
        List<LatestInventoryProcess> latestInventoryProcesses = inventoryProcessService.getLatestInventoryProcessesByType(projectId, Domain1002.PREQ);
        return latestInventoryProcesses.stream().map(i -> {
            PrerequisiteDto.PrerequisiteResponse dto = modelMapper.map(i, PrerequisiteDto.PrerequisiteResponse.class);
            dto.setResult(gson.fromJson(i.getResultJson(), ServerResult.PrerequisiteJson.class));
            return dto;
        }).collect(Collectors.toList());
    }


    public List<PrerequisiteDto.PrerequisiteHistoryResponse> getPrerequistieHistory(Long projectId, String from, String to) {
        List<InventoryProcessHistory> inventoryProcessHistories = inventoryProcessService.getInventoryProcessByTypeAndDate(projectId, Domain1002.PREQ, from, to);
        return inventoryProcessHistories.stream().map(i -> {
            PrerequisiteDto.PrerequisiteHistoryResponse dto = modelMapper.map(i, PrerequisiteDto.PrerequisiteHistoryResponse.class);
            dto.setResult(gson.fromJson(i.getResultJson(), ServerResult.PrerequisiteJson.class));
            return dto;
        }).collect(Collectors.toList());
    }

    public ByteArrayInputStream getPrerequisitesExcel(Long projectId) {
        SimpleDateFormat sdf = new SimpleDateFormat(CommonConstants.DEFAULT_DATE_FORMAT);

        List<PrerequisiteDto.PrerequisiteResponse> prerequisiteResponses = getPrerequisites(projectId);

        // 헤더 설정
        ListToExcelDto listToExcelDto = new ListToExcelDto();
        listToExcelDto.getHeaderItemList().add("Server Inventory ID");
        listToExcelDto.getHeaderItemList().add("Server Inventory Name");
        listToExcelDto.getHeaderItemList().add("IP Address");
        listToExcelDto.getHeaderItemList().add("SSH Port");
        listToExcelDto.getHeaderItemList().add("Username");
        listToExcelDto.getHeaderItemList().add("Prerequisite Check Result");
        listToExcelDto.getHeaderItemList().add("Status Message");
        listToExcelDto.getHeaderItemList().add("Scan Enabled");
        listToExcelDto.getHeaderItemList().add("Checked Date");

        ListToExcelDto.RowItem rowItem;
        String checkStatus;
        for (PrerequisiteDto.PrerequisiteResponse prerequisiteResponse : prerequisiteResponses) {
            rowItem = new ListToExcelDto.RowItem();
            rowItem.getCellItemList().add(prerequisiteResponse.getServerInventoryId());
            rowItem.getCellItemList().add(prerequisiteResponse.getServerInventoryName());
            rowItem.getCellItemList().add(prerequisiteResponse.getRepresentativeIpAddress());
            rowItem.getCellItemList().add(prerequisiteResponse.getConnectionPort());

            ServerResult.PrerequisiteJson result = prerequisiteResponse.getResult();
            if (result != null) {
                rowItem.getCellItemList().add(result.getUserName());

                checkStatus = CollectionUtils.isNotEmpty(result.getCheckStatus()) ? String.join("\n", generatePrerequisiteStatus(result.getCheckStatus())) : "";
                checkStatus = checkStatus.replaceAll("(?!\nstep\\s\\d)(\n)", ", ");

                rowItem.getCellItemList().add(checkStatus);
                rowItem.getCellItemList().add(String.join("\n", result.getStatusMessage()));
                rowItem.getCellItemList().add(result.getAssessmentEnabled());
                rowItem.getCellItemList().add(result.getCheckedDate() == null ? "" : sdf.format(new Date(result.getCheckedDate())));
            } else {
                rowItem.getCellItemList().add("");
                rowItem.getCellItemList().add("");
                rowItem.getCellItemList().add("");
                rowItem.getCellItemList().add("");
                rowItem.getCellItemList().add("");
            }

            listToExcelDto.getRowItemList().add(rowItem);
        }

        ByteArrayOutputStream out;
        try {
            out = ExcelUtil.listToExcel("Prerequisites", listToExcelDto);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            log.error("Unhandled exception occurred while create prerequisite excel list.", e);
            throw new RoRoApiException(ErrorCode.EXCEL_CREATE_FAILED, e.getMessage());
        }
    }

    /**
     * Excel Check Status의 icon 정보를 Step + {Number}로 변환한다.
     */
    private List<String> generatePrerequisiteStatus(List<CheckStatus> checkStatus) {
        List<String> resultList = new ArrayList<>();
        for (int index = 0; index < checkStatus.size(); index++) {
            String text = checkStatus.get(index).toExcel();
            if (text.contains("icon:")) {
                resultList.add(text.replace("icon:", "step " + (index + 1) + " - "));
            }
        }

        return resultList;
    }
}
