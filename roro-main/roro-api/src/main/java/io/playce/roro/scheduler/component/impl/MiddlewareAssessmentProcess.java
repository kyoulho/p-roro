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
 * Author			Date				Description
 * ---------------	----------------	------------
 * Dong-Heon Han    Jan 06, 2022		First Draft.
 */

package io.playce.roro.scheduler.component.impl;

import io.playce.roro.api.domain.insights.service.InsightsService;
import io.playce.roro.api.domain.inventory.service.InventoryProcessService;
import io.playce.roro.api.domain.tracking.TrackingInfoService;
import io.playce.roro.common.cancel.InventoryProcessCancelInfo;
import io.playce.roro.common.code.*;
import io.playce.roro.common.dto.common.InventoryProcessConnectionInfo;
import io.playce.roro.common.dto.inventory.process.InventoryProcessQueueItem;
import io.playce.roro.common.exception.InsufficientException;
import io.playce.roro.common.exception.NotsupportedException;
import io.playce.roro.common.exception.RoRoException;
import io.playce.roro.common.util.SSHUtil;
import io.playce.roro.common.util.ThreadLocalUtils;
import io.playce.roro.common.util.support.MiddlewareInventory;
import io.playce.roro.common.util.support.TargetHost;
import io.playce.roro.jpa.entity.MiddlewareMaster;
import io.playce.roro.jpa.repository.MiddlewareMasterRepository;
import io.playce.roro.mw.asmt.MiddlewareAssessment;
import io.playce.roro.mw.asmt.MiddlewarePostProcess;
import io.playce.roro.mw.asmt.apache.ApacheAssessment;
import io.playce.roro.mw.asmt.apache.dto.ApacheAssessmentResult;
import io.playce.roro.mw.asmt.dto.MiddlewareAssessmentResult;
import io.playce.roro.mw.asmt.jboss.JBossAssessment;
import io.playce.roro.mw.asmt.jboss.dto.JbossAssessmentResult;
import io.playce.roro.mw.asmt.jeus.JeusAssessment;
import io.playce.roro.mw.asmt.jeus.dto.JeusAssessmentResult;
import io.playce.roro.mw.asmt.nginx.NginxAssessment;
import io.playce.roro.mw.asmt.nginx.dto.NginxAssessmentResult;
import io.playce.roro.mw.asmt.tomcat.TomcatAssessment;
import io.playce.roro.mw.asmt.tomcat.dto.TomcatAssessmentResult;
import io.playce.roro.mw.asmt.util.GetInfoStrategy;
import io.playce.roro.mw.asmt.weblogic.WebLogicAssessment;
import io.playce.roro.mw.asmt.weblogic.dto.WebLogicAssessmentResult;
import io.playce.roro.mw.asmt.websphere.WebSphereAssessment;
import io.playce.roro.mw.asmt.websphere.dto.WebSphereAssessmentResult;
import io.playce.roro.mw.asmt.webtob.WebToBAssessment;
import io.playce.roro.mw.asmt.webtob.dto.WebToBAssessmentResult;
import io.playce.roro.mybatis.domain.inventory.middleware.MiddlewareMapper;
import io.playce.roro.mybatis.domain.inventory.server.ServerMapper;
import io.playce.roro.mybatis.domain.thirdparty.ThirdPartyMapper;
import io.playce.roro.scheduler.component.AbstractAssessmentProcess;
import io.playce.roro.scheduler.service.impl.AssessmentSchedulerManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

