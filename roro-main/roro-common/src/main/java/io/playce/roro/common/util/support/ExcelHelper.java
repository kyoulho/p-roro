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
package io.playce.roro.common.util.support;

import com.fasterxml.jackson.databind.JsonNode;
import io.playce.roro.common.exception.RoRoException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.text.WordUtils;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
@Slf4j
public class ExcelHelper {

    private static final String REF_SHEET_NAME = "Cell Reference";
    private static final Pattern UTF_PATTERN = Pattern.compile("_(x[0-9A-F]{4}_)");

    private static final String UNICODE_CHARACTER_LOW_LINE = "_x005F_";

    /**
     * @param workbook
     * @param linkAddress
     * @param cell
     * @param isHeader
     * @param styleMap
     */
    public static void makeLinkToSource(SXSSFWorkbook workbook, String linkAddress, SXSSFCell cell, boolean isHeader, Map<String, CellStyle> styleMap) {
        CreationHelper createHelper = workbook.getCreationHelper();
        Hyperlink link = createHelper.createHyperlink(HyperlinkType.DOCUMENT);
        link.setAddress(linkAddress);

        cell.setHyperlink(link);

        if (isHeader) {
            cell.setCellStyle(styleMap.get("C_THIN_HEADER_LINK"));
        } else {
            cell.setCellStyle(styleMap.get("C_THIN_LINK"));
        }
    }

    /**
     * Init cell style map.
     *
     * @param workbook the workbook
     * @return the map
     */
    public static Map<String, CellStyle> initCellStyle(Workbook workbook) {
        Map<String, CellStyle> styleMap = new HashMap<>();

        DataFormat format = workbook.createDataFormat();
        CellStyle style;

        style = workbook.createCellStyle();
        style.setDataFormat(format.getFormat("0"));
        styleMap.put("0", style);

        style = workbook.createCellStyle();
        style.setDataFormat(format.getFormat("#,##0"));
        styleMap.put("#,##0", style);

        style = workbook.createCellStyle();
        style.setDataFormat(format.getFormat("#,##0.00"));
        styleMap.put("#,##0.00", style);

        style = workbook.createCellStyle();
        style.setDataFormat(format.getFormat("#.##"));
        styleMap.put("#.##", style);

        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 12);

        Font boldFont = workbook.createFont();
        boldFont.setFontHeightInPoints((short) 12);
        boldFont.setBold(true);

        Font linkFont = workbook.createFont();
        linkFont.setFontHeightInPoints((short) 12);
        linkFont.setUnderline(Font.U_SINGLE);
        linkFont.setColor(IndexedColors.BLUE.getIndex());

        Font linkBoldFont = workbook.createFont();
        linkBoldFont.setFontHeightInPoints((short) 12);
        linkBoldFont.setBold(true);
        linkBoldFont.setUnderline(Font.U_SINGLE);
        linkBoldFont.setColor(IndexedColors.BLUE.getIndex());

        style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderLeft(BorderStyle.THICK);
        style.setBorderRight(BorderStyle.THICK);
        style.setBorderBottom(BorderStyle.THICK);
        style.setBorderTop(BorderStyle.THICK);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setFont(boldFont);
        styleMap.put("L_THICK_HEADER", style);

        style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderLeft(BorderStyle.THICK);
        style.setBorderRight(BorderStyle.THICK);
        style.setBorderBottom(BorderStyle.THICK);
        style.setBorderTop(BorderStyle.THICK);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setFont(boldFont);
        styleMap.put("C_THICK_HEADER", style);

        style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderLeft(BorderStyle.THICK);
        style.setBorderRight(BorderStyle.THICK);
        style.setBorderBottom(BorderStyle.THICK);
        style.setBorderTop(BorderStyle.THICK);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setFont(boldFont);
        styleMap.put("R_THICK_HEADER", style);

        style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setFont(boldFont);
        styleMap.put("L_THIN_HEADER", style);

        style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setFont(boldFont);
        styleMap.put("C_THIN_HEADER", style);

        style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setFont(linkBoldFont);
        styleMap.put("C_THIN_HEADER_LINK", style);

        style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setFont(boldFont);
        styleMap.put("R_THIN_HEADER", style);

