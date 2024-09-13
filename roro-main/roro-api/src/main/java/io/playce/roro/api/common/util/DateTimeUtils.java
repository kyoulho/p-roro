/*
 * Copyright 2020 The Playce-RoRo Project.
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
 * Jeongho Baek     10월 21, 2020        First Draft.
 */
package io.playce.roro.api.common.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 1.0
 */
public class DateTimeUtils {
    public static final String DEFAULT_FILEPATH_FORMAT = "yyyyMMddHHmmss";
    public static final String DEFAULT_DATETIME = "yyyy-MM-dd HH:mm:ss";
    public static final String DEFAULT_DATE = "yyyy-MM-dd";
    public static final String DEFAULT_TIME = "HH:mm:ss";

    /**
     * Convert default date time format string.
     *
     * @param date the date
     *
     * @return the string
     */
    public static String convertDefaultDateTimeFormat(Date date) {
        return convertDefaultDateTimeFormat(date, DEFAULT_DATETIME);
    }

    /**
     * Convert default date format string.
     *
     * @param date the date
     *
     * @return the string
     */
    public static String convertDefaultDateFormat(Date date) {
        return convertDefaultDateTimeFormat(date, DEFAULT_DATE);
    }

    /**
     * Convert string to date.
     *
     * @param dateStr
     * @param sdf
     *
     * @return
     */
    public static Date convertToDate(String dateStr, SimpleDateFormat sdf) {
        Date date = null;
        if (dateStr != null) {
            try {
                date = sdf.parse(dateStr);
            } catch (ParseException e) {
                // ignore
            }
        }

        return date;
    }

    /**
     * Convert default time format string.
     *
     * @param date the date
     *
     * @return the string
     */
    public static String convertDefaultTimeFormat(Date date) {
        return convertDefaultDateTimeFormat(date, DEFAULT_TIME);
    }

    public static String getDefaultFilePath(Date date) {
        return convertDefaultDateTimeFormat(date, DEFAULT_FILEPATH_FORMAT);
    }

    public static String convertDefaultDateTimeFormat(Date date, String format) {
        if (date != null) {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            return sdf.format(date);
        }
        return "";
    }
}
