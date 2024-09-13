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
 * SangCheon Park   Nov 22, 2021		First Draft.
 */
package io.playce.roro.api.domain.common.aop;

import io.playce.roro.api.common.error.ErrorCode;
import io.playce.roro.api.common.error.exception.ResourceNotFoundException;
import io.playce.roro.api.common.error.exception.RoRoApiException;
import io.playce.roro.common.dto.subscription.Subscription;
import io.playce.roro.common.dto.subscription.SubscriptionStausType;
import io.playce.roro.common.dto.subscription.SubscriptionType;
import io.playce.roro.common.property.CommonProperties;
import io.playce.roro.common.util.CommandUtil;
import io.playce.roro.common.util.SubscriptionUtil;
import io.playce.roro.common.util.ThreadLocalUtils;
import io.playce.roro.jpa.repository.ProjectMasterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;

import static io.playce.roro.api.common.CommonConstants.ORIGIN_HOST;
import static io.playce.roro.api.common.CommonConstants.RORO_HOST;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
@DependsOn({"commonProperties"})
@Order(1)
public class SubscriptionManager {

    /**
     * <pre>
     * 1. License가 아닌 Subscription 방식
     * 2. Subscription에는 다음과 같은 정보를 포함
     *    a. Type
     *       - 아래 4개의 타입(가칭)을 가지며, 아래로 갈 수록 상위 기능을 포함한다.
     *       - Trial => (Count는 5, Expire Date는 생성일로부터 15일로 한다.)
     *       - Enterprise (Inventory&Assessment)
     *       - Enterprise (Migration)
     *       - Enterprise (Verify)
     *    b. Count
     *       - Inventory Server 갯수를 의미 (해당 숫자 이상의 서버 등록 불가)
     *    c. Expire Date
     *       - 서브스크립션 만료일 (기본은 생성일로부터 1년이며, 해당 날짜가 지나면 읽기만 가능)
     *    d. Signature
     *       - Trial 타입에서는 사용되지 않음
     *       - Mac Address와 같은 서버 고유의 값을 확인할 수 있는 키 (고객으로부터 전달 받아야 하는 값)
     *
     * <Subscription 처리 프로세스>
     * 1. RoRo 서버 구동 시 roro.working.dir-path 내에 .roro_subription 파일을 읽는다. (subscription.path 옵션으로 변경 가능)
     *    -> 파일이 없는 경우 Subscription Not Found 상태
     * 2. 파일 내용을 복호화 하여 Subscription 정보에 해당하는 Value Object로 바인딩 시킨다.
     *    -> 복호화 및 바인딩이 실패하는 경우 Subscription Invalid
     * 3. Trial 타입이 아닌 경우 RoRo 서버 내의 signature 파일을 실행하고 Subscription 정보 내의 signature 정보와 비교한다.
     *    -> signature 정보가 틀린 경우 Signature Does Not Match
     * 4. 만료일이 지난 경우
     *    -> Subscription Expired
     * 5. 등록된 Inventory Server 갯수가 Count를 넘을 수 없도록 처리
     * 6. Subscription 상태와 정보를 전역으로 저장하고 API 호출시 처리
     *    -> Enterprise (Inventory&Assessment)는 Migration API를 사용할 수 없음
     * </pre>
     */

    private static final String AUTH_URI = "/api/auth";
    private static final String COMMON_URI = "/api/common";
    private static final String SUBSCRIPTION_FILE = ".roro_subscription";

    private static Subscription subscription;

    private final ProjectMasterRepository projectMasterRepository;
    private File signatureFile;

    /**
     * Gets subscription.
     *
     * @return the subscription
     */
    public static Subscription getSubscription() {
        return subscription;
    }

