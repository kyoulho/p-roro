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
 * SangCheon Park   Jan 12, 2022	    First Draft.
 */
package io.playce.roro.app.asmt.java.threadpool.task;

import io.playce.roro.app.asmt.java.policy.Policy;
import io.playce.roro.app.asmt.result.ApplicationAssessmentResult;
import io.playce.roro.app.asmt.result.ApplicationAssessmentResult.DataSource.JdbcProperty;
import io.playce.roro.common.code.Domain1109;
import io.playce.roro.common.dto.info.JdbcInfo;
import io.playce.roro.common.util.FileUtil;
import io.playce.roro.common.util.JdbcURLParser;
import io.playce.roro.common.util.WellKnownPortUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.benf.cfr.reader.api.CfrDriver;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0.0
 */
@Slf4j
public class DependencyCheckTask extends BaseTask {

    /**
     * The File.
     */
    private File file;
    /**
     * The Extension.
     */
    private String extension;
    /**
     * The Root path.
     */
    private String rootPath;
    /**
     * The Policy.
     */
    private Policy policy;
    /**
     * The Result.
     */
    private ApplicationAssessmentResult result;

    private List<String> excludeDomains;

    /**
     * Instantiates a new Dependency check task.
     *
     * @param file      the file
     * @param extension the extension
     * @param rootPath  the root path
     * @param policy    the policy
     * @param result    the result
     */
    public DependencyCheckTask(File file, String extension, String rootPath, Policy policy, ApplicationAssessmentResult result, List<String> excludeDomains) {
        super(file.getAbsoluteFile() + " Dependency Check Task");
        this.file = file;
        this.extension = extension;
        this.rootPath = rootPath;
        this.policy = policy;
        this.result = result;
        this.excludeDomains = excludeDomains;

        // log.debug("[{}] Dependency Check Task has been started.", file.getAbsolutePath());
    }

    /**
     * Read class to String
     *
     * @param classFile
     * @return
     */
    /*
    private static synchronized String readClassOld(String classFile) throws Exception {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            System.setOut(new PrintStream(baos));

            String[] args = new String[]{classFile};
            Main.main(args);

            return baos.toString("UTF-8");
        } finally {
            System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
        }
    }
    /*/
    private String readClass(String classFile) {
        // String classString = null;
        //
        // try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
        //     RoRoOutputSinkFactory sinkFactory = new RoRoOutputSinkFactory(new PrintStream(baos));
        //     CfrDriver driver = new CfrDriver.Builder().withOutputSink(sinkFactory).build();
        //     driver.analyse(Collections.singletonList(classFile));
        //     classString = baos.toString("UTF-8");
        // } catch (Exception e) {
        //     log.warn("Unhandled exception occurred while decompile [{}] class. Reason : [{}]", classFile, e.getMessage());
        // }
        //
        // return classString;

        RoRoOutputSinkFactory2 sinkFactory = new RoRoOutputSinkFactory2();
        CfrDriver driver = new CfrDriver.Builder().withOutputSink(sinkFactory).build();
        driver.analyse(Collections.singletonList(classFile));

        return sinkFactory.getResult();
    }
    //*/

