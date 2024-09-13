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
 * SangCheon Park   Nov 11, 2021		    First Draft.
 */
package io.playce.roro.app.asmt.java;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.ReadContext;
import io.playce.roro.app.asmt.AbstractApplicationAssessment;
import io.playce.roro.app.asmt.ApplicationScanConfig;
import io.playce.roro.app.asmt.java.analyzer.ApplicationAnalyzer;
import io.playce.roro.app.asmt.java.policy.Policy;
import io.playce.roro.app.asmt.result.ApplicationAssessmentResult;
import io.playce.roro.app.asmt.support.ApplicationAssessmentHelper;
import io.playce.roro.app.asmt.util.ApplicationFileUtil;
import io.playce.roro.common.cancel.InventoryProcessCancelInfo;
import io.playce.roro.common.code.Domain1013;
import io.playce.roro.common.dto.assessment.ApplicationDto;
import io.playce.roro.common.dto.assessment.InventoryProcessDto;
import io.playce.roro.common.exception.InsufficientException;
import io.playce.roro.common.property.CommonProperties;
import io.playce.roro.common.util.CommandUtil;
import io.playce.roro.common.util.SSHUtil;
import io.playce.roro.common.util.ThreadLocalUtils;
import io.playce.roro.common.util.WinRmUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.XML;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.playce.roro.common.util.ThreadLocalUtils.APP_SCAN_ERROR;
import static io.playce.roro.common.util.support.DistinctByKey.distinctByKey;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
@Slf4j
@Component("JavaAssessment")
@RequiredArgsConstructor
public class JavaAssessment extends AbstractApplicationAssessment {

    private final ApplicationScanConfig applicationScanConfig;