        style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderLeft(BorderStyle.NONE);
        style.setBorderRight(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.NONE);
        style.setBorderTop(BorderStyle.NONE);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setFont(boldFont);
        styleMap.put("L_NONE_HEADER", style);

        style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderLeft(BorderStyle.NONE);
        style.setBorderRight(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.NONE);
        style.setBorderTop(BorderStyle.NONE);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setFont(boldFont);
        styleMap.put("C_NONE_HEADER", style);

        style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderLeft(BorderStyle.NONE);
        style.setBorderRight(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.NONE);
        style.setBorderTop(BorderStyle.NONE);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setFont(boldFont);
        styleMap.put("R_NONE_HEADER", style);

        style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderLeft(BorderStyle.THICK);
        style.setBorderRight(BorderStyle.THICK);
        style.setBorderBottom(BorderStyle.THICK);
        style.setBorderTop(BorderStyle.THICK);
        style.setFont(font);
        style.setWrapText(true);
        styleMap.put("L_THICK", style);

        style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderLeft(BorderStyle.THICK);
        style.setBorderRight(BorderStyle.THICK);
        style.setBorderBottom(BorderStyle.THICK);
        style.setBorderTop(BorderStyle.THICK);
        style.setFont(font);
        style.setWrapText(true);
        styleMap.put("C_THICK", style);

        style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderLeft(BorderStyle.THICK);
        style.setBorderRight(BorderStyle.THICK);
        style.setBorderBottom(BorderStyle.THICK);
        style.setBorderTop(BorderStyle.THICK);
        style.setFont(font);
        style.setWrapText(true);
        styleMap.put("R_THICK", style);

        style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setFont(font);
        style.setWrapText(true);
        styleMap.put("L_THIN", style);

        style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setFont(linkFont);
        style.setWrapText(true);
        styleMap.put("L_THIN_LINK", style);

        style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setFont(font);
        style.setWrapText(true);
        styleMap.put("C_THIN", style);

        style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setFont(linkFont);
        style.setWrapText(true);
        styleMap.put("C_THIN_LINK", style);

        style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setFont(font);
        style.setWrapText(true);
        styleMap.put("R_THIN", style);

        style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderLeft(BorderStyle.NONE);
        style.setBorderRight(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.NONE);
        style.setBorderTop(BorderStyle.NONE);
        style.setFont(font);
        style.setWrapText(true);
        styleMap.put("L_NONE", style);

        style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderLeft(BorderStyle.NONE);
        style.setBorderRight(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.NONE);
        style.setBorderTop(BorderStyle.NONE);
        style.setFont(linkFont);
        style.setWrapText(true);
        styleMap.put("L_NONE_LINK", style);

        style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderLeft(BorderStyle.NONE);
        style.setBorderRight(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.NONE);
        style.setBorderTop(BorderStyle.NONE);
        style.setFont(font);
        style.setWrapText(true);
        styleMap.put("C_NONE", style);

        style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderLeft(BorderStyle.NONE);
        style.setBorderRight(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.NONE);
        style.setBorderTop(BorderStyle.NONE);
        style.setFont(font);
        style.setWrapText(true);
        styleMap.put("R_NONE", style);

        style = workbook.createCellStyle();
        style.setVerticalAlignment(VerticalAlignment.TOP);
        style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setFont(font);
        style.setWrapText(true);
        styleMap.put("REF_THIN", style);

