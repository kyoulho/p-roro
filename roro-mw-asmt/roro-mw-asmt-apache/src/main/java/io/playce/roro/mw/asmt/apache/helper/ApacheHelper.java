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
 * Jaeeon Bae       3월 22, 2021            First Draft.
 */
package io.playce.roro.mw.asmt.apache.helper;

import io.playce.roro.mw.asmt.apache.dto.ApacheAssessmentResult;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Jaeeon Bae
 * @version 2.0.0
 */
public class ApacheHelper {

    // Directive
    public static final String define = "Define";
    public static final String listen = "Listen";
    public static final String serverRoot = "ServerRoot";
    public static final String documentRoot = "DocumentRoot";
    public static final String include = "Include";
    public static final String sslEngine = "SSLEngine";
    public static final String sslCertificate = "SSLCertificate";
    public static final String setEnvIf = "SetEnvIf";

    // WAS connection
    public static final String webSpherePluginConfig = "WebSpherePluginConfig"; // WebSphere
    public static final String jkWorkersFile = "JkWorkersFile"; // tomcat & jeus
    public static final String jkMountFile = "JkMountFile";     // tomcat & jeus

    // LoadModule
    public static final String loadModule = "LoadModule";

    // Enclosure
    public static final String ifModlue = "IfModule";
    public static final String virtualHost = "VirtualHost";
    public static final String directory = "Directory";
    public static final String files = "Files";
    public static final String location = "Location";
    public static final String proxy = "Proxy";

    // KeepAlive
    public static final String keepAlive = "KeepAlive";
    public static final String maxKeepAliveRequests = "MaxKeepAliveRequests";

    // Solution Specific
    public static final String useCanonicalName = "UseCanonicalName";
    public static final String serverTokens = "ServerTokens";
    public static final String traceEnable = "TraceEnable";
    public static final String hostnameLookups = "HostnameLookups";
    public static final String user = "User";
    public static final String group = "Group";
    public static final String serverAdmin = "ServerAdmin";
    public static final String serverSignature = "ServerSignature";

    // Logs
    public static final String logLevel = "LogLevel";
    public static final String errorLog = "ErrorLog";
    public static final String logFormat = "LogFormat";
    public static final String customLog = "CustomLog";

    // Error Documents
    public static final String errorDocument = "ErrorDocument";

    // Browser Matches
    public static final String browserMatches = "BrowserMatch";

    private static final Logger logger = LoggerFactory.getLogger(ApacheHelper.class);

    /**
     * ${key}의 내용을 Define 에 선언된 내용으로 치환
     *
     * @param defineMap
     * @param str
     * @return
     */
    public static String replaceDefinesInString(Map<String, String> defineMap, String str) {
        String newStr = str;

        String regex;
        for (String key : defineMap.keySet()) {
            regex = "\\$\\{ *" + key + " *\\}";

            Pattern pattern = Pattern.compile(regex);
            newStr = pattern.matcher(newStr).replaceAll(defineMap.get(key));
        }

        return StringUtils.strip(newStr, StringUtils.SPACE + "\"");
    }

    /**
     * Utility to check if a line matches an Apache comment.
     *
     * @param line
     *            the line to check for a comment.
     * @return a boolean indicating if the line is a comment.
     */
    public static boolean isCommentMatch(String line) {
        Pattern commentPattern = Pattern.compile("^\\s*#");
        return commentPattern.matcher(line).find();
    }

    /**
     * Utility used to check if a line matches an enclosure with a specified type.
     *
     * @param line
     *            the line to match against the enclosure.
     * @return a boolean indicating if the line matches the enclosure.
     */
    public static boolean isEnclosureTypeMatch(String line) {
        // Pattern enclosurePattern = Pattern.compile("<\\s*\\b" + enclosureType + "\\b.*>", Pattern.CASE_INSENSITIVE);
        Pattern enclosurePattern = Pattern.compile("<\\s*\\b.*>", Pattern.CASE_INSENSITIVE);
        return enclosurePattern.matcher(line).find();
    }

    /**
     * Utility used to check if a line matches a closing enclosure with a specified type.
     *
     * @param line
     *            the line to match against the closing enclosure.
     * @return a boolean indicating if the line matches the closing enclosure type.
     */
    public static boolean isCloseEnclosureTypeMatch(String line) {
        // Pattern closeEnclosurePattern = Pattern.compile("</\\s*\\b" + enclosureType + "\\b\\s*>", Pattern.CASE_INSENSITIVE);
        Pattern closeEnclosurePattern = Pattern.compile("</\\s*\\b.*>", Pattern.CASE_INSENSITIVE);
        return closeEnclosurePattern.matcher(line).find();
    }

    /**
     * Replace Enclosure value to instance.
     *
     * @param enclosureType
     * @param key
     * @param parsableLine
     */
    public static void replaceEnclosureToInstance(Map<String, String> defineMap, String enclosureType, String key, List<String> parsableLine, ApacheAssessmentResult.Instance instance) {
        /**
         * Save map object to instance
         *
         *     private static final String ifModlue = "IfModule";
         *     private static final String virtualHost = "VirtualHost";
         *     private static final String directory = "Directory";
         *     private static final String files = "Files";
         *     private static final String location = "Location";
         *     private static final String proxy = "Proxy";
         */
        Map<String, List<String>> enclosureMap = null;

        // Define 에 선언된 내용 치환
        key = replaceDefinesInString(defineMap, key);
        List<String> valueList = parsableLine.stream().map(s -> replaceDefinesInString(defineMap, s).trim()).collect(Collectors.toList());

        if (ifModlue.equals(enclosureType)) {
            if (instance.getIfModule() != null) {
                enclosureMap = new HashMap<>(instance.getIfModule());
            } else {
                enclosureMap = new HashMap<>();
            }
            enclosureMap.put(key, valueList);
            instance.setIfModule(enclosureMap);
        } else if (virtualHost.equals(enclosureType)) {
            if (instance.getVirtualHost() != null) {
                enclosureMap = new HashMap<>(instance.getVirtualHost());
            } else {
                enclosureMap = new HashMap<>();
            }
            enclosureMap.put(key, valueList);
            instance.setVirtualHost(enclosureMap);
        } else if (directory.equals(enclosureType)) {
            if (instance.getDirectory() != null) {
                enclosureMap = new HashMap<>(instance.getDirectory());
            } else {
                enclosureMap = new HashMap<>();
            }
            enclosureMap.put(key, valueList);
            instance.setDirectory(enclosureMap);
        } else if (files.equals(enclosureType)) {
            if (instance.getFiles() != null) {
                enclosureMap = new HashMap<>(instance.getFiles());
            } else {
                enclosureMap = new HashMap<>();
            }
            enclosureMap.put(key, valueList);
            instance.setFiles(enclosureMap);
        } else if (location.equals(enclosureType)) {
            if (instance.getLocation() != null) {
                enclosureMap = new HashMap<>(instance.getLocation());
            } else {
                enclosureMap = new HashMap<>();
            }
            enclosureMap.put(key, valueList);
            instance.setLocation(enclosureMap);
        } else if (proxy.equals(enclosureType)) {
            if (instance.getProxy() != null) {
                enclosureMap = new HashMap<>(instance.getProxy());
            } else {
                enclosureMap = new HashMap<>();
            }
            enclosureMap.put(key, valueList);
            instance.setProxy(enclosureMap);
        }
    }
}
//end of ApacheHttpdHelper.java