    @Override
    public ApplicationAssessmentResult assessment(InventoryProcessDto inventoryProcess) throws Exception {
        ApplicationDto application = (ApplicationDto) inventoryProcess;

        ApplicationAssessmentResult result = new ApplicationAssessmentResult();

        /**
         * 1. Application 파일 다운로드
         *    - analyze_application_uri 값이 있는 경우 해당 URI로부터 파일을 다운로드한다.
         *      : 다운로드 경로는 AssessmentManager.getWorkDir()/application/{application_id} 로 하며, 동일 파일이 있는 경우 Overwrite 한다.
         *      : 확장자는 zip, ear, war, jar만 가능하다.
         *
         * 2. Extract 파일
         *    - AssessmentManager.getWorkDir()/assessment/{assessment_id}/{file_name} 에 파일을 압축 해제한다.
         *    - 하나의 폴더로 감싸여 있는 압축일 경우 폴더는 건너뛴다.
         *    - 압축 해제 시 File Summary(각 파일 타입별 카운트)를 계산한다. (추후 EE Modules 분석 시, anlz_target_lib_list 대상 압축 해제를 추가로 함으로써 카운트 정보가 늘어날 수 있음)
         *    - 압축 해제 시 Application Type을 추정한다.
         *      : META-INF/application.xml 파일이 있는 경우 Java Enterprise Application
         *      : WEB-INF/web.xml 파일이 있거나, WEB-INF/classes/ 에 클래스 파일들이 있으면 Java Web Application
         *      : META-INF/ejb-jar.xml 파일이 있는 경우 Java EJB Application
         *      : *.c 파일이 있으면 C Application
         *      : *.pc 파일이 있으면 Proc Application
         *      : *.py 파일이 있으면 Python Application
         *
         * 3. Discovery
         *    - Descriptor File 목록에서 META-INF/application.xml 파일을 파싱하여 eeModules 정보를 추가하고, EJB, Java, Web 아카이브 파일을 압축 해제한다.
         *    - 추가 분석 라이브러리 파일이 존재하는 경우 해당 jar 파일을 압축 해제한다.
         *    - 디렉토리를 순차적으로 탐색하면서 Assessment를 수행한다.
         *
         * 4. 디렉토리 삭제
         *    - Assessment를 위해 압축 해제한 디렉토리를 삭제한다.
         */
        try {
            initialize(CommonProperties.getWorkDir(), application.getApplicationId(), inventoryProcess.getInventoryProcessId(), result);

            result.setAnalysisLibList(application.getAnalysisLibList());

            if (InventoryProcessCancelInfo.hasCancelRequest(inventoryProcess.getInventoryProcessId())) {
                return result;
            }
            download(application, result);

            if (result.getApplicationFile() != null) {
                // TODO 'id -u -n', 'id -g -n' 을 사용하여 chown 수행
                ApplicationFileUtil.chmod(result.getApplicationDir(), "777");
                ApplicationFileUtil.chmod(result.getApplicationFile(), "777");
            }

            if (result.getApplicationFile() == null || !new File(result.getApplicationFile()).exists()) {
                //throw new Exception("Application(" + result.getApplicationFile() + ") does not exist for assessment.");
                throw new InsufficientException("Application(" + result.getApplicationFile() + ") does not exist for assessment.");
            }

            if (getApplicationSize(application, result) == 0L) {
                File f = new File(result.getApplicationFile());

                if (f.exists()) {
                    // 디렉토리 또는 파일 사이즈를 가져온다.
                    result.setApplicationSize(FileUtils.sizeOf(f));
                }
            }

            if (FileUtils.sizeOf(new File(result.getApplicationFile())) <= 0L) {
                ThreadLocalUtils.add(APP_SCAN_ERROR, "Application is empty. Please check the application does exist and has permission to read.");
            } else {
                if (result.getApplicationFile().endsWith(".ear")) {
                    result.setApplicationType("Java Enterprise Application");
                } else if (result.getApplicationFile().endsWith(".war")) {
                    result.setApplicationType("Java Web Application");
                }

                if (StringUtils.isEmpty(result.getFileName())) {
                    result.setFileName(new File(result.getApplicationFile()).getName());
                }

                if (InventoryProcessCancelInfo.hasCancelRequest(inventoryProcess.getInventoryProcessId())) {
                    return result;
                }
                extract(result);

                if (InventoryProcessCancelInfo.hasCancelRequest(inventoryProcess.getInventoryProcessId())) {
                    return result;
                }
                discover(result, inventoryProcess);

                // HardCodedIP 중복 제거 및 Filename, IP, Port로 정렬
                result.setHardCodedIpList(result.getHardCodedIpList().stream()
                        .filter(distinctByKey(h -> h.getFileName() + ":" + h.getIpAddress() + ":" + h.getPort()))
                        .sorted()
                        .collect(Collectors.toList()));

                // HardCodedIP에 프로토콜 정보가 없는 경우 Datasource 목록에서 동일한 IP, Port 정보가 있는지 확인
                Map<String, String> databaseByHostAndPort = new HashMap<>();
                for (ApplicationAssessmentResult.DataSource ds : result.getDataSourceList()) {
                    if (ds.getJdbcProperties() != null) {
                        for (ApplicationAssessmentResult.DataSource.JdbcProperty jp : ds.getJdbcProperties()) {
                            databaseByHostAndPort.put(jp.getHost() + ":" + jp.getPort(), jp.getType());
                        }
                    }
                }

                for (ApplicationAssessmentResult.HardCodedIp hardCodedIp : result.getHardCodedIpList()) {
                    if (StringUtils.isEmpty(hardCodedIp.getProtocol()) && hardCodedIp.getPort() != null) {
                        String database = databaseByHostAndPort.get(hardCodedIp.getIpAddress() + ":" + hardCodedIp.getPort());
                        if (database != null) {
                            try {
                                hardCodedIp.setProtocol(Domain1013.valueOf(database).enname());
                            } catch (Exception e) {
                                hardCodedIp.setProtocol(database);
                            }
                        }
                    }
                }

                // if (StringUtils.isNotEmpty(application.getSourceLocationUri()) && result.getAssessmentDir().equals(result.getApplicationDir())) {
                //     // 폴더 형태로 서버에 존재하는 애플리케이션은 서버에서 조회한다.
                //     result.setThirdPartySolutions(ThirdPartySolutionUtil.detectThirdPartySolutionsFromApplication(application.getTargetHost(), application.getWindowsYn().equals("Y") ? true : false, application.getSourceLocationUri()));
                // } else {
                //     // 압축 파일 형태의 Application 과 업로드 된 Application은 RoRo 서버 내의 파일로 조회한다.
                //     result.setThirdPartySolutions(ThirdPartySolutionUtil.detectThirdPartySolutionsFromApplication(application.getTargetHost(), new File(result.getAssessmentDir()), result.getAssessmentDir()));
                // }
            }
        } catch (Exception e) {
            log.error("Application assessment error.", e);

            if (e instanceof UnknownHostException) {
                throw new Exception("UnknownHostException occurred. Please add '127.0.0.1 " + e.getMessage() + "' to your hosts file.");
            } else if (e instanceof InterruptedException) {
                cancel(application.getTargetHost().getIpAddress(), application.getTargetHost().getPort(), result.getApplicationDir());
                throw e;
            } else {
                throw e;
            }
        }

        return result;
    }