    /**
     * Task run.
     */
    @Override
    protected void taskRun() {
        try {
            if ("class".equals(extension)) {
                if (result.getApplicationType() == null || !result.getApplicationType().contains("Web")) {
                    String type = "Java Application";
                    try {
                        if (file.getParentFile().getAbsolutePath().contains("classes") &&
                                file.getParentFile().getAbsolutePath().contains("-INF")) {
                            // web.xml이 존재하지 않는 Web Application
                            type = "Java Web Application";
                        }
                    } catch (Exception e) {
                        // ignore
                    } finally {
                        result.setApplicationType(type);
                    }
                }

                check(readClass(file.getAbsolutePath()), extension);
            } else {
                if (result.getApplicationType() == null) {
                    if ("java".equals(extension)) {
                        result.setApplicationType("Java Application");
                    } else if ("c".equals(extension) || "cpp".equals(extension)) {
                        result.setApplicationType("C Application");
                    } else if ("pc".equals(extension)) {
                        result.setApplicationType("Proc Application");
                    } else if ("py".equals(extension)) {
                        result.setApplicationType("Python Application");
                    }
                }

                //*
                String fileContents = FileUtil.getFileContents(file);

                if (StringUtils.isNotEmpty(fileContents)) {
                    check(fileContents, extension);
                }
                /*/
                CharsetDetector detector = new CharsetDetector();

                String fileContents = null;
                try (InputStream input = new FileInputStream(file)) {
                    byte[] data = IOUtils.toByteArray(input, file.length());
                    detector.setDeclaredEncoding("UTF-8");
                    detector.setText(data);
                    detector.detectAll();

                    for (com.ibm.icu.text.CharsetMatch m : detector.detectAll()) {
                        if (m.getName().toLowerCase().equals("euc-kr")) {
                            fileContents = m.getString();
                            break;
                        }
                    }

                    if (fileContents == null) {
                        fileContents = detector.detect().getString();
                    }

                    try (BufferedReader buffer = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(fileContents.getBytes())))) {
                        check(buffer, extension);
                    }
                }
                //*/
            }

            // log.debug("[{}] Dependency Check Task has been finished.", file.getAbsolutePath());
        } catch (Exception e) {
            log.error("Exception occurred while execute DependencyCheckTask.", e);
        }
    }

