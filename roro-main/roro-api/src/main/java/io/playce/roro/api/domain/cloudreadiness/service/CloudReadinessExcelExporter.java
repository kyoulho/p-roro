/*
 * Copyright 2022 The playce-roro-v3 Project.
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
 * Author,,,Date,,,,Description
 * ---------------,----------------,------------
 * SangCheon Park   Jun 15, 2022,,    First Draft.
 */
package io.playce.roro.api.domain.cloudreadiness.service;

import io.playce.roro.api.common.error.ErrorCode;
import io.playce.roro.api.common.error.exception.RoRoApiException;
import io.playce.roro.common.dto.cloudreadiness.CloudReadinessCategoryResult;
import io.playce.roro.common.dto.inventory.service.ServiceDetail;
import io.playce.roro.common.util.support.ExcelHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.*;
import org.apache.poi.xddf.usermodel.chart.*;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.*;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTBoolean;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTDLbl;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTExtension;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTScatterSer;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
@Component
@Slf4j
public class CloudReadinessExcelExporter {

    private static final String CTDLBL = "CTDLbl";
    private static final String CTEXTENSION = "CTExtension";
    private static final String CTDLBL_CTEXTENSION = "CTDLbl_CTExtension";

    public ByteArrayOutputStream createExcelReport(List<CloudReadinessCategoryResult> surveyResults) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            // -1 means turn off auto-flushing and accumulate all rows in memory
            SXSSFWorkbook workbook = new SXSSFWorkbook(-1);
            Map<String, CellStyle> styleMap = ExcelHelper.initCellStyle(workbook);

            SXSSFSheet sheet1 = workbook.createSheet("Cloud Readiness Survey");
            sheet1.trackAllColumnsForAutoSizing();

            XSSFSheet sheet = workbook.getXSSFWorkbook().getSheetAt(0);

            createHeader(workbook.getXSSFWorkbook(), sheet, styleMap);
            createDataRow(workbook.getXSSFWorkbook(), sheet, styleMap, surveyResults);
            createScatterChart(workbook.getXSSFWorkbook().getSheetAt(0), surveyResults.size());
            //createScatterChart(wb.getSheetAt(0), surveyResults.size());

            // Set Filter
            sheet.setAutoFilter(new CellRangeAddress(1, surveyResults.size() + 1, 0, 1));

            // Set Freeze Pane
            sheet.createFreezePane(2, 2);

            // Adjust Column Size
            autoSizeColumn(workbook.getXSSFWorkbook());

            workbook.getXSSFWorkbook().write(out);

            // workbook.dispose();

