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
 * Author			Date				Description
 * ---------------	----------------	------------
 * Jeongho Baek   9월 10, 2021		First Draft.
 */
package io.playce.roro.asmt.windows.impl.factory;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.InetAddressValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Jeongho Baek
 * @version 2.0.0
 */
public class PowerShellParseUtil {

    private static final String DELIMITER_COLON = ":";
    private static final String FORWARD = "forward";
    private static final String BACKWARD = "backward";

    public static String[] splitToArrayByCrlf(String splitString) {
        return splitString.split("\\r?\\n");
    }

    public static String getSystemPropertyValue(String source, String property) {
        return getSystemPropertyValue(source, property, FORWARD);
    }

    public static String getSystemPropertyValue(String source, String property, String indexDirection) {

        try {
            String[] lineStr = splitToArrayByCrlf(source);

            for (String tempString : lineStr) {
                if (tempString.contains(DELIMITER_COLON) && tempString.startsWith(property)) {

                    if (indexDirection.equals(BACKWARD)) {
                        return tempString.substring(tempString.lastIndexOf(DELIMITER_COLON) + 1).trim();
                    } else {
                        return tempString.substring(tempString.indexOf(DELIMITER_COLON) + 1).trim();
                    }

                }
            }

            return "";
        } catch (Exception ignore) {
            return "";
        }

    }

    public static String getPropertyValueForOneLine(String source, String property) {
        return getPropertyValueForOneLine(source, property, DELIMITER_COLON);
    }

    public static String getPropertyValueForOneLine(String source, String property, String delim) {
        try {
            if (source.startsWith(property)) {
                return source.substring(source.indexOf(delim) + 1).trim();
            }
        } catch (Exception ignore) {
            return "";
        }

        return "";
    }

    public static String getPropertyValueForMultiLine(String source, String property) {
        try {
            String[] lineStr = splitToArrayByCrlf(source);

            for (String tempString : lineStr) {
                if (tempString.startsWith(property)) {
                    return tempString.substring(property.length() + 1).trim();
                }
            }

            return "";
        } catch (Exception ignore) {
            return "";
        }
    }

    public static boolean isValidIp4Address(String ipAddress) {
        final InetAddressValidator validator = InetAddressValidator.getInstance();

        return validator.isValidInet4Address(ipAddress);
    }

    public static String getIntegerDivideKilobyte(String number) {
        if (StringUtils.isNotEmpty(number)) {
            return (Long.parseLong(number) / 1024) + "";
        }

        return StringUtils.defaultString(number);
    }

    public static String getDecimalDivideKilobyte(String number) {
        if (StringUtils.isNotEmpty(number)) {
            Double decimal = (Double.parseDouble(number) / (1024));
            return String.format("%.2f", decimal);
        }

        return StringUtils.defaultString(number);
    }

    public static String getAddressFamily(String addressFamily) {
        if(StringUtils.defaultString(addressFamily).equals("2")) {
            return "IPv4";
        } else if(StringUtils.defaultString(addressFamily).equals("23")) {
            return "IPv6";
        } else {
            return addressFamily;
        }
    }

    @SuppressWarnings("unchecked")
    public static List<String> convertObjectToList(Object object) {
        List<String> tempStrings = new ArrayList<>();

        if (object instanceof List) {
            tempStrings = (List<String>) object;
        } else {
            tempStrings.add(StringUtils.defaultString((String) object));
        }

        return tempStrings;
    }

    public static boolean isJsonArray(String json) {
        return json.startsWith("[");
    }

}
//end of PowerShellParseUtil.java