    /**
     * @param contents
     * @param extension
     * @throws IOException
     */
    private void check(String contents, String extension) throws IOException {
        String fileName = file.getAbsolutePath().substring(rootPath.length() + 1).replaceAll("(_roro(\\d+)?)", "!");

        Pattern apiPattern = policy.getApiPattern();
        Pattern servletPattern = policy.getServletPattern();
        Pattern jdbcPattern = policy.getJdbcPattern();
        Pattern jndiPattern = policy.getJndiPattern();
        Pattern notJndiPattern = policy.getNotJndiPattern();
        Pattern ipPattern = policy.getIpPattern();
        Pattern notIpPattern = policy.getNotIpPattern();
        Pattern etcPattern = policy.getEtcPattern();
        Pattern ipPortPattern = policy.getIpPortPattern();
        Pattern httpPattern = policy.getHttpPattern();
        Pattern localhostPortPattern = policy.getLocalhostPortPattern();

        Matcher match;
        Matcher portMatcher;
        boolean isFound;

        String lineStr;
        int lineNum = 1;
        ApplicationAssessmentResult.Check check = new ApplicationAssessmentResult.Check();
        ApplicationAssessmentResult.DataSource dataSource;
        ApplicationAssessmentResult.Check.Point point;
        ApplicationAssessmentResult.DataSource.JdbcProperty jdbcProperty;
        ApplicationAssessmentResult.DataSource.Use use;

        ApplicationAssessmentResult.HardCodedIp hardCodedIp;
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(contents.getBytes())))) {
            while ((lineStr = buffer.readLine()) != null) {
                // API Usage 여부
                match = apiPattern.matcher(lineStr);
                if (match.matches()) {
                    point = new ApplicationAssessmentResult.Check.Point();
                    point.setLine(lineNum);
                    point.setValue(lineStr.trim());
                    check.getApiUsages().add(point);
                }

                // Servlet 상속 여부
                if ("class".equals(extension) || "java".equals(extension)) {
                    match = servletPattern.matcher(lineStr);
                    if (match.matches()) {
                        point = new ApplicationAssessmentResult.Check.Point();
                        point.setLine(lineNum);
                        point.setValue(lineStr.trim());
                        check.getServletExtends().add(point);
                    }
                }

                // jdbc 사용 여부
                if (!"js".equals(extension)) {
                    match = jdbcPattern.matcher(lineStr);
                    if (match.matches()) {
                        if (!lineStr.contains("<jdbc:") && !lineStr.contains("</jdbc:") && !lineStr.contains("-jdbc:")) {
                            String type = Domain1109.JDBC.name();
                            String value = getJdbcUrl(lineStr.replaceAll("</pre>", "")
                                    .replaceAll("</strong>", "")
                                    .replaceAll("<br/>", "")
                                    .replaceAll("<br>", "").trim());

                            dataSource = result.getDataSourceList().stream().filter(ds -> type.equals(ds.getType()) && value.equals(ds.getValue())).findAny().orElse(null);

                            if (dataSource == null) {
                                dataSource = new ApplicationAssessmentResult.DataSource();
                                dataSource.setType(type);
                                dataSource.setValue(value);
                                dataSource.setJdbcProperties(getJdbcProperties(value));

                                result.getDataSourceList().add(dataSource);
                            }

                            use = new ApplicationAssessmentResult.DataSource.Use();
                            use.setFileName(fileName);
                            use.setLine(lineNum);
                            use.setValue(lineStr.trim());

                            dataSource.getUses().add(use);
                        }
                    }

                    // jndi 사용 여부
                    match = jndiPattern.matcher(lineStr);
                    if (match.matches()) {
                        match = notJndiPattern.matcher(lineStr);

                        if (!match.matches()) {
                            String type = Domain1109.JNDI.name();
                            String value = getJndiName(lineStr.trim(), contents);

                            dataSource = result.getDataSourceList().stream().filter(ds -> type.equals(ds.getType()) && value.equals(ds.getValue())).findAny().orElse(null);

                            if (dataSource == null) {
                                dataSource = new ApplicationAssessmentResult.DataSource();
                                dataSource.setType(type);
                                dataSource.setValue(value);

                                result.getDataSourceList().add(dataSource);
                            }

                            use = new ApplicationAssessmentResult.DataSource.Use();
                            use.setFileName(fileName);
                            use.setLine(lineNum);
                            use.setValue(lineStr.trim());

                            dataSource.getUses().add(use);
                        }
                    }
                }

                // HTTP Pattern 사용 여부
                isFound = false;
                match = httpPattern.matcher(lineStr);
                while (match.find()) {
                    for (int i = 0; i < match.groupCount(); i++) {
                        if (match.group(i) != null && match.group(i).contains("://")) {
                            hardCodedIp = new ApplicationAssessmentResult.HardCodedIp();

                            String url = match.group(i);
                            if (url.toLowerCase().startsWith("https")) {
                                hardCodedIp.setProtocol("HTTPS");
                                hardCodedIp.setPort(443);
                            } else if (url.toLowerCase().startsWith("wss")) {
                                hardCodedIp.setProtocol("WSS");
                                hardCodedIp.setPort(443);
                            } else if (url.toLowerCase().startsWith("http")) {
                                hardCodedIp.setProtocol("HTTP");
                                hardCodedIp.setPort(80);
                            } else if (url.toLowerCase().startsWith("ws")) {
                                hardCodedIp.setProtocol("WS");
                                hardCodedIp.setPort(80);
                            }

                            url = url.substring(url.indexOf("://") + 3);

                            String[] ipPort = url.split(":");

                            boolean isExcluded = false;
                            for (String excludeDomain : excludeDomains) {
                                if (ipPort[0].contains(excludeDomain)) {
                                    isExcluded = true;
                                    break;
                                }
                            }

                            if (isExcluded) {
                                continue;
                            }

                            hardCodedIp.setFileName(fileName);
                            hardCodedIp.setLineNum(lineNum);
                            hardCodedIp.setIpAddress(ipPort[0]);
                            if (ipPort.length > 1 && StringUtils.isNotEmpty(ipPort[1]) && NumberUtils.isDigits(ipPort[1])) {
                                hardCodedIp.setPort(Integer.parseInt(ipPort[1]));
                            }

                            if (!isFound) {
                                point = new ApplicationAssessmentResult.Check.Point();
                                point.setLine(lineNum);
                                point.setValue(lineStr.trim());
                                check.getIpPatterns().add(point);

                                isFound = true;
                            }

                            result.getHardCodedIpList().add(hardCodedIp);
                        }
                    }
                }

                // IP Pattern 사용 여부
                isFound = false;
                match = ipPattern.matcher(lineStr);
                if (match.matches()) {
                    match = notIpPattern.matcher(lineStr);

                    if (!match.matches()) {
                        portMatcher = ipPortPattern.matcher(lineStr);
                        while (portMatcher.find()) {
                            for (int i = 0; i < portMatcher.groupCount(); i++) {
                                String connection = portMatcher.group(i);

                                if (StringUtils.isNotEmpty(connection)) {
                                    String[] ipPort = connection.split(":");
                                    if (StringUtils.isNotEmpty(ipPort[0])) {
                                        boolean isExcluded = false;
                                        for (String excludeDomain : excludeDomains) {
                                            if (ipPort[0].contains(excludeDomain)) {
                                                isExcluded = true;
                                                break;
                                            }
                                        }

                                        if (isExcluded) {
                                            continue;
                                        }

                                        hardCodedIp = new ApplicationAssessmentResult.HardCodedIp();
                                        hardCodedIp.setFileName(fileName);
                                        hardCodedIp.setLineNum(lineNum);
                                        hardCodedIp.setIpAddress(ipPort[0]);
                                        if (ipPort.length > 1 && StringUtils.isNotEmpty(ipPort[1]) && NumberUtils.isDigits(ipPort[1])) {
                                            hardCodedIp.setPort(Integer.parseInt(ipPort[1]));
                                            hardCodedIp.setProtocol(WellKnownPortUtil.getType("TCP", Integer.parseInt(ipPort[1]), null));
                                        }

                                        if (!isFound) {
                                            point = new ApplicationAssessmentResult.Check.Point();
                                            point.setLine(lineNum);
                                            point.setValue(lineStr.trim());
                                            check.getIpPatterns().add(point);

                                            isFound = true;
                                        }

                                        result.getHardCodedIpList().add(hardCodedIp);
                                    }
                                }
                            }
                        }
                    }
                }

                if (lineStr.contains("localhost") && !excludeDomains.contains("localhost")) {
                    point = new ApplicationAssessmentResult.Check.Point();
                    point.setLine(lineNum);
                    point.setValue(lineStr.trim());
                    check.getIpPatterns().add(point);

                    portMatcher = localhostPortPattern.matcher(lineStr);
                    while (portMatcher.find()) {
                        for (int i = 0; i < portMatcher.groupCount(); i++) {
                            String connection = portMatcher.group(i);

                            if (StringUtils.isNotEmpty(connection)) {
                                String[] ipPort = connection.split(":");

                                if (StringUtils.isNotEmpty(ipPort[0])) {
                                    hardCodedIp = new ApplicationAssessmentResult.HardCodedIp();
                                    hardCodedIp.setFileName(fileName);
                                    hardCodedIp.setLineNum(lineNum);
                                    hardCodedIp.setIpAddress(ipPort[0]);
                                    if (ipPort.length > 1 && StringUtils.isNotEmpty(ipPort[1]) && NumberUtils.isDigits(ipPort[1])) {
                                        hardCodedIp.setPort(Integer.parseInt(ipPort[1]));
                                        hardCodedIp.setProtocol(WellKnownPortUtil.getType("TCP", Integer.parseInt(ipPort[1]), null));
                                    }

                                    result.getHardCodedIpList().add(hardCodedIp);
                                }
                            }
                        }
                    }
                }

                // 사용자 정의 의존성 사용 여부
                if (etcPattern != null) {
                    match = etcPattern.matcher(lineStr);
                    if (match.matches()) {
                        point = new ApplicationAssessmentResult.Check.Point();
                        point.setLine(lineNum);
                        point.setValue(lineStr.trim());
                        check.getCustomPatterns().add(point);
                    }
                }

                lineNum++;
            }
        }

        if (check.getApiUsages().size() > 0 || check.getServletExtends().size() > 0 || check.getIpPatterns().size() > 0 || check.getCustomPatterns().size() > 0) {
            check.setFileName(fileName);
            result.getCheckList().add(check);
        }
    }

    /**
     * @param name
     * @param contents
     * @return
     * @throws IOException
     */
    private String getJndiName(String name, String contents) throws IOException {
        try {
            if (name.contains("property") && name.contains("name") && name.contains("value")) {
                // <property name="jndiName" value="jdbc/roroDS"/>
                name = name.substring(name.indexOf("value") + 6, name.lastIndexOf("/>"));
                name = name.replaceAll("=", "");
                name = name.replaceAll("\"", "");
                name = name.replaceAll("'", "").trim();
            } else if (name.contains("name") && name.contains("type") && name.contains("lookup")) {
                // @Resource(name="jdbc/roroDS", type=javax.sql.DataSource.class, lookup="jdbc/roroDS")
                name = name.substring(name.indexOf("lookup") + 7, name.lastIndexOf("\""));
                name = name.replaceAll("=", "");
                name = name.replaceAll("\"", "");
                name = name.replaceAll("'", "").trim();
            } else {
                long cnt = name.chars().filter(ch -> ch == '"').count();

                if (cnt != 2) {
                    name = name.substring(name.lastIndexOf("(") + 1, name.lastIndexOf(")"));
                    cnt = name.chars().filter(ch -> ch == '"').count();
                }

                if (cnt == 2) {
                    name = name.substring(name.indexOf("\"") + 1, name.lastIndexOf("\""));
                    name = name.replaceAll("java:comp/env/", "");
                    name = name.replaceAll("java:", "");
                } else {
                    // 변수로 지정된 JNDI Name인 경우 바인딩 된 값을 조회
                    if (StringUtils.isNotEmpty(name)) {
                        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(contents.getBytes())))) {
                            Pattern pattern = Pattern.compile("(.*String.*" + name + ".*=.*)");

                            String lineStr;
                            Matcher match;
                            while ((lineStr = buffer.readLine()) != null) {
                                match = pattern.matcher(lineStr);
                                if (match.matches()) {
                                    name = lineStr.substring(lineStr.indexOf("\"") + 1, lineStr.lastIndexOf("\""));
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // ignore
            log.warn("Unhandled exception occurred while get jndi name from [{}] : {}", name, e.getMessage());
        }

        return name;
    }

    /**
     * @param url
     * @return
     */
    private String getJdbcUrl(String url) {
        try {
            long cnt = url.chars().filter(ch -> ch == '"').count();

            if (cnt == 2) {
                String tempUrl = url.substring(url.indexOf("\"") + 1, url.lastIndexOf("\""));

                if (tempUrl.startsWith("jdbc:")) {
                    url = tempUrl;
                } else {
                    url = url.substring(url.indexOf("jdbc:"));
                    url = url.substring(0, url.indexOf("\"") > 0 ? url.indexOf("\"") : url.length());
                    url = url.substring(0, url.indexOf("<") > 0 ? url.indexOf("<") : url.length());
                }
            } else {
                url = url.substring(url.indexOf("jdbc:"));
                url = url.substring(0, url.indexOf("\"") > 0 ? url.indexOf("\"") : url.length());
            }
        } catch (Exception e) {
            // ignore
            log.warn("Unhandled exception occurred while get jdbc url from [{}]: {}", url, e.getMessage());
        }

        return url;
    }

    /**
     * @param jdbcUrl
     * @return
     */
    public List<JdbcProperty> getJdbcProperties(String jdbcUrl) {
        List<JdbcProperty> jdbcProperties = new ArrayList<>();

        try {
            List<JdbcInfo> parse = JdbcURLParser.parse(StringUtils.defaultString(jdbcUrl));

            if (CollectionUtils.isNotEmpty(parse)) {
                for (JdbcInfo jdbcInfo : parse) {
                    jdbcProperties.add(JdbcProperty.builder()
                            .type(jdbcInfo.getType())
                            .database(jdbcInfo.getDatabase())
                            .host(jdbcInfo.getHost())
                            .port(jdbcInfo.getPort())
                            .build());
                }
            }
        } catch (Exception e) {
            // ignore
            log.warn("Unhandled exception occurred while parse jdbc property : {}", e.getMessage());
        }

        return jdbcProperties;
    }
}
//end of DependencyCheckTask.java