            return out;
        } catch (Exception e) {
            log.error("Unhandled exception occurred while create an excel survey report.", e);
            throw new RoRoApiException(ErrorCode.EXCEL_CREATE_FAILED, e.getMessage());
        }
    }

    public void writeServiceList(XSSFWorkbook workbook, List<ServiceDetail> serviceList) {
        Map<String, CellStyle> styleMap = ExcelHelper.initCellStyle(workbook);

        // Step 01_Business Factors
        XSSFSheet businessFactorSheet = workbook.getSheetAt(1);

        // Step 02_Technical Factors
        XSSFSheet technicalFactorSheet = workbook.getSheetAt(2);

        XSSFRow businessFactorRow, technicalFactorRow;
        XSSFCell businessFactorCell = null, technicalFactorCell;

        ServiceDetail service;
        int rowNum;
        for (int i = 0; i < serviceList.size(); i++) {
            rowNum = i + 1;
            service = serviceList.get(i);

            businessFactorRow = businessFactorSheet.getRow(rowNum);
            if (businessFactorRow == null) {
                businessFactorRow = businessFactorSheet.createRow(rowNum);
            }

            technicalFactorRow = technicalFactorSheet.getRow(rowNum);
            if (technicalFactorRow == null) {
                technicalFactorRow = technicalFactorSheet.createRow(rowNum);
            }

            for (int j = 6; j <= 17; j++) {
                if (j < 13) {
                    businessFactorCell = createCellWithBorder(workbook, businessFactorRow, j, HorizontalAlignment.RIGHT, BorderStyle.THIN, false, styleMap);
                }
                technicalFactorCell = createCellWithBorder(workbook, technicalFactorRow, j, HorizontalAlignment.RIGHT, BorderStyle.THIN, false, styleMap);

                if (j == 6) {
                    businessFactorCell.setCellValue(service.getServiceId());
                    technicalFactorCell.setCellValue(service.getServiceId());
                }
                if (j == 7) {
                    businessFactorCell.setCellValue(service.getServiceName());
                    technicalFactorCell.setCellValue(service.getServiceName());
                }
            }
        }
    }

    private void createHeader(XSSFWorkbook workbook, XSSFSheet sheet, Map<String, CellStyle> styleMap) {
        XSSFRow row = sheet.createRow(sheet.getLastRowNum() + 1);
        XSSFCell cell;

        cell = createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("서비스 아이디\n(Service ID)");
        cell.getCellStyle().setWrapText(true);

        cell = createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("서비스 명\n(Service Name)");
        cell.getCellStyle().setWrapText(true);

        for (int i = 0; i < 5; i++) {
            cell = createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, true, null, IndexedColors.LIGHT_TURQUOISE);
            cell.setCellValue("업무적 관점 (Business Factors)");
        }

        mergeRegion(sheet, 0, 0, 2, 6);

        for (int i = 0; i < 9; i++) {
            cell = createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, true, null, IndexedColors.LIGHT_CORNFLOWER_BLUE);
            cell.setCellValue("기술적 관점 (Technical Factors)");
        }

        mergeRegion(sheet, 0, 0, 7, 15);

        cell = createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, true, null, IndexedColors.LIGHT_GREEN);
        cell.setCellValue("비즈니스 효과\n(Business Effect)");
        cell.getCellStyle().setWrapText(true);

        cell = createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, true, null, IndexedColors.LIGHT_GREEN);
        cell.setCellValue("기술 적합성\n(Technical Fitness)");
        cell.getCellStyle().setWrapText(true);

        row.setHeight((short) 600);

        row = sheet.createRow(sheet.getLastRowNum() + 1);

        cell = createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("서비스 아이디\n(Service ID)");

        cell = createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("서비스 명\n(Service Name)");

        mergeRegion(sheet, 0, 1, 0, 0);
        mergeRegion(sheet, 0, 1, 1, 1);

        cell = createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, true, null, IndexedColors.LIGHT_TURQUOISE);
        cell.setCellValue("업무 중요도\n(Business Relevance)");
        cell.getCellStyle().setWrapText(true);

        cell = createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, true, null, IndexedColors.LIGHT_TURQUOISE);
        cell.setCellValue("시스템 규모\n(Scale of Service)");
        cell.getCellStyle().setWrapText(true);

        cell = createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, true, null, IndexedColors.LIGHT_TURQUOISE);
        cell.setCellValue("서비스 대상\n(Target of Service)");
        cell.getCellStyle().setWrapText(true);

        cell = createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, true, null, IndexedColors.LIGHT_TURQUOISE);
        cell.setCellValue("부하의 탄력성\n(Elasticity of Load)");
        cell.getCellStyle().setWrapText(true);

        cell = createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, true, null, IndexedColors.LIGHT_TURQUOISE);
        cell.setCellValue("비즈니스 요구\n(Business Requirements)");
        cell.getCellStyle().setWrapText(true);

        cell = createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, true, null, IndexedColors.LIGHT_CORNFLOWER_BLUE);
        cell.setCellValue("자원 사용률\n(Usage of Resources)");
        cell.getCellStyle().setWrapText(true);

        cell = createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, true, null, IndexedColors.LIGHT_CORNFLOWER_BLUE);
        cell.setCellValue("자원 노후화\n(Ageing of Resources)");
        cell.getCellStyle().setWrapText(true);

        cell = createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, true, null, IndexedColors.LIGHT_CORNFLOWER_BLUE);
        cell.setCellValue("연계 시스템 수\n(Number of Systems Interfaced)");
        cell.getCellStyle().setWrapText(true);

        cell = createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, true, null, IndexedColors.LIGHT_CORNFLOWER_BLUE);
        cell.setCellValue("사용 언어\n(Language)");
        cell.getCellStyle().setWrapText(true);

        cell = createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, true, null, IndexedColors.LIGHT_CORNFLOWER_BLUE);
        cell.setCellValue("프레임워크\n(Framework)");
        cell.getCellStyle().setWrapText(true);

        cell = createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, true, null, IndexedColors.LIGHT_CORNFLOWER_BLUE);
        cell.setCellValue("시스템 구조\n(System Architecture)");
        cell.getCellStyle().setWrapText(true);

        cell = createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, true, null, IndexedColors.LIGHT_CORNFLOWER_BLUE);
        cell.setCellValue("사용 OS\n(OS)");
        cell.getCellStyle().setWrapText(true);

        cell = createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, true, null, IndexedColors.LIGHT_CORNFLOWER_BLUE);
        cell.setCellValue("가상화 적용 여부\n(Virtualization)");
        cell.getCellStyle().setWrapText(true);

        cell = createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, true, null, IndexedColors.LIGHT_CORNFLOWER_BLUE);
        cell.setCellValue("클라우드 전환 비즈니스 요구\n(Business Requirements for\nCloud Adoption)");
        cell.getCellStyle().setWrapText(true);

        cell = createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, true, null, IndexedColors.LIGHT_GREEN);
        cell.setCellValue("비즈니스 효과\n(Business Effect)");

        cell = createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, true, null, IndexedColors.LIGHT_GREEN);
        cell.setCellValue("기술 적합성\n(Technical Fitness)");

        mergeRegion(sheet, 0, 1, 16, 16);
        mergeRegion(sheet, 0, 1, 17, 17);
    }

    private void createDataRow(XSSFWorkbook workbook, XSSFSheet sheet, Map<String, CellStyle> styleMap, List<CloudReadinessCategoryResult> surveyResults) {
        XSSFRow row;
        XSSFCell cell;

        for (CloudReadinessCategoryResult result : surveyResults) {
            row = sheet.createRow(sheet.getLastRowNum() + 1);

            cell = createCellWithBorder(workbook, row, null, HorizontalAlignment.RIGHT, BorderStyle.THIN, false, styleMap);
            cell.setCellValue(result.getServiceId());
            cell = createCellWithBorder(workbook, row, null, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            cell.setCellValue(result.getServiceName());
            cell = createCellWithBorder(workbook, row, null, HorizontalAlignment.RIGHT, BorderStyle.THIN, false, styleMap);
            setCellDate(cell, result.getBusinessRelevance());
            cell = createCellWithBorder(workbook, row, null, HorizontalAlignment.RIGHT, BorderStyle.THIN, false, styleMap);
            setCellDate(cell, result.getScaleOfService());
            cell = createCellWithBorder(workbook, row, null, HorizontalAlignment.RIGHT, BorderStyle.THIN, false, styleMap);
            setCellDate(cell, result.getTargetOfService());
            cell = createCellWithBorder(workbook, row, null, HorizontalAlignment.RIGHT, BorderStyle.THIN, false, styleMap);
            setCellDate(cell, result.getElasticityOfLoad());
            cell = createCellWithBorder(workbook, row, null, HorizontalAlignment.RIGHT, BorderStyle.THIN, false, styleMap);
            setCellDate(cell, result.getBusinessRequirements());
            cell = createCellWithBorder(workbook, row, null, HorizontalAlignment.RIGHT, BorderStyle.THIN, false, styleMap);
            setCellDate(cell, result.getUsageOfResources());
            cell = createCellWithBorder(workbook, row, null, HorizontalAlignment.RIGHT, BorderStyle.THIN, false, styleMap);
            setCellDate(cell, result.getAgeingOfResources());
            cell = createCellWithBorder(workbook, row, null, HorizontalAlignment.RIGHT, BorderStyle.THIN, false, styleMap);
            setCellDate(cell, result.getNumberOfSystemsInterfaced());
            cell = createCellWithBorder(workbook, row, null, HorizontalAlignment.RIGHT, BorderStyle.THIN, false, styleMap);
            setCellDate(cell, result.getLanguage());
            cell = createCellWithBorder(workbook, row, null, HorizontalAlignment.RIGHT, BorderStyle.THIN, false, styleMap);
            setCellDate(cell, result.getFramework());
            cell = createCellWithBorder(workbook, row, null, HorizontalAlignment.RIGHT, BorderStyle.THIN, false, styleMap);
            setCellDate(cell, result.getSystemArchitecture());
            cell = createCellWithBorder(workbook, row, null, HorizontalAlignment.RIGHT, BorderStyle.THIN, false, styleMap);
            setCellDate(cell, result.getOs());
            cell = createCellWithBorder(workbook, row, null, HorizontalAlignment.RIGHT, BorderStyle.THIN, false, styleMap);
            setCellDate(cell, result.getVirtualization());
            cell = createCellWithBorder(workbook, row, null, HorizontalAlignment.RIGHT, BorderStyle.THIN, false, styleMap);
            setCellDate(cell, result.getBusinessRequirementsForCloudAdoption());
            cell = createCellWithBorder(workbook, row, null, HorizontalAlignment.RIGHT, BorderStyle.THIN, false, null, IndexedColors.LIGHT_GREEN);
            setCellDate(cell, result.getBusinessScore());
            cell = createCellWithBorder(workbook, row, null, HorizontalAlignment.RIGHT, BorderStyle.THIN, false, null, IndexedColors.LIGHT_GREEN);
            setCellDate(cell, result.getTechnicalScore());
        }
    }

    private void createScatterChart(XSSFSheet sheet, int size) throws IOException {
        XSSFDrawing drawing = sheet.createDrawingPatriarch();
        XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 19, 3, 34, 49);

        XSSFChart chart = drawing.createChart(anchor);

        XDDFValueAxis technicalAxis = chart.createValueAxis(AxisPosition.BOTTOM);
        technicalAxis.setTitle("Technical Fitness");
        technicalAxis.setTickLabelPosition(AxisTickLabelPosition.LOW);
        technicalAxis.crossAxis(technicalAxis);
        technicalAxis.setMinimum(0.0);
        technicalAxis.setMaximum(5.0);
        technicalAxis.setCrosses(AxisCrosses.AUTO_ZERO);
        technicalAxis.setMinorUnit(0.5D);
        technicalAxis.setMajorUnit(2.5D);
        technicalAxis.setMajorTickMark(AxisTickMark.CROSS);
        technicalAxis.setMinorTickMark(AxisTickMark.CROSS);
        technicalAxis.getOrAddMajorGridProperties().setLineProperties(new XDDFLineProperties(new XDDFSolidFillProperties(XDDFColor.from(PresetColor.BLACK))));

        XDDFValueAxis businessAxis = chart.createValueAxis(AxisPosition.LEFT);
        businessAxis.setTitle("Business Effect");
        businessAxis.setTickLabelPosition(AxisTickLabelPosition.LOW);
        businessAxis.crossAxis(businessAxis);
        businessAxis.setMinimum(0.0);
        businessAxis.setMaximum(5.0);
        businessAxis.setCrosses(AxisCrosses.AUTO_ZERO);
        businessAxis.setMinorUnit(0.5D);
        businessAxis.setMajorUnit(2.5D);
        businessAxis.setMajorTickMark(AxisTickMark.CROSS);
        businessAxis.setMinorTickMark(AxisTickMark.CROSS);
        businessAxis.getOrAddMajorGridProperties().setLineProperties(new XDDFLineProperties(new XDDFSolidFillProperties(XDDFColor.from(PresetColor.BLACK))));

        XDDFNumericalDataSource<Double> technicalFactor = XDDFDataSourcesFactory.fromNumericCellRange(sheet, new CellRangeAddress(2, size + 1, 17, 17));
        XDDFNumericalDataSource<Double> businessFactor = XDDFDataSourcesFactory.fromNumericCellRange(sheet, new CellRangeAddress(2, size + 1, 16, 16));

        XDDFScatterChartData data = (XDDFScatterChartData) chart.createData(ChartTypes.SCATTER, technicalAxis, businessAxis);
        XDDFScatterChartData.Series series = (XDDFScatterChartData.Series) data.addSeries(technicalFactor, businessFactor);

        series.setMarkerStyle(MarkerStyle.CIRCLE);
        series.setMarkerSize((short) 5);
        setLineNoFill(series);

        chart.plot(data);

        Map<String, Object> referenceMap = readChartTemplate(size);

        List<CTScatterSer> list = chart.getCTChart().getPlotArea().getScatterChartArray(0).getSerList();
        CTScatterSer scatterSer = list.get(0);

        scatterSer.addNewDLbls().setDLblArray((CTDLbl[]) referenceMap.get(CTDLBL));

        CTBoolean ctBoolean = CTBoolean.Factory.newInstance();
        ctBoolean.setVal(false);
        scatterSer.getDLbls().setShowLegendKey(ctBoolean);

        ctBoolean = CTBoolean.Factory.newInstance();
        ctBoolean.setVal(false);
        scatterSer.getDLbls().setShowVal(ctBoolean);

        ctBoolean = CTBoolean.Factory.newInstance();
        ctBoolean.setVal(false);
        scatterSer.getDLbls().setShowCatName(ctBoolean);

        ctBoolean = CTBoolean.Factory.newInstance();
        ctBoolean.setVal(false);
        scatterSer.getDLbls().setShowSerName(ctBoolean);

        ctBoolean = CTBoolean.Factory.newInstance();
        ctBoolean.setVal(true);
        scatterSer.getDLbls().setShowPercent(ctBoolean);

        ctBoolean = CTBoolean.Factory.newInstance();
        ctBoolean.setVal(true);
        scatterSer.getDLbls().setShowBubbleSize(ctBoolean);

        ctBoolean = CTBoolean.Factory.newInstance();
        ctBoolean.setVal(false);
        scatterSer.getDLbls().setShowLeaderLines(ctBoolean);

        scatterSer.getDLbls().addNewExtLst().setExtArray((CTExtension[]) referenceMap.get(CTDLBL_CTEXTENSION));

        scatterSer.addNewExtLst().setExtArray((CTExtension[]) referenceMap.get(CTEXTENSION));
    }

    /**
     * Merge region.
     *
     * @param sheet     the sheet
     * @param firstRow  the first row
     * @param lastRow   the last row
     * @param firstCell the first cell
     * @param lastCell  the last cell
     */
    public void mergeRegion(XSSFSheet sheet, int firstRow, int lastRow, int firstCell, int lastCell) {
        try {
            if (firstRow != lastRow || firstCell != lastCell) {
                sheet.addMergedRegion(new CellRangeAddress(firstRow, lastRow, firstCell, lastCell));
            }
        } catch (Exception e) {
            log.warn("Sheet : [{}], firstRow : [{}], lastRow : [{}], firstCell : [{}], lastCell : [{}]",
                    sheet.getSheetName(), firstRow, lastRow, firstCell, lastCell);
            log.error("Unable to merge cells.", e);
        }
    }

    private void setCellDate(Cell cell, Float value) {
        DecimalFormat df = new DecimalFormat("#.##");
        String formatted = df.format(value);
        cell.setCellValue(formatted);
        cell.setCellValue(Double.parseDouble(formatted));
    }

    private void setLineNoFill(XDDFScatterChartData.Series series) {
        XDDFNoFillProperties noFillProperties = new XDDFNoFillProperties();
        XDDFLineProperties lineProperties = new XDDFLineProperties();
        lineProperties.setFillProperties(noFillProperties);
        XDDFShapeProperties shapeProperties = series.getShapeProperties();

        if (shapeProperties == null) {
            shapeProperties = new XDDFShapeProperties();
        }
        shapeProperties.setLineProperties(lineProperties);
        series.setShapeProperties(shapeProperties);
    }

    private Map<String, Object> readChartTemplate(int size) throws IOException {
        Map<String, Object> referenceMap = new HashMap<>();

        XSSFWorkbook workbook = new XSSFWorkbook(Objects.requireNonNull(
                CloudReadinessExcelExporter.class.getResourceAsStream("/template/Cloud_Readiness_Survey_Results.xlsx")));
        XSSFSheet sheet = workbook.getSheetAt(0);

        XSSFDrawing drawing = sheet.getDrawingPatriarch();
        XSSFChart chart = drawing.getCharts().get(0);

        List<CTScatterSer> list = chart.getCTChart().getPlotArea().getScatterChartArray(0).getSerList();
        CTScatterSer ser = list.get(0);

        CTExtension[] extensions;
        CTDLbl[] ctdLbls;

        // Data Label 목록
        ctdLbls = ser.getDLbls().getDLblArray();
        referenceMap.put(CTDLBL, ArrayUtils.subarray(ctdLbls, 0, size));

        // Data Lable의 부가정보
        extensions = ser.getDLbls().getExtLst().getExtArray();
        referenceMap.put(CTDLBL_CTEXTENSION, extensions);

        // Series의 부가 정보
        extensions = ser.getExtLst().getExtArray();
        referenceMap.put(CTEXTENSION, ArrayUtils.subarray(extensions, 0, size));

        return referenceMap;
    }

    public XSSFCell createCellWithBorder(XSSFWorkbook workbook, XSSFRow row, Integer columnIdx, HorizontalAlignment align, BorderStyle border, boolean isHeader, Map<String, CellStyle> styleMap) {
        return createCellWithBorder(workbook, row, columnIdx, align, border, isHeader, styleMap, null);
    }

    public XSSFCell createCellWithBorder(XSSFWorkbook workbook, XSSFRow row, Integer columnIdx, HorizontalAlignment align, BorderStyle border, boolean isHeader, Map<String, CellStyle> styleMap, IndexedColors colors) {
        XSSFCell cell = null;
        if (columnIdx != null) {
            cell = row.createCell(columnIdx);
        }

        if (cell == null) {
            cell = row.createCell(row.getLastCellNum() == -1 ? 0 : row.getLastCellNum());
        }

        CellStyle style = null;
        if (styleMap != null) {
            if (isHeader) {
                if (HorizontalAlignment.LEFT.equals(align)) {
                    if (BorderStyle.THICK.equals(border)) {
                        style = styleMap.get("L_THICK_HEADER");
                    } else if (BorderStyle.THIN.equals(border)) {
                        style = styleMap.get("L_THIN_HEADER");
                    } else {
                        style = styleMap.get("L_NONE_HEADER");
                    }
                } else if (HorizontalAlignment.CENTER.equals(align)) {
                    if (BorderStyle.THICK.equals(border)) {
                        style = styleMap.get("C_THICK_HEADER");
                    } else if (BorderStyle.THIN.equals(border)) {
                        style = styleMap.get("C_THIN_HEADER");
                    } else {
                        style = styleMap.get("C_NONE_HEADER");
                    }
                } else if (HorizontalAlignment.RIGHT.equals(align)) {
                    if (BorderStyle.THICK.equals(border)) {
                        style = styleMap.get("R_THICK_HEADER");
                    } else if (BorderStyle.THIN.equals(border)) {
                        style = styleMap.get("R_THIN_HEADER");
                    } else {
                        style = styleMap.get("R_NONE_HEADER");
                    }
                }
            } else {
                if (HorizontalAlignment.LEFT.equals(align)) {
                    if (BorderStyle.THICK.equals(border)) {
                        style = styleMap.get("L_THICK");
                    } else if (BorderStyle.THIN.equals(border)) {
                        style = styleMap.get("L_THIN");
                    } else {
                        style = styleMap.get("L_NONE");
                    }
                } else if (HorizontalAlignment.CENTER.equals(align)) {
                    if (BorderStyle.THICK.equals(border)) {
                        style = styleMap.get("C_THICK");
                    } else if (BorderStyle.THIN.equals(border)) {
                        style = styleMap.get("C_THIN");
                    } else {
                        style = styleMap.get("C_NONE");
                    }
                } else if (HorizontalAlignment.RIGHT.equals(align)) {
                    if (BorderStyle.THICK.equals(border)) {
                        style = styleMap.get("R_THICK");
                    } else if (BorderStyle.THIN.equals(border)) {
                        style = styleMap.get("R_THIN");
                    } else {
                        style = styleMap.get("R_NONE");
                    }
                }
            }

            if (style == null) {
                style = styleMap.get("L_NONE");
            }
        } else {
            Font font = workbook.createFont();
            font.setFontHeightInPoints((short) 12);

            style = workbook.createCellStyle();
            style.setAlignment(align);
            style.setVerticalAlignment(VerticalAlignment.CENTER);
            style.setBorderLeft(border);
            style.setBorderRight(border);
            style.setBorderBottom(border);
            style.setBorderTop(border);

            if (isHeader) {
                style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
                style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                font.setBold(true);
            } else {
                style.setWrapText(true);
            }

            style.setFont(font);

        }

        if (colors != null) {
            style.setFillForegroundColor(colors.getIndex());
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }
        cell.setCellStyle(style);

        return cell;
    }

    /**
     * Auto size column.
     *
     * @param workbook the workbook
     */
    public void autoSizeColumn(XSSFWorkbook workbook) {
        for (int idx = 0; idx < workbook.getNumberOfSheets(); idx++) {
            XSSFSheet sheet = workbook.getSheetAt(idx);

            int rowCnt = sheet.getPhysicalNumberOfRows();
            int rowIdx = 0;
            if (rowCnt > 0) {
                XSSFRow row = null;

                while (rowIdx < rowCnt) {
                    row = sheet.getRow(rowIdx);

                    if (row != null) {
                        break;
                    }

                    rowIdx++;
                }

                if (row != null) {
                    Iterator<Cell> cellIterator = row.cellIterator();
                    while (cellIterator.hasNext()) {
                        XSSFCell cell = (XSSFCell) cellIterator.next();

                        if (cell != null) {
                            int columnIndex = cell.getColumnIndex();
                            sheet.autoSizeColumn(columnIndex, true);
                            int currentColumnWidth = sheet.getColumnWidth(columnIndex);
                            int maxColumnWidth = (currentColumnWidth + 500);
                            if (maxColumnWidth > 30000) {
                                maxColumnWidth = 30000;
                            }
                            sheet.setColumnWidth(columnIndex, maxColumnWidth);
                        }
                    }
                }
            }
        }
    }
}