import static io.playce.roro.common.util.ThreadLocalUtils.MW_SCAN_ERROR;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Dong-Heon Han
 * @version 3.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MiddlewareAssessmentProcess extends AbstractAssessmentProcess {
    private final Map<String, MiddlewareAssessment> middlewareAssessmentMap;
    private final InventoryProcessService inventoryProcessService;
    private final InsightsService insightsService;
    private final ServerMapper serverMapper;
    private final MiddlewareMapper middlewareMapper;
    private final ThirdPartyMapper thirdPartyMapper;
    private final MiddlewarePostProcessor middlewarePostProcessor;
    private final Map<String, MiddlewarePostProcess> middlewarePostProcessMap;
    private final TrackingInfoService trackingInfoService;
    private final MiddlewareMasterRepository middlewareMasterRepository;

    @Override
    public Domain1003 assessment(InventoryProcessQueueItem item, Domain1003 resultState) throws InterruptedException {
        Long inventoryProcessId = item.getInventoryProcessId();
        log.debug("Step 4 ~ item: {}", item);

        MiddlewareInventory middlewareInventory = null;
        MiddlewareAssessmentResult result = null;
        String resultString = null;
        boolean saveReport = false;
        TargetHost targetHost = null;

        try {
            middlewareInventory = middlewareMapper.selectMiddlewareInventory(item.getInventoryId());

            // inventory detail type code 'ETC' not support
            if (Domain1013.ETC.name().equals(middlewareInventory.getInventoryDetailTypeCode())) {
                throw new NotsupportedException("Inventory Detail Type Code(" + middlewareInventory.getInventoryDetailTypeCode() + ") does not supported.");
            }

            // middleware type code 'ETC' not support
            if (Domain1102.ETC.name().equals(middlewareInventory.getMiddlewareTypeCode())) {
                throw new NotsupportedException("Middleware Type Code(" + middlewareInventory.getMiddlewareTypeCode() + ") does not supported.");
            }

            String engineVersion = middlewareInventory.getEngineVersion();
            String solutionName = "";
            String javaVersion = "";
            String javaVendor = "";

            if (engineVersion != null && engineVersion.contains(".")) {
                engineVersion = engineVersion.substring(0, engineVersion.indexOf("."));
            }
            String componentName = makeComponentName(middlewareInventory.getInventoryDetailTypeCode(), engineVersion);
            MiddlewareAssessment assessment = middlewareAssessmentMap.get(componentName);
            if (assessment == null) {
                componentName = makeComponentName(middlewareInventory.getInventoryDetailTypeCode());
                assessment = middlewareAssessmentMap.get(componentName);
            }
            log.debug("exec component name: {}", componentName);

            if (assessment == null) {
                // throw new RoRoException("The processing component does not exist.");
                throw new NotsupportedException("Scan cannot be performed. It is not supported Middleware.");
            }

            InventoryProcessConnectionInfo connectionInfo = serverMapper.selectServerConnectionInfoByInventoryProcessId(item.getInventoryProcessId());
            log.debug("Step 4 ~ id: {}. load connection info: {}", inventoryProcessId, connectionInfo);
            targetHost = InventoryProcessConnectionInfo.targetHost(connectionInfo);

            if (!targetHost.isValid()) {
                throw new InsufficientException("Insufficient server connection information.");
            }

            // List<ThirdPartySearchTypeResponse> thirdPartySearchTypeList = thirdPartyMapper.selectThirdPartySearchType();
            // targetHost.setThirdPartySearchTypeList(thirdPartySearchTypeList);

            GetInfoStrategy strategy = GetInfoStrategy.getStrategy(connectionInfo.getWindowsYn().equals("Y"));
            result = assessment.assessment(targetHost, middlewareInventory, strategy);

            // Scan이 정상적으로 완료된 경우 상태에 관계없이 Report 생성 대상이 된다.
            saveReport = true;

            // Unsupported Middleware version 체크
            if (!checkUnsupportedMiddleware(result)) {
                // 지원되지 않는 버전의 Middleware 이지만 정상 수행 가능성이 있기 때문에 Exception을 throw 하지 않고 데이터 누락이 있을 수 있다는 메시지만 추가한다.
                // resultState = Domain1003.UNS;
                resultString = "Not tested middleware version, some information may be missing.";
            }

            MiddlewareMaster middlewareMaster = middlewareMasterRepository.findById(item.getInventoryId()).orElse(null);

            if (StringUtils.isEmpty(engineVersion)) {
                if (assessment instanceof ApacheAssessment) {
                    engineVersion = ((ApacheAssessmentResult.Engine) result.getEngine()).getVersion();
                } else if (assessment instanceof JBossAssessment) {
                    engineVersion = ((JbossAssessmentResult.Engine) result.getEngine()).getVersion();
                } else if (assessment instanceof JeusAssessment) {
                    engineVersion = ((JeusAssessmentResult.Engine) result.getEngine()).getVersion();
                } else if (assessment instanceof TomcatAssessment) {
                    engineVersion = ((TomcatAssessmentResult.Engine) result.getEngine()).getVersion();
                } else if (assessment instanceof WebLogicAssessment) {
                    engineVersion = ((WebLogicAssessmentResult.Engine) result.getEngine()).getVersion();
                } else if (assessment instanceof WebSphereAssessment) {
                    engineVersion = ((WebSphereAssessmentResult.Engine) result.getEngine()).getVersion();
                } else if (assessment instanceof WebToBAssessment) {
                    engineVersion = ((WebToBAssessmentResult.Engine) result.getEngine()).getVersion();
                } else if (assessment instanceof NginxAssessment) {
                    engineVersion = ((NginxAssessmentResult.Engine) result.getEngine()).getVersion();
                }

                if (middlewareMaster != null && StringUtils.isEmpty(middlewareMaster.getEngineVersion())) {
                    middlewareMaster.setEngineVersion(engineVersion);
                }
                middlewareInventory.setEngineVersion(engineVersion);

                if (engineVersion != null && engineVersion.contains(".")) {
                    engineVersion = engineVersion.substring(0, engineVersion.indexOf("."));
                }
            }

            // Solution Name및 Java 정보.
            if (assessment instanceof ApacheAssessment) {
                solutionName = ((ApacheAssessmentResult.Engine) result.getEngine()).getName();
            } else if (assessment instanceof JBossAssessment) {
                solutionName = ((JbossAssessmentResult.Engine) result.getEngine()).getName();
                javaVersion = ((JbossAssessmentResult.Instance) result.getInstance()).getJavaVersion();
                javaVendor = ((JbossAssessmentResult.Instance) result.getInstance()).getJavaVendor();
            } else if (assessment instanceof JeusAssessment) {
                solutionName = ((JeusAssessmentResult.Engine) result.getEngine()).getName();
                javaVersion = ((JeusAssessmentResult.Instance) result.getInstance()).getJavaVersion();
                javaVendor = ((JeusAssessmentResult.Instance) result.getInstance()).getJavaVendor();
            } else if (assessment instanceof TomcatAssessment) {
                solutionName = ((TomcatAssessmentResult.Engine) result.getEngine()).getName();
                javaVersion = ((TomcatAssessmentResult.Instance) result.getInstance()).getJavaVersion();
                javaVendor = ((TomcatAssessmentResult.Instance) result.getInstance()).getJavaVendor();
            } else if (assessment instanceof WebLogicAssessment) {
                solutionName = ((WebLogicAssessmentResult.Engine) result.getEngine()).getName();
                javaVersion = ((WebLogicAssessmentResult.Instance) result.getInstance()).getJavaVersion();
                javaVendor = ((WebLogicAssessmentResult.Instance) result.getInstance()).getJavaVendor();
            } else if (assessment instanceof WebSphereAssessment) {
                solutionName = ((WebSphereAssessmentResult.Engine) result.getEngine()).getName();
                javaVersion = ((WebSphereAssessmentResult.Instance) result.getInstance()).getGeneral().getJavaVersion();
                javaVendor = ((WebSphereAssessmentResult.Instance) result.getInstance()).getGeneral().getJavaVendor();
            } else if (assessment instanceof WebToBAssessment) {
                solutionName = ((WebToBAssessmentResult.Engine) result.getEngine()).getName();
            } else if (assessment instanceof NginxAssessment) {
                solutionName = ((NginxAssessmentResult.Engine) result.getEngine()).getName();
            }

            middlewareMaster.setJavaVersion(StringUtils.isEmpty(javaVersion) ? StringUtils.EMPTY : javaVersion);
            middlewareMaster.setJavaVendor(StringUtils.isEmpty(javaVendor) ? StringUtils.EMPTY : javaVendor);
            middlewareMasterRepository.save(middlewareMaster);

            // Insight 생성.
            // 2023.07.19 : solution name 또는 engine version이 있어야 insight를 생성한다.
            if (StringUtils.isNotEmpty(solutionName) && StringUtils.isNotEmpty(middlewareInventory.getEngineVersion())) {
                insightsService.createInventoryLifecycleVersionLink(item.getInventoryId(), Domain1001.MW, solutionName, middlewareInventory.getEngineVersion(), javaVendor, javaVersion);
            } else {
                log.debug("Not Found Solution Name : {}, Engine Version: {}", solutionName, middlewareInventory.getEngineVersion());
            }

            String postProcessorName = makePostProcessorName(middlewareInventory.getInventoryDetailTypeCode(),
                    StringUtils.defaultString(engineVersion));

            MiddlewarePostProcess postProcess = middlewarePostProcessMap.get(postProcessorName);
            if (postProcess == null) {
                postProcessorName = makePostProcessorName(middlewareInventory.getInventoryDetailTypeCode());
                postProcess = middlewarePostProcessMap.get(postProcessorName);
            }

            if (postProcess == null) {
                //throw new RoRoException("The post processor component does not exist.");
                throw new NotsupportedException("Scan cannot be performed. It is an unsupported Middleware version.");
            }

            synchronized (AssessmentSchedulerManager.lockMw) {
                if (!InventoryProcessCancelInfo.hasCancelRequest(item.getInventoryProcessId())) {
                    try {
                        middlewarePostProcessor.setDiscoveredData(postProcess, item, targetHost, Domain1006.INV, middlewareInventory, result, strategy);
                        trackingInfoService.saveMiddlewareTrackingInfo(inventoryProcessId, result);
                    } catch (Exception e) {
                        log.error("Unhandled exception occurred while execute middleware scan's post processing.", e);

                        // 후 처리 과정에서 에러가 발생한 경우도 Partially Completed로 처리한다.
                        resultState = Domain1003.PC;

                        if (StringUtils.isEmpty(resultString)) {
                            resultString = "Post processing failed. [Reason] " + e.getMessage();
                        } else {
                            resultString += "\nPost processing failed. [Reason] " + e.getMessage();
                        }
                    }
                }
            }

            // Partial Completed Check
            if (ThreadLocalUtils.get(MW_SCAN_ERROR) == null && !resultState.equals(Domain1003.PC)) {
                resultState = Domain1003.CMPL;
            } else {
                resultState = Domain1003.PC;
                resultString = (String) ThreadLocalUtils.get(MW_SCAN_ERROR);
            }
        } catch (Throwable e) {
            e = getCausedException(e);

            boolean isContainer = false;
            if (e instanceof RoRoException || e instanceof InsufficientException) {
                isContainer = checkIsRunningOnContainer(middlewareInventory, targetHost);
            }

            if (isContainer) {
                resultState = Domain1003.NS;
                resultString = "Scan of middleware running as Docker containers is not yet supported.";
            } else {
                ScanResult scanResult = getScanResult(e);

                if (scanResult != null) {
                    resultState = scanResult.getResultState();
                    resultString = scanResult.getResultString();
                }

                log.error("item {} - {}", item, resultString, e);
            }
        } finally {
            ThreadLocalUtils.clearSharedObject();

            synchronized (AssessmentSchedulerManager.lockMw) {
                if (!InventoryProcessCancelInfo.hasCancelRequest(item.getInventoryProcessId())) {
                    String message = saveResult(item, inventoryProcessService, inventoryProcessId, result, resultString, saveReport);

                    if (StringUtils.isNotEmpty(message)) {
                        if (resultState.equals(Domain1003.CMPL)) {
                            resultState = Domain1003.PC;
                        }
                    }
                }
            }
        }
        return resultState;
    }

    private boolean checkIsRunningOnContainer(MiddlewareInventory middlewareInventory, TargetHost targetHost) {
        boolean isContainer = false;

        try {
            log.debug("Middleware type : [{}], enginePath : [{}], instancePath : [{}]", middlewareInventory.getInventoryDetailTypeCode(),
                    middlewareInventory.getEngineInstallationPath(), middlewareInventory.getDomainHomePath());

            StringBuilder psCmd = new StringBuilder().append("ps -ef | grep ");

            if (middlewareInventory.getInventoryDetailTypeCode().equals(Domain1013.APACHE.name())) {
                psCmd.append("httpd");
            } else if (middlewareInventory.getInventoryDetailTypeCode().equals(Domain1013.NGINX.name())) {
                psCmd.append("nginx | grep master");
            } else if (middlewareInventory.getInventoryDetailTypeCode().equals(Domain1013.TOMCAT.name())) {
                psCmd.append("java | grep tomcat");
            }

            if (StringUtils.isNotEmpty(middlewareInventory.getEngineInstallationPath())) {
                psCmd.append(" | grep '").append(middlewareInventory.getEngineInstallationPath()).append("'");
            }

            psCmd.append(" | grep -v grep | head -1 | awk '{print $2}'");

            String pid = SSHUtil.executeCommand(targetHost, psCmd.toString());

            if (StringUtils.isNotEmpty(pid)) {
                String cmd = "ps -e -o pid,cgroup | grep " + pid;
                String result = SSHUtil.executeCommand(targetHost, cmd);

                if (StringUtils.isNotEmpty(result) && result.contains("docker")) {
                    isContainer = true;
                }
            }
        } catch (Exception e) {
            // ignore
            log.error("Unhandled exception occurred while check the middleware run as a container.", e);
        }

        return isContainer;
    }

    private boolean checkUnsupportedMiddleware(MiddlewareAssessmentResult result) {
        boolean isSupported = false;

        String version;
        if (result instanceof ApacheAssessmentResult) {
            ApacheAssessmentResult.Engine engine = (ApacheAssessmentResult.Engine) result.getEngine();
            if (engine != null) {
                version = engine.getVersion();

                if (StringUtils.isNotEmpty(version) && version.compareTo("2.2") >= 0) {
                    isSupported = true;
                }
            }
        } else if (result instanceof JeusAssessmentResult) {
            JeusAssessmentResult.Engine engine = (JeusAssessmentResult.Engine) result.getEngine();
            if (engine != null) {
                version = engine.getVersion();

                if (StringUtils.isNotEmpty(version) && version.compareTo("6") >= 0) {
                    isSupported = true;
                }
            }
        } else if (result instanceof TomcatAssessmentResult) {
            TomcatAssessmentResult.Engine engine = (TomcatAssessmentResult.Engine) result.getEngine();
            if (engine != null) {
                version = engine.getVersion();

                if (StringUtils.isNotEmpty(version) && version.compareTo("7") >= 0) {
                    isSupported = true;
                }
            }
        } else if (result instanceof WebLogicAssessmentResult) {
            WebLogicAssessmentResult.Engine engine = (WebLogicAssessmentResult.Engine) result.getEngine();
            if (engine != null) {
                version = engine.getVersion();

                if (StringUtils.isNotEmpty(version) && version.compareTo("10") >= 0) {
                    isSupported = true;
                }
            }
        } else if (result instanceof WebSphereAssessmentResult) {
            WebSphereAssessmentResult.Engine engine = (WebSphereAssessmentResult.Engine) result.getEngine();
            if (engine != null) {
                version = engine.getVersion();

                if (StringUtils.isNotEmpty(version) && version.compareTo("7") >= 0) {
                    isSupported = true;
                }
            }
        } else if (result instanceof JbossAssessmentResult) {
            JbossAssessmentResult.Engine engine = (JbossAssessmentResult.Engine) result.getEngine();
            if (engine != null) {
                version = engine.getVersion();

                if (StringUtils.isNotEmpty(version) && version.compareTo("6") >= 0) {
                    isSupported = true;
                }
            }
        } else if (result instanceof WebToBAssessmentResult) {
            WebToBAssessmentResult.Engine engine = (WebToBAssessmentResult.Engine) result.getEngine();
            if (engine != null) {
                version = engine.getVersion();

                if (StringUtils.isNotEmpty(version)) {
                    version = version.replaceAll("WebtoB ", "")
                            .replaceAll("Version ", "");
                }

                if (StringUtils.isNotEmpty(version) && version.substring(0, 1).compareTo("4") >= 0) {
                    isSupported = true;
                }
            }
        } else if (result instanceof NginxAssessmentResult) {
            NginxAssessmentResult.Engine engine = (NginxAssessmentResult.Engine) result.getEngine();

            if (engine != null) {
                version = engine.getVersion();
                if (StringUtils.isNotEmpty(version)) {
                    long dotCount = StringUtils.countMatches(version, ".");
                    String compareVersion = dotCount >= 2 ? version.substring(0, version.lastIndexOf(".")) : version;

                    if (compareVersion.compareTo("1.14") >= 0) {
                        isSupported = true;
                    }
                }
            }
        }

        return isSupported;
    }
}