    /**
     * Logging.
     *
     * @param joinPoint the join point
     *
     * @throws Throwable the throwable
     */
    @Before("execution(* io.playce.roro.api.domain.*.controller.*Controller*.*(..)) " //+
//            "&& !execution(* io.playce.roro.api.domain.assessment.controller.WindowsController.*(..)) " +
//            "&& !execution(* io.playce.roro.api.domain.assessment.controller.WindowsRestController.*(..))"
    )
    public void logging(JoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        // To create a hyper link in excel report
        ThreadLocalUtils.add(ORIGIN_HOST, request.getHeader("x-forwarded-host"));
        ThreadLocalUtils.add(RORO_HOST, request.getHeader("host"));

        // log.debug("start - " + joinPoint.getSignature().getDeclaringTypeName() + " / " + joinPoint.getSignature().getName());

        if (!request.getRequestURI().contains(AUTH_URI) && !request.getRequestURI().contains(COMMON_URI) && !request.getMethod().equals(HttpMethod.GET.name())) {
            // 서브스크립션 파일이 없으면 읽기만 가능
            if (subscription.getSubscriptionStausType().equals(SubscriptionStausType.SUBSCRIPTION_NOT_FOUND)) {
                throw new RoRoApiException(ErrorCode.SUBSCRIPTION_NOT_FOUND);
            }

            // 서브스크립션 파일의 유효 하지 않으면 읽기만 가능
            if (subscription.getSubscriptionStausType().equals(SubscriptionStausType.SUBSCRIPTION_INVALID)) {
                throw new RoRoApiException(ErrorCode.SUBSCRIPTION_INVALID);
            }

            // 서브스크립션 파일의 Signature가 일치 하지 않으면 읽기만 가능
            if (subscription.getSubscriptionStausType().equals(SubscriptionStausType.SIGNATURE_NOT_MATCH)) {
                throw new RoRoApiException(ErrorCode.SUBSCRIPTION_SIGNATURE_NOTMATCH);
            }

            // 서브스크립션 기한이 만료되었으면 읽기만 가능
            if (subscription.getSubscriptionStausType().equals(SubscriptionStausType.SUBSCRIPTION_EXPIRED)) {
                throw new RoRoApiException(ErrorCode.SUBSCRIPTION_EXPIRED);
            }

            // 서브스크립션 만료 여부 검사
            if (subscription.getExpireDate().getTime() < System.currentTimeMillis()) {
                subscription.setSubscriptionStausType(SubscriptionStausType.SUBSCRIPTION_EXPIRED);
                throw new RoRoApiException(ErrorCode.SUBSCRIPTION_EXPIRED);
            }
        }

        // 삭제된 project에 대한 API 호출인지를 확인한다.
        if (request.getRequestURI().matches("^/api/projects/[0-9]+/.*$")) {
            long projectId = Long.parseLong(request.getRequestURI().split("/")[3]);
            projectMasterRepository.findById(projectId).orElseThrow(() -> new ResourceNotFoundException("Project ID : " + projectId + " Not Found."));
        }

        // 서브스크립션 타입이 TRIAL 또는 MIGRATION_AND_VERIFY 이 아니면 Migration 및 Verify 관련 API에 접근할 수 없다.
        if (!SubscriptionType.TRIAL.equals(subscription.getType()) && !SubscriptionType.MIGRATION_AND_VERIFY.equals(subscription.getType())) {
            if (request.getRequestURI().contains("/api/migration") || request.getRequestURI().contains("/api/target-cloud") ||
                    request.getRequestURI().contains("/api/verify")) {
                if (subscription.getType() == null) {
                    throw new RoRoApiException(ErrorCode.SUBSCRIPTION_NOT_ALLOWED1);
                } else {
                    throw new RoRoApiException(ErrorCode.SUBSCRIPTION_NOT_ALLOWED2);
                }
            }
        }

        // log.debug("finished - " + joinPoint.getSignature().getDeclaringTypeName() + " / " + joinPoint.getSignature().getName());
    }

    @PostConstruct
    private void init() {
        copyResource();
        subscription = new Subscription();
        subscription.setSubscriptionStausType(SubscriptionStausType.SUBSCRIPTION_NOT_FOUND);

        /**
         * 1. RoRo 서버 구동 시 roro.working.dir-path 내에 .roro_subsription 파일을 읽는다.
         *    -> 파일이 없는 경우 Subscription Not Found 상태
         * 2. 파일 내용을 복호화 하여 Subscription 정보에 해당하는 Value Object로 바인딩 시킨다.
         *    -> 복호화 및 바인딩이 실패하는 경우 Subscription Invalid
         * 3. Trial 타입이 아닌 경우 RoRo 서버 내의 signature 파일을 실행하고 Subscription 정보 내의 signature 정보와 비교한다.
         *    -> signature 정보가 틀린 경우 Signature Does Not Match
         */
        if (StringUtils.isNotEmpty(CommonProperties.getWorkDir())) {
            File subscriptionFile;
            if (StringUtils.isNotEmpty(CommonProperties.getProperty("roro.subscription.path"))) {
                subscriptionFile = new File(CommonProperties.getProperty("roro.subscription.path"));
            } else {
                subscriptionFile = new File(CommonProperties.getWorkDir(), SUBSCRIPTION_FILE);
            }

            if (subscriptionFile.exists()) {
                try {
                    String subscriptionStr = FileUtils.readFileToString(subscriptionFile, "UTF-8");
                    subscription = SubscriptionUtil.getSubscription(subscriptionStr);
                    subscription.setSubscriptionStausType(SubscriptionStausType.SUBSCRIPTION_VALID);

                    if (subscription.getExpireDate().getTime() < System.currentTimeMillis()) {
                        subscription.setSubscriptionStausType(SubscriptionStausType.SUBSCRIPTION_EXPIRED);
                    } else {
                        if (!subscription.getType().equals(SubscriptionType.TRIAL)) {
                            CommandLine cl = CommandUtil.getCommandLine(CommandUtil.findCommand("sudo"), signatureFile.getAbsolutePath());
                            String result = CommandUtil.executeCommand(cl);

                            if (!result.equals(subscription.getSignature())) {
                                subscription.setSubscriptionStausType(SubscriptionStausType.SIGNATURE_NOT_MATCH);
                            } else {
                                subscription.setSubscriptionStausType(SubscriptionStausType.SUBSCRIPTION_VALID);
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("Unhandled exception occurred while check subscription.", e);
                    subscription.setSubscriptionStausType(SubscriptionStausType.SUBSCRIPTION_INVALID);
                }
            }
        }

        log.debug("RoRo Subscription Type : [{}], Status : [{}]", subscription.getType(), subscription.getSubscriptionStausType());
    }

    private void copyResource() {
        String tmpDir = CommonProperties.getProperty("java.io.tmpdir");
        ClassLoader classLoader = getClass().getClassLoader();
        signatureFile = new File(tmpDir, "scripts/signature");

        try {
            FileUtils.copyURLToFile(classLoader.getResource("scripts/signature"), signatureFile);
            log.debug("signature file copied to " + signatureFile.getAbsolutePath());
            if (signatureFile.setExecutable(true, false)) {
                log.debug("Set executable permission " + signatureFile.getAbsolutePath());
            }
        } catch (IOException e) {
            // nothing to do
            log.warn("Failed signature file copied to " + signatureFile.getAbsolutePath());
        }
    }
}
//end of SubscriptionManager.java