        style = workbook.createCellStyle();
        style.setVerticalAlignment(VerticalAlignment.TOP);
        style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.NONE);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setFont(font);
        style.setWrapText(true);
        styleMap.put("REF_NONE", style);

        return styleMap;
    }

    /**
     * Init font map.
     *
     * @param workbook the workbook
     * @return the map
     */
    public static Map<String, Font> initFont(SXSSFWorkbook workbook) {
        Map<String, Font> fontMap = new HashMap<>();

        Font font;

        font = workbook.createFont();
        font.setFontHeightInPoints((short) 16);
        font.setBold(true);
        font.setItalic(true);
        fontMap.put("16_BI", font);

        font = workbook.createFont();
        font.setFontHeightInPoints((short) 14);
        font.setBold(true);
        font.setItalic(true);
        fontMap.put("14_BI", font);

        font = workbook.createFont();
        font.setFontHeightInPoints((short) 12);
        font.setBold(true);
        font.setItalic(true);
        fontMap.put("12_BI", font);

        font = workbook.createFont();
        font.setFontHeightInPoints((short) 16);
        font.setBold(true);
        fontMap.put("16_B", font);

        font = workbook.createFont();
        font.setFontHeightInPoints((short) 14);
        font.setBold(true);
        fontMap.put("14_B", font);

        font = workbook.createFont();
        font.setFontHeightInPoints((short) 12);
        font.setBold(true);
        fontMap.put("12_B", font);

        font = workbook.createFont();
        font.setFontHeightInPoints((short) 16);
        font.setItalic(true);
        fontMap.put("16_I", font);

        font = workbook.createFont();
        font.setFontHeightInPoints((short) 14);
        font.setItalic(true);
        fontMap.put("14_I", font);

        font = workbook.createFont();
        font.setFontHeightInPoints((short) 12);
        font.setItalic(true);
        fontMap.put("12_I", font);

        font = workbook.createFont();
        font.setFontHeightInPoints((short) 20);
        font.setBold(true);
        fontMap.put("20_B", font);

        font = workbook.createFont();
        font.setFontHeightInPoints((short) 16);
        fontMap.put("16", font);

        font = workbook.createFont();
        font.setFontHeightInPoints((short) 14);
        fontMap.put("14", font);

        font = workbook.createFont();
        font.setFontHeightInPoints((short) 12);
        fontMap.put("12", font);

        font = workbook.createFont();
        font.setFontHeightInPoints((short) 11);
        fontMap.put("11", font);

        font = workbook.createFont();
        font.setFontHeightInPoints((short) 12);
        font.setUnderline(Font.U_SINGLE);
        font.setColor(IndexedColors.BLUE.getIndex());
        fontMap.put("LINK", font);

        return fontMap;
    }

    /**
     * Init tab colors.
     *
     * @return the map
     */
    public static List<Short> initTabColors() {
        List<Short> colorList = new ArrayList<>();

        colorList.add(IndexedColors.ROSE.getIndex());
        colorList.add(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        colorList.add(IndexedColors.SKY_BLUE.getIndex());
        colorList.add(IndexedColors.LIME.getIndex());
        colorList.add(IndexedColors.GOLD.getIndex());
        colorList.add(IndexedColors.AQUA.getIndex());
        colorList.add(IndexedColors.INDIGO.getIndex());

        return colorList;
    }

    /**
     * @param workbook
     * @param row
     * @param align
     * @param border
     * @param isHeader
     * @return
     */
    public static SXSSFCell createCellWithBorder(SXSSFWorkbook workbook, SXSSFRow row, HorizontalAlignment align, BorderStyle border, boolean isHeader) {
        return createCellWithBorder(workbook, row, null, align, border, isHeader);
    }

    /**
     * Create cell with border xssf cell.
     *
     * @param workbook the workbook
     * @param row      the row
     * @param align    the align
     * @param border   the border
     * @param isHeader the is header
     * @return the xssf cell
     */
    public static SXSSFCell createCellWithBorder(SXSSFWorkbook workbook, SXSSFRow row, HorizontalAlignment align, BorderStyle border, boolean isHeader, Map<String, CellStyle> styleMap) {
        return createCellWithBorder(workbook, row, null, align, border, isHeader, styleMap);
    }

    /**
     * @param workbook
     * @param row
     * @param columnIdx
     * @param align
     * @param border
     * @param isHeader
     * @return
     */
    public static SXSSFCell createCellWithBorder(SXSSFWorkbook workbook, SXSSFRow row, Integer columnIdx, HorizontalAlignment align, BorderStyle border, boolean isHeader) {
        return createCellWithBorder(workbook, row, columnIdx, align, border, isHeader, null);
    }

    /**
     * @param workbook  the workbook
     * @param row       the row
     * @param columnIdx the column idx
     * @param align     the align
     * @param border    the border
     * @param isHeader  the is header
     * @return the sxssf cell
     */
    public static SXSSFCell createCellWithBorder(SXSSFWorkbook workbook, SXSSFRow row, Integer columnIdx, HorizontalAlignment align, BorderStyle border, boolean isHeader, Map<String, CellStyle> styleMap) {
        return createCellWithBorder(workbook, row, columnIdx, align, border, isHeader, styleMap, null);
    }

    /**
     * Workbook 내에서 64000 개 이상의 Cell Style을 생성 시 아래와 같은 오류가 발생.
     * (java.lang.IllegalStateException: The maximum number of Cell Styles was exceeded. You can define up to 64000 style in a .xlsx Workbook)
     * <p>
     * 따라서 생성된 CellStyle은 최대한 재활용을 해야하며, cell.getCellStyle() 을 사용하여 스타일을 변경하는 경우 재활용 대상이 아님.
     *
     * @param workbook  the workbook
     * @param row       the row
     * @param columnIdx the column idx
     * @param align     the align
     * @param border    the border
     * @param isHeader  the is header
     * @param colors    the colors
     * @return the sxssf cell
     */
    public static SXSSFCell createCellWithBorder(SXSSFWorkbook workbook, SXSSFRow row, Integer columnIdx, HorizontalAlignment align, BorderStyle border, boolean isHeader, Map<String, CellStyle> styleMap, IndexedColors colors) {
        SXSSFCell cell = null;
        if (columnIdx != null) {
            cell = row.createCell(columnIdx);
        }

        if (cell == null) {
            cell = row.createCell(row.getLastCellNum() == -1 ? 0 : row.getLastCellNum());
        }

        //cell.getSheet().autoSizeColumn(cell.getColumnIndex(), true);

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
     * Create contents.
     *
     * @param workbook the workbook
     */
    public static void createContents(SXSSFWorkbook workbook, Map<String, CellStyle> styleMap) {
        CreationHelper createHelper = workbook.getCreationHelper();

        //cell style for hyperlinks
        //by default hyperlinks are blue and underlined
        Font linkFont = workbook.createFont();
        linkFont.setUnderline(Font.U_SINGLE);
        linkFont.setColor(IndexedColors.BLUE.getIndex());

        SXSSFSheet contentsSheet = workbook.getSheetAt(1);
        contentsSheet.trackAllColumnsForAutoSizing();
        SXSSFRow row = contentsSheet.createRow(contentsSheet.getLastRowNum() + 1);

        SXSSFCell cell = createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("No");

        cell = createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("Sheet Name");

        SXSSFSheet dataSheet;
        Hyperlink link;
        for (int i = 2; i < workbook.getNumberOfSheets(); i++) {
            dataSheet = workbook.getSheetAt(i);

            if (!REF_SHEET_NAME.equals(dataSheet.getSheetName())) {
                link = createHelper.createHyperlink(HyperlinkType.DOCUMENT);
                link.setAddress("'" + dataSheet.getSheetName() + "'!A1");

                row = contentsSheet.createRow(contentsSheet.getLastRowNum() + 1);
                cell = createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);
                cell.setCellValue(i - 1);

                cell = createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false);
                cell.setHyperlink(link);
                cell.setCellValue(dataSheet.getSheetName());
                cell.setCellStyle(styleMap.get("L_THIN_LINK"));
            }
        }
    }

    public static void replaceContents(SXSSFWorkbook workbook) {
        workbook.removeSheetAt(1);

        var newSheet = workbook.createSheet("Table of Contents");
        List<Short> colorList = ExcelHelper.initTabColors();
        newSheet.setTabColor(colorList.get((1)));
        workbook.setSheetOrder("Table of Contents", 1);

        createContents(workbook, ExcelHelper.initCellStyle(workbook));
    }

    public static Hyperlink addLargeText(SXSSFWorkbook workbook, SXSSFCell sourceCell, String key, String value, Map<String, CellStyle> styleMap) throws InterruptedException {
        CreationHelper createHelper = workbook.getCreationHelper();
        Hyperlink link = createHelper.createHyperlink(HyperlinkType.DOCUMENT);

        SXSSFSheet refSheet = workbook.getSheet(REF_SHEET_NAME);

        if (refSheet == null) {
            refSheet = workbook.createSheet(REF_SHEET_NAME);
            refSheet.trackAllColumnsForAutoSizing();
            refSheet.setTabColor(IndexedColors.SKY_BLUE.getIndex());
        }

        int rowIdx = 0;
        int cellIdx = 0;

        SXSSFRow row = refSheet.getRow(rowIdx);

        if (row == null) {
            row = refSheet.createRow(rowIdx);
        }

        SXSSFCell cell = createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue(key);

        int ci = sourceCell.getColumnIndex();
        int ri = sourceCell.getRowIndex() + 1;
        String columnLetter = CellReference.convertNumToColString(ci);

        String linkAddress = "'" + sourceCell.getRow().getSheet().getSheetName() + "'!" + columnLetter + ri;
        makeLinkToSource(workbook, linkAddress, cell, true, styleMap);

        cellIdx = cell.getColumnIndex();
        columnLetter = CellReference.convertNumToColString(cellIdx);

        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(value.getBytes())))) {
            StringBuilder sb = new StringBuilder();
            String lineStr = null;
            int lineNum = 1;
            while ((lineStr = buffer.readLine()) != null) {
                if (lineNum++ % 25 == 0) {
                    rowIdx = setLargeTextValue(refSheet, rowIdx, cellIdx, sb.toString(), BorderStyle.NONE, styleMap);
                    sb = new StringBuilder();
                }

                sb.append(lineStr).append("\n");
            }

            if (sb.length() > 0) {
                setLargeTextValue(refSheet, rowIdx, cellIdx, sb.toString(), BorderStyle.THIN, styleMap);
            }

            link.setAddress("'" + refSheet.getSheetName() + "'!" + columnLetter + "1");
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            log.error("Exception occurred while create a link for cell reference.", e);
            link = null;
        }

        return link;
    }

    /**
     * Auto size column.
     *
     * @param workbook the workbook
     */
    public static void autoSizeColumn(SXSSFWorkbook workbook) throws InterruptedException {
        SXSSFSheet sheet;

        int start = 0;
        if (workbook.getNumberOfSheets() > 1) {
            start = 1;
        }

        for (int i = start; i < workbook.getNumberOfSheets(); i++) {
            try {
                sheet = workbook.getSheetAt(i);

                int rowCnt = sheet.getPhysicalNumberOfRows();
                int rowIdx = 0;
                if (rowCnt > 0) {
                    SXSSFRow row = null;

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
                            SXSSFCell cell = (SXSSFCell) cellIterator.next();

                            if (cell != null) {
                                int columnIndex = cell.getColumnIndex();
                                sheet.autoSizeColumn(columnIndex, true);
                                int currentColumnWidth = sheet.getColumnWidth(columnIndex);
                                int maxColumnWidth = (currentColumnWidth + 2500);
                                if (maxColumnWidth > 30000) {
                                    maxColumnWidth = 30000;
                                }
                                sheet.setColumnWidth(columnIndex, maxColumnWidth);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                RoRoException.checkInterruptedException(e);
                // ignore
            }
        }

        // Move "Cell Reference" sheet to last
        if (workbook.getSheet(REF_SHEET_NAME) != null) {
            workbook.setSheetOrder(REF_SHEET_NAME, workbook.getNumberOfSheets() - 1);
        }
    }

    /**
     * Sets cell data.
     *
     * @param workbook the workbook
     * @param cell     the cell
     * @param node     the node
     * @param nodeName the node name
     * @param styleMap the style map
     * @param fontMap  the font map
     */
    public static void setCellData(SXSSFWorkbook workbook, SXSSFCell cell, JsonNode node, String nodeName, Map<String, CellStyle> styleMap, Map<String, Font> fontMap) throws InterruptedException {
        if (node != null) {
            if (nodeName == null) {
                nodeName = "";
            }

            if (node.isNumber()) {
                if ((nodeName.toLowerCase().contains("date") || nodeName.toLowerCase().contains("time") || nodeName.toLowerCase().contains("created")) && !nodeName.contains("picker")) {
                    Long timestamp = Double.valueOf(node.asDouble()).longValue();
                    if (timestamp < 10000000000L) {
                        timestamp *= 1000L;
                    }

                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String cellValue = formatter.format(new Date(timestamp));
                    cell.setCellValue(cellValue);
                } else {
                    if (node.isInt()) {
                        cell.setCellValue(node.asInt());
                        if (!nodeName.toLowerCase().contains("port") && !nodeName.toLowerCase().contains("id")) {
                            CellStyle style = copyCellStyle(workbook, cell.getCellStyle());
                            style.setDataFormat(styleMap.get("#,##0").getDataFormat());
                            cell.setCellStyle(style);
                            // cell.getCellStyle().setDataFormat(styleMap.get("#,##0").getDataFormat());
                        }
                    } else if (node.isLong()) {
                        cell.setCellValue(node.asLong());
                        CellStyle style = copyCellStyle(workbook, cell.getCellStyle());
                        style.setDataFormat(styleMap.get("#,##0").getDataFormat());
                        cell.setCellStyle(style);
                        // cell.getCellStyle().setDataFormat(styleMap.get("#,##0").getDataFormat());
                    } else if (node.isFloat()) {
                        cell.setCellValue(node.asDouble());
                        CellStyle style = copyCellStyle(workbook, cell.getCellStyle());
                        style.setDataFormat(styleMap.get("#,##0.00").getDataFormat());
                        cell.setCellStyle(style);
                        // cell.getCellStyle().setDataFormat(styleMap.get("#,##0.00").getDataFormat());
                    } else if (node.isDouble()) {
                        cell.setCellValue(node.asDouble());
                        CellStyle style = copyCellStyle(workbook, cell.getCellStyle());
                        style.setDataFormat(styleMap.get("#,##0.00").getDataFormat());
                        cell.setCellStyle(style);
                        // cell.getCellStyle().setDataFormat(styleMap.get("#,##0.00").getDataFormat());
                    } else if (node.isShort()) {
                        cell.setCellValue(node.asInt());
                    } else {
                        cell.setCellValue(node.asText());
                    }
                }
            } else if (node.isBoolean()) {
                cell.setCellValue(node.booleanValue());
            } else if (node.isTextual()) {
                String value = node.textValue().replaceAll("\t", "    ");

                if (value.length() > 30000) {
                    Hyperlink link = addLargeText(workbook, cell, nodeName, value, styleMap);

                    cell.setCellValue("View " + nodeName);
                    if (link != null) {
                        cell.setHyperlink(link);
                        cell.setCellStyle(styleMap.get("L_THIN_LINK"));
                    }
                } else {
                    cell.setCellValue(escape(value));
                }

                if (cell.getStringCellValue().startsWith("-") && !NumberUtils.isCreatable(cell.getStringCellValue())) {
                    CellStyle style = copyCellStyle(workbook, cell.getCellStyle());
                    style.setQuotePrefixed(true);
                    cell.setCellStyle(style);
                }
            } else if (node.isArray()) {
                List<String> nodeList = new ArrayList<>();

                if (nodeName.equalsIgnoreCase("proxySetHeader")) {
                    String header;
                    for (int i = 0; i < node.size(); i++) {
                        header = String.valueOf(node.get(i));
                        if (header.startsWith("\"") && header.endsWith("\"")) {
                            header = header.substring(1, header.length() - 1);
                        }

                        nodeList.add(header.replaceAll("\\\\", ""));
                    }
                } else {
                    for (int i = 0; i < node.size(); i++) {
                        nodeList.add(String.valueOf(node.get(i)).replaceAll("\"", ""));
                    }
                }

                if (nodeName.equalsIgnoreCase("listenport")) {
                    cell.setCellValue(escape(String.join(", ", nodeList)));
                } else {
                    cell.setCellValue(escape(String.join("\r\n", nodeList)));
                }

                if (cell.getStringCellValue().startsWith("-") && !NumberUtils.isCreatable(cell.getStringCellValue())) {
                    CellStyle style = copyCellStyle(workbook, cell.getCellStyle());
                    style.setQuotePrefixed(true);
                    cell.setCellStyle(style);
                }
            }
        }
    }

    /**
     * Sets cell data.
     *
     * @param workbook
     * @param cell
     * @param value
     */
    public static void setCellData(SXSSFWorkbook workbook, SXSSFCell cell, Map<String, CellStyle> styleMap, Object value, String nodeName) {
        if (value != null) {
            if (value instanceof Integer || value instanceof Long || value instanceof Float || value instanceof Double) {
                CellStyle style = copyCellStyle(workbook, cell.getCellStyle());
                style.setAlignment(HorizontalAlignment.RIGHT);

                if (nodeName != null && !nodeName.toLowerCase().endsWith("id") && !nodeName.toLowerCase().contains("port")) {
                    try {
                        Long.parseLong(value.toString());
                        cell.setCellValue(Long.valueOf(value.toString()));
                        //style.setDataFormat(styleMap.get("#,##0").getDataFormat());
                    } catch (Exception ignore) {
                        Double.parseDouble(value.toString());
                        cell.setCellValue(Double.valueOf(value.toString()));
                        //style.setDataFormat(styleMap.get("#,##0.00").getDataFormat());
                    }
                } else {
                    cell.setCellValue(Double.valueOf(value.toString()));
                }
                cell.setCellStyle(style);
            } else {
                cell.setCellValue(value.toString());
                // cell.setCellValue(escape(value.toString()));

                // 음수가 아닌 -로 시작하는 문자는 ' 문자를 붙여준다.
                if (cell.getStringCellValue().startsWith("-") && !NumberUtils.isCreatable(cell.getStringCellValue())) {
                    CellStyle style = copyCellStyle(workbook, cell.getCellStyle());
                    style.setQuotePrefixed(true);
                    cell.setCellStyle(style);
                }
            }
        }
    }

    /**
     * <pre>
     * Capitalize string.
     * eg. fileSummaryMap change to File Summary Map
     * </pre>
     *
     * @param name the name
     * @return the string
     */
    public static String capitalize(String name) {
        if (name.contains("(DB)")) {
            return name;
        } else if (name.equalsIgnoreCase("cpu")) {
            return "CPU";
        } else if (name.equalsIgnoreCase("dns")) {
            return "DNS";
        } else if (name.equalsIgnoreCase("hardCodedIpList")) {
            return "Hard Coded IP List";
        }

        return WordUtils.capitalize(name.replaceAll("_", " "))
                .replaceAll("(?!^)([A-Z])", " $1")
                .replaceAll("  ", " ");
    }

    /**
     * Sets large text value.
     *
     * @param sheet       the sheet
     * @param rowIdx      the row idx
     * @param cellIdx     the cell idx
     * @param value       the value
     * @param borderStyle the borderStyle
     * @param styleMap    the styleMap
     * @return the large text value
     */
    private static int setLargeTextValue(SXSSFSheet sheet, int rowIdx, int cellIdx, String value, BorderStyle borderStyle, Map<String, CellStyle> styleMap) {
        int size = 2000;

        for (int start = 0; start < value.length(); start += size) {
            rowIdx++;
            SXSSFRow row = sheet.getRow(rowIdx);
            if (row == null) {
                row = sheet.createRow(rowIdx);
            }

            SXSSFCell cell = row.createCell(cellIdx);

            if ((start + size) >= value.length() && !borderStyle.equals(BorderStyle.NONE)) {
                cell.setCellStyle(styleMap.get("REF_THIN"));
            } else {
                cell.setCellStyle(styleMap.get("REF_NONE"));
            }

            cell.setCellValue(value.substring(start, Math.min(value.length(), start + size)));
        }

        return rowIdx;
    }

    /**
     * Copy cell style.
     *
     * @param workbook
     * @param origin
     * @return
     */
    public static CellStyle copyCellStyle(SXSSFWorkbook workbook, CellStyle origin) {
        CellStyle style = workbook.createCellStyle();

        style.setAlignment(origin.getAlignment());
        style.setVerticalAlignment(origin.getVerticalAlignment());
        style.setBorderTop(origin.getBorderTop());
        style.setBorderBottom(origin.getBorderBottom());
        style.setBorderLeft(origin.getBorderLeft());
        style.setBorderRight(origin.getBorderRight());
        style.setFillBackgroundColor(origin.getFillBackgroundColor());
        style.setFillForegroundColor(origin.getFillForegroundColor());
        style.setFillPattern(origin.getFillPattern());
        style.setWrapText(origin.getWrapText());
        style.setDataFormat(origin.getDataFormat());

        return style;
    }

    /**
     * @param value
     * @return
     */
    private static String escape(final String value) {
        if (StringUtils.isEmpty(value)) {
            return "";
        }

        StringBuffer buf = new StringBuffer();
        Matcher m = UTF_PATTERN.matcher(value);
        int idx = 0;
        while (m.find()) {
            int pos = m.start();
            if (pos > idx) {
                buf.append(value.substring(idx, pos));
            }

            buf.append(UNICODE_CHARACTER_LOW_LINE + m.group(1));

            idx = m.end();
        }

        buf.append(value.substring(idx));

        return buf.toString();
    }
}
//end of ExcelHelper.java