    @Override
    public void cancel(String ipAddress, Integer port, String applicationDir) {
        DefaultExecutor executor = null;
        PumpStreamHandler streamHandler = null;
        CommandLine cl = null;

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            executor = new DefaultExecutor();
            streamHandler = new PumpStreamHandler(baos);
            executor.setStreamHandler(streamHandler);

            cl = CommandUtil.getCommandLine(CommandUtil.findCommand("sudo"),
                    CommandUtil.findCommand("sh"),
                    ApplicationAssessmentHelper.getAppScanCancelFile().getAbsolutePath(),
                    ipAddress,
                    Integer.toString(port),
                    applicationDir);

            log.debug("Application scan will be cancel using [{}]", cl.toString());

            int exitCode = executor.execute(cl);

            if (exitCode == 0) {
                log.debug("Application({}) scan has been cancelled.", applicationDir);
            } else {
                log.debug("Application({}) scan cancel failed.", applicationDir);
            }
        } catch (Exception e) {
            log.error("Application({}) scan cancel failed.", applicationDir, e);
        }
    }

    @Override
    protected void discover(ApplicationAssessmentResult result, InventoryProcessDto inventoryProcess) throws Exception {
        ApplicationAnalyzer analyzer = null;

        try {
            ApplicationAssessmentResult.EEModule eeModule = null;
            for (ApplicationAssessmentResult.File f : result.getDescriptorFiles()) {
                // Descriptor File 목록에서 META-INF/application.xml 파일을 파싱하여 eeModules 정보를 추가하고, EJB, Java, Web 아카이브 파일을 압축 해제한다.
                if (f.getFile().equals("application.xml")) {
                    result.setApplicationType("Java Enterprise Application");
                    eeModule = applicationParse(f.getContents());

                    log.debug("application.xml parse result : {}", eeModule);

                    result.getEeModules().add(eeModule);

                    // EJB 아카이브 파일을 압축 해제한다.
                    if (eeModule.getEjb() != null) {
                        for (String ejbPath : eeModule.getEjb()) {
                            File ejb = new File(FilenameUtils.separatorsToSystem(result.getAssessmentDir() +
                                    File.separator +
                                    f.getLocation().replaceAll("META-INF", "") +
                                    File.separator +
                                    ejbPath));

                            if (ejb.exists() && !ejb.isDirectory()) {
                                int idx = 1;
                                String postfix = "_roro";
                                while (true) {
                                    if (new File(ejb.getAbsolutePath(), postfix).exists()) {
                                        postfix += idx++;
                                        continue;
                                    }

                                    ApplicationFileUtil.unzip(ejb.getAbsolutePath(), ejb.getAbsolutePath() + postfix, result);
                                    break;
                                }
                            }
                        }
                    }

                    // Java 아카이브 파일을 압축 해제한다.
                    if (eeModule.getJava() != null) {
                        for (String javaPath : eeModule.getJava()) {
                            File java = new File(FilenameUtils.separatorsToSystem(result.getAssessmentDir() +
                                    File.separator +
                                    f.getLocation().replaceAll("META-INF", "") +
                                    File.separator +
                                    javaPath));

                            if (java.exists() && !java.isDirectory()) {
                                int idx = 1;
                                String postfix = "_roro";
                                while (true) {
                                    if (new File(java.getAbsolutePath(), postfix).exists()) {
                                        postfix += idx++;
                                        continue;
                                    }

                                    ApplicationFileUtil.unzip(java.getAbsolutePath(), java.getAbsolutePath() + postfix, result);
                                    break;
                                }
                            }
                        }
                    }

                    // Web 아카이브 파일을 압축 해제한다.
                    if (eeModule.getWeb() != null) {
                        for (ApplicationAssessmentResult.EEModule.Web web : eeModule.getWeb()) {
                            File webFile = new File(FilenameUtils.separatorsToSystem(result.getAssessmentDir() +
                                    File.separator +
                                    f.getLocation().replaceAll("META-INF", "") +
                                    File.separator +
                                    web.getWebUri()));

                            if (webFile.exists() && !webFile.isDirectory()) {
                                int idx = 1;
                                String postfix = "_roro";
                                while (true) {
                                    if (new File(webFile.getAbsolutePath(), postfix).exists()) {
                                        postfix += idx++;
                                        continue;
                                    }

                                    ApplicationFileUtil.unzip(webFile.getAbsolutePath(), webFile.getAbsolutePath() + postfix, result);
                                    break;
                                }
                            }
                        }
                    }
                } else if (f.getFile().equals("web.xml")) {
                    if (result.getApplicationType() == null) {
                        result.setApplicationType("Java Web Application");
                    }
                } else if (f.getFile().equals("ejb-jar.xml")) {
                    if (result.getApplicationType() == null) {
                        result.setApplicationType("Java EJB Application");
                    }
                }
            }

            // 추가 분석 라이브러리 파일이 존재하는 경우 해당 jar 파일을 압축 해제한다.
            for (String libPath : result.getAnalysisLibPathList()) {
                File lib = new File(libPath);

                if (lib.exists() && !lib.isDirectory()) {
                    int idx = 1;
                    String postfix = "_roro";
                    while (true) {
                        if (new File(lib.getAbsolutePath(), postfix).exists()) {
                            postfix += idx++;
                            continue;
                        }

                        ApplicationFileUtil.unzip(lib.getAbsolutePath(), lib.getAbsolutePath() + postfix, result);
                        break;
                    }
                }
            }

            ApplicationDto application = (ApplicationDto) inventoryProcess;
            Policy policy = new Policy(application.getAnalysisStringList());

            analyzer = new ApplicationAnalyzer(policy, result, result.getAssessmentDir(), applicationScanConfig);
            analyzer.analyze(result.getAssessmentDir());
        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                if (analyzer != null) {
                    analyzer.cancel();
                }
            }

            throw e;
        }
    }

    /**
     * Application parse application assessment result . ee module.
     *
     * @param contents the contents
     *
     * @return the application assessment result . ee module
     *
     * @throws Exception the exception
     */
    private ApplicationAssessmentResult.EEModule applicationParse(String contents) {
        ApplicationAssessmentResult.EEModule eeModule = new ApplicationAssessmentResult.EEModule();

        String xmlToJson = XML.toJSONObject(contents).toString();
        ReadContext ctx = JsonPath.parse(xmlToJson);

        try {
            String displayName = ctx.read("$.application.display-name", String.class);
            if (displayName != null) {
                eeModule.setDisplayName(displayName);
            }
        } catch (PathNotFoundException e) {
            log.warn("PathNotFoundException occurred while parse an application.xml. Detail : [{}]", e.getMessage());
            // ThreadLocalUtils.add(APP_SCAN_ERROR, "Exception occurred while parse an application.xml. Detail : [" + e.getMessage() + "]");
        }

        try {
            String description = ctx.read("$.application.description", String.class);
            if (description != null) {
                eeModule.setDescription(description);
            }
        } catch (PathNotFoundException e) {
            log.warn("PathNotFoundException occurred while parse an application.xml. Detail : [{}]", e.getMessage());
            //ThreadLocalUtils.add(APP_SCAN_ERROR, "Exception occurred while parse an application.xml. Detail : [" + e.getMessage() + "]");
        }

        /**
         * <application> 내에 <module> 엘리먼트가 1개만 존재하는 경우
         * $.application.module.* 로 하면 조회가 되지 않음.
         */
        try {
            List<String> ejbs = ctx.read("$.application.module.*.ejb", List.class);

            if (ejbs == null || ejbs.size() == 0) {
                String ejb = ctx.read("$.application.module.ejb", String.class);

                if (StringUtils.isNotEmpty(ejb)) {
                    eeModule.getEjb().add(ejb);
                }
            } else {
                for (String ejb : ejbs) {
                    eeModule.getEjb().add(ejb);
                }
            }
        } catch (PathNotFoundException e) {
            log.warn("PathNotFoundException occurred while parse an application.xml. Detail : [{}]", e.getMessage());
            //ThreadLocalUtils.add(APP_SCAN_ERROR, "Exception occurred while parse an application.xml. Detail : [" + e.getMessage() + "]");
        }

        try {
            List<String> javas = ctx.read("$.application.module.*.java", List.class);
            if (javas == null || javas.size() == 0) {
                String java = ctx.read("$.application.module.java", String.class);

                if (StringUtils.isNotEmpty(java)) {
                    eeModule.getJava().add(java);
                }
            } else {
                for (String java : javas) {
                    eeModule.getJava().add(java);
                }
            }
        } catch (PathNotFoundException e) {
            log.warn("PathNotFoundException occurred while parse an application.xml. Detail : [{}]", e.getMessage());
            //ThreadLocalUtils.add(APP_SCAN_ERROR, "Exception occurred while parse an application.xml. Detail : [" + e.getMessage() + "]");
        }

        try {
            List<Map<String, String>> webMapList = ctx.read("$.application.module.*.web", List.class);
            if (webMapList == null || webMapList.size() == 0) {
                Map<String, String> webMap = ctx.read("$.application.module.web", Map.class);

                if (webMap != null) {
                    ApplicationAssessmentResult.EEModule.Web web = new ApplicationAssessmentResult.EEModule.Web();
                    web.setWebUri(webMap.get("web-uri"));
                    web.setContextRoot(webMap.get("context-root"));
                    eeModule.getWeb().add(web);
                }
            } else {
                for (Map<String, String> webMap : webMapList) {
                    ApplicationAssessmentResult.EEModule.Web web = new ApplicationAssessmentResult.EEModule.Web();
                    web.setWebUri(webMap.get("web-uri"));
                    web.setContextRoot(webMap.get("context-root"));
                    eeModule.getWeb().add(web);
                }
            }
        } catch (PathNotFoundException e) {
            log.warn("PathNotFoundException occurred while parse an application.xml. Detail : [{}]", e.getMessage());
            //ThreadLocalUtils.add(APP_SCAN_ERROR, "Exception occurred while parse an application.xml. Detail : [" + e.getMessage() + "]");
        }

        if (eeModule.getEjb().size() == 0 && eeModule.getJava().size() == 0 && eeModule.getWeb().size() == 0) {
            ThreadLocalUtils.add(APP_SCAN_ERROR, "No modules exists in application.xml.");
        }

        /*
        ApplicationAssessmentResult.EEModule eeModule = new ApplicationAssessmentResult.EEModule();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder;
        Document doc = null;

        InputSource is = new InputSource(new StringReader(contents));
        builder = factory.newDocumentBuilder();
        doc = builder.parse(is);
        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();

        XPathExpression expr = null;
        Node node = null;
        NodeList nodeList = null;

        expr = xpath.compile("//application/display-name");
        node = (Node) expr.evaluate(doc, XPathConstants.NODE);

        if (node != null) {
            eeModule.setDisplayName(node.getTextContent());
        }

        expr = xpath.compile("//application/description");
        node = (Node) expr.evaluate(doc, XPathConstants.NODE);

        if (node != null) {
            eeModule.setDescription(node.getTextContent());
        }

        expr = xpath.compile("//application/module/ejb");
        nodeList = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

        if (nodeList != null) {
            for (int i = 0; i < nodeList.getLength(); i++) {
                NodeList child = nodeList.item(i).getChildNodes();
                for (int j = 0; j < child.getLength(); j++) {
                    Node n = child.item(j);
                    eeModule.getEjb().add(n.getTextContent());
                }
            }
        }

        expr = xpath.compile("//application/module/java");
        nodeList = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

        if (nodeList != null) {
            for (int i = 0; i < nodeList.getLength(); i++) {
                NodeList child = nodeList.item(i).getChildNodes();
                for (int j = 0; j < child.getLength(); j++) {
                    Node n = child.item(j);
                    eeModule.getJava().add(n.getTextContent());
                }
            }
        }

        expr = xpath.compile("//application/module/web");
        nodeList = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

        if (nodeList != null) {
            for (int i = 0; i < nodeList.getLength(); i++) {
                NodeList child = nodeList.item(i).getChildNodes();

                ApplicationAssessmentResult.EEModule.Web web = new ApplicationAssessmentResult.EEModule.Web();
                for (int j = 0; j < child.getLength(); j++) {
                    Node n = child.item(j);

                    if ("web-uri".equals(n.getNodeName())) {
                        web.setWebUri(n.getTextContent());
                    } else if ("context-root".equals(n.getNodeName())) {
                        web.setContextRoot(n.getTextContent());
                    }
                }

                if (web.getWebUri() != null || web.getContextRoot() != null) {
                    eeModule.getWeb().add(web);
                }
            }
        }
        //*/

        return eeModule;
    }

    private Long getApplicationSize(ApplicationDto application, ApplicationAssessmentResult result) throws Exception {
        long applicationSize = 0L;

        if (StringUtils.isNotEmpty(application.getUploadSourceFilePath())) {
            File f = new File(application.getUploadSourceFilePath());

            if (f.exists()) {
                applicationSize = FileUtils.sizeOf(f);
                result.setApplicationSize(applicationSize);
            }

            log.debug("Application size for [{}] is [{}]", application.getUploadSourceFilePath(), applicationSize);
        } else {
            String path = application.getDeployPath();
            String command, r;

            if ("Y".equals(application.getWindowsYn())) {
                command = "Get-ChildItem \"" + path + "\" -recurse | Measure-Object -property length -sum | Select-Object Sum;";
                r = WinRmUtils.executePsShell(application.getTargetHost(), command);

                try {
                    r = r.split("\\r?\\n")[2];
                    applicationSize = Long.parseLong(r);
                    result.setApplicationSize(applicationSize);
                } catch (Exception e) {
                    log.warn("Unhandled exception occurred while get application size. Reason : [{}]", e.getMessage());
                    // ignore
                }
            } else {
                // uname 조회
                String uname = SSHUtil.executeCommand(application.getTargetHost(), "uname").trim();
                uname = uname.replaceAll("-", " ");

                if (Domain1013.LINUX.name().equalsIgnoreCase(uname)) {
                    command = "sudo du -b '" + path + "' | tail -1 | awk -F' ' '{print $1}'";
                } else {
                    command = "sudo du -sk '" + path + "' | awk '{printf \"%d\", $1 * 1024}'";
                }

                r = SSHUtil.executeCommand(application.getTargetHost(), command);

                try {
                    applicationSize = Long.parseLong(r);
                    result.setApplicationSize(applicationSize);
                } catch (Exception e) {
                    log.warn("Unhandled exception occurred while get application size. Reason : [{}]", e.getMessage());
                    // ignore
                }
            }

            log.debug("Application size for [{}] is [{}]", path, applicationSize);
        }

        return applicationSize;
    }
}
//end of JavaApplicationAssessment.java