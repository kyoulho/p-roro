/*
 * Copyright 2022 The Playce-RoRo Project.
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
 * Jaeeon Bae       5월 19, 2022            First Draft.
 */
package io.playce.roro.api.domain.cloudreadiness.service;

import io.playce.roro.api.common.error.ErrorCode;
import io.playce.roro.api.common.error.exception.ResourceNotFoundException;
import io.playce.roro.api.common.error.exception.RoRoApiException;
import io.playce.roro.api.common.util.WebUtil;
import io.playce.roro.api.domain.inventory.service.ServerService;
import io.playce.roro.api.domain.inventory.service.helper.survey.CloudReadinessParser;
import io.playce.roro.api.domain.inventory.service.helper.survey.SurveyParser;
import io.playce.roro.common.code.Domain1003;
import io.playce.roro.common.dto.cloudreadiness.*;
import io.playce.roro.common.dto.cloudreadiness.CloudReadinessQuestionResponse.Answer;
import io.playce.roro.common.dto.cloudreadiness.CloudReadinessQuestionResponse.Question;
import io.playce.roro.common.dto.cloudreadiness.CloudReadinessQuestionResponse.*;
import io.playce.roro.common.dto.inventory.server.ServerResponse;
import io.playce.roro.common.dto.inventory.service.ServiceDetail;
import io.playce.roro.jpa.entity.*;
import io.playce.roro.jpa.entity.pk.SurveyUserAnswerId;
import io.playce.roro.jpa.repository.*;
import io.playce.roro.mybatis.domain.cloudreadiness.CloudReadinessMapper;
import io.playce.roro.mybatis.domain.inventory.service.ServiceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static io.playce.roro.api.common.CommonConstants.*;
import static io.playce.roro.common.util.support.DistinctByKey.distinctByKey;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Jaeeon Bae
 * @version 3.0
 */
@Slf4j
@Transactional
@Service
@RequiredArgsConstructor
public class CloudReadinessService {

    private final CloudReadinessExcelExporter cloudReadinessExcelExporter;
    private final ServiceMasterRepository serviceMasterRepository;
    private final SurveyRepository surveyRepository;
    private final SurveyProcessRepository surveyProcessRepository;
    private final SurveyUserAnswerRepository surveyUserAnswerRepository;
    private final SurveyQuestionRepository surveyQuestionRepository;
    private final QuestionAnswerRepository questionAnswerRepository;

    private final CloudReadinessMapper cloudReadinessMapper;
    private final ServiceMapper serviceMapper;
    private final ServerService serverService;

    public CloudReadinessQuestionResponse getQuestions(Long surveyId) {
        // 설문
        CloudReadinessQuestionResponse cloudReadinessQuestionResponse = cloudReadinessMapper.selectSurvey(surveyId);

        // 질문 목록
        List<QuestionDto> questionDtos = cloudReadinessMapper.selectQuestion(surveyId);

        // 답변 목록
        List<CloudReadinessQuestionResponse.Answer> answer = cloudReadinessMapper.selectAnswer();

        // 카테고리 가져오기
        List<Category> categories = questionDtos.stream()
                .map(p -> new Category(p.getSurveyCategoryId(), p.getCategoryStep(), p.getCategoryNameEnglish(), p.getCategoryNameKorean()))
                .filter(distinctByKey(Category::getSurveyCategoryId))
                .collect(Collectors.toList());

        // 평가항목 가져오기
        List<EvaluationItem> evaluationItems = questionDtos.stream()
                .map(p -> new EvaluationItem(p.getParentSurveyCategoryId(), p.getEvaluationItemId(), p.getEvaluationItemEnglish(), p.getEvaluationItemKorean()))
                .filter(distinctByKey(EvaluationItem::getSurveyCategoryId))
                .collect(Collectors.toList());

        // 질문 목록
        List<CloudReadinessQuestionResponse.Question> questions = questionDtos.stream()
                .map(p -> new CloudReadinessQuestionResponse.Question(p.getQuestionSurveyCategoryId(), p.getQuestionId(), p.getQuestionContentEnglish(), p.getQuestionContentKorean(), p.getQuestionDisplayOrder()))
                .collect(Collectors.toList());

        // 평가 항목 Grouping
        Map<Long, List<EvaluationItem>> groupingEvaluationItemMap = evaluationItems.stream()
                .collect(Collectors.groupingBy(EvaluationItem::getParentSurveyCategoryId));

        // 질문 목록 Grouping
        Map<Long, List<CloudReadinessQuestionResponse.Question>> groupingQuestionMap = questions.stream()
                .collect(Collectors.groupingBy(CloudReadinessQuestionResponse.Question::getQuestionSurveyCategoryId));

        // 답변 목록 Grouping
        Map<Long, List<CloudReadinessQuestionResponse.Answer>> groupingAnswerMap = answer.stream()
                .collect(Collectors.groupingBy(Answer::getQuestionId));


        // Category의 Evaluation Item, Question, Answer 추가
        for (Category category : categories) {
            category.setEvaluationItems(groupingEvaluationItemMap.get(category.getSurveyCategoryId()));

            for (EvaluationItem item : category.getEvaluationItems()) {
                item.setQuestions(groupingQuestionMap.get(item.getSurveyCategoryId()));

                for (Question question : item.getQuestions()) {
                    question.setAnswers(groupingAnswerMap.get(question.getQuestionId()));
                }
            }
        }

        if (CollectionUtils.isNotEmpty(categories)) {
            cloudReadinessQuestionResponse.setCategories(categories);
        }

        return cloudReadinessQuestionResponse;
    }

    public List<CloudReadiness> getSurveyList(Long projectId) {
        List<CloudReadiness> cloudReadinesses = cloudReadinessMapper.selectSurveyAnswer(projectId);

        for (CloudReadiness cloudReadiness : cloudReadinesses) {
            cloudReadiness.setSurveyResult(getCloudReadinessSurveyResult(cloudReadiness));

            ServiceMaster serviceMaster = serviceMasterRepository.findById(cloudReadiness.getServiceId()).orElse(null);
            if (serviceMaster != null) {
                List<ServerResponse> serverResponseList = serverService.getServers(serviceMaster.getProjectId(), cloudReadiness.getServiceId(), false);
                boolean hasCompletedScan = false;
                for (ServerResponse sr : serverResponseList) {
                    if (sr.getLastCompleteScan() != null) {
                        hasCompletedScan = true;
                        break;
                    }
                }

                cloudReadiness.setHasCompletedScan(hasCompletedScan);
            }
        }

        return cloudReadinesses;
    }

    public void saveAnswers(Long surveyId, Long serviceId, List<CloudReadinessAnswer> answers) {
        // PCR-5522 서비스 내 스캔 완료된 서버가 하나도 없으면 설문을 저장할 수 없다.
        ServiceMaster serviceMaster = serviceMasterRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Service ID : " + serviceId + " Not Found."));

        List<ServerResponse> serverResponseList = serverService.getServers(serviceMaster.getProjectId(), serviceId, false);
        boolean hasCompletedScan = false;
        for (ServerResponse sr : serverResponseList) {
            if (sr.getLastCompleteScan() != null) {
                hasCompletedScan = true;
                break;
            }
        }

        if (!hasCompletedScan) {
            throw new RoRoApiException(ErrorCode.CLOUD_READINESS_SCAN_INCOMPLETE);
        }

        List<SurveyQuestion> surveyQuestionList = surveyQuestionRepository.findBySurveyId(surveyId);

        // 완성된 설문인지에 대한 validation
        if (surveyQuestionList.size() != answers.size()) {
            throw new RoRoApiException(ErrorCode.CLOUD_READINESS_INVALID);
        }

        // 설문 진행 등록
        SurveyProcess surveyProcess = surveyProcessRepository.findBySurveyIdAndServiceId(surveyId, serviceId).orElse(new SurveyProcess());
        if (surveyProcess.getId() == null) {
            createNewSurveyProcess(surveyProcess, surveyId, serviceId);
        } else {
            Instant instant = new Date().toInstant();
            surveyProcess.setModifyDatetime(instant);
        }

        // 기존 데이터 삭제
        surveyUserAnswerRepository.deleteAllBySurveyProcessId(surveyProcess.getId());

        // 설문 답변 등록
        List<SurveyUserAnswer> userAnswers = answers.stream().map(a -> {
            SurveyUserAnswerId id = makeSurveyUserAnswerId(surveyId, a, surveyProcess);
            SurveyUserAnswer userAnswer = new SurveyUserAnswer();
            userAnswer.setId(id);
            return userAnswer;
        }).collect(Collectors.toList());
        surveyUserAnswerRepository.saveAll(userAnswers);
    }

    private SurveyUserAnswerId makeSurveyUserAnswerId(Long surveyId, CloudReadinessAnswer a, SurveyProcess surveyProcess) {
        SurveyUserAnswerId id = new SurveyUserAnswerId();
        id.setSurveyId(surveyId);
        id.setQuestionId(a.getQuestionId());
        id.setAnswerId(a.getAnswerId());
        id.setSurveyProcessId(surveyProcess.getId());
        return id;
    }

    private void createNewSurveyProcess(SurveyProcess surveyProcess, Long surveyId, Long serviceId) {
        Instant instant = new Date().toInstant();
        Long userId = WebUtil.getUserId();
        surveyProcess.setSurveyId(surveyId);
        surveyProcess.setServiceId(serviceId);
        surveyProcess.setRegistUserId(userId);
        surveyProcess.setRegistDatetime(instant);
        surveyProcess.setModifyUserId(userId);
        surveyProcess.setModifyDatetime(instant);
        surveyProcess.setSurveyProcessResultCode(Domain1003.CMPL.name());
        surveyProcessRepository.save(surveyProcess);
    }

    private String getCloudReadinessSurveyResult(CloudReadiness cloudReadiness) {
        float technicalScore = cloudReadiness.getTechnicalScore();
        float businessScore = cloudReadiness.getBusinessScore();

        String assessmentResult;

        if ((technicalScore > 0 && technicalScore < 2.5) && (businessScore > 0 && businessScore < 2.5)) {
            assessmentResult = CLOUD_READINESS_MIGRATION_RISK;
        } else if ((technicalScore > 0 && technicalScore < 2.5) && (businessScore >= 2.5 && businessScore <= 5)) {
            assessmentResult = CLOUD_READINESS_BUSINESS_FITNESS;
        } else if ((technicalScore >= 2.5 && technicalScore <= 5) && (businessScore > 0 && businessScore < 2.5)) {
            assessmentResult = CLOUD_READINESS_TECHNICAL_FITNESS;
        } else if ((technicalScore >= 2.5 && technicalScore <= 5) && (businessScore >= 2.5 && businessScore <= 5)) {
            assessmentResult = CLOUD_READINESS_MIGRATION_FITNESS;
        } else {
            assessmentResult = "";
        }

        return assessmentResult;
    }

    public CloudReadinessDetail getCloudReadinessDetail(Long projectId, Long serviceId) {
        CloudReadinessDetail cloudReadinessDetail = cloudReadinessMapper.selectCloudReadinessDetail(projectId, serviceId);

        if (cloudReadinessDetail != null) {
            CloudReadiness cloudReadiness = new CloudReadiness();
            cloudReadiness.setTechnicalScore(cloudReadinessDetail.getTechnicalScore());
            cloudReadiness.setBusinessScore(cloudReadinessDetail.getBusinessScore());

            cloudReadinessDetail.setSurveyResult(getCloudReadinessSurveyResult(cloudReadiness));
            cloudReadinessDetail.setAnswerSummary(cloudReadinessMapper.selectAnswerSummary(cloudReadinessDetail.getSurveyProcessId()));
        }
        return cloudReadinessDetail;
    }

    public List<CloudReadinessCategoryResult> getCloudReadinessResultList(Long projectId, String serviceIds) {
        List<CloudReadinessCategoryResult> cloudReadinessCategoryResult = cloudReadinessMapper.selectCloudReadinessResultList(projectId, convertServiceId(serviceIds));
        for (CloudReadinessCategoryResult result : cloudReadinessCategoryResult) {
            CloudReadiness cloudReadiness = new CloudReadiness();
            cloudReadiness.setTechnicalScore(result.getTechnicalScore());
            cloudReadiness.setBusinessScore(result.getBusinessScore());
            result.setSurveyResult(getCloudReadinessSurveyResult(cloudReadiness));
        }
        return cloudReadinessCategoryResult;
    }

    private List<Integer> convertServiceId(String serviceIds) {
        if (StringUtils.isEmpty(serviceIds)) {
            return null;
        }

        List<Integer> serviceIdList = new ArrayList<>();

        StringTokenizer st = new StringTokenizer(serviceIds, ",");
        while (st.hasMoreTokens()) {
            serviceIdList.add(Integer.parseInt(st.nextToken()));
        }

        return serviceIdList;
    }

    public ByteArrayOutputStream getCloudReadinessReport(Long projectId, String serviceIds, String fileType) {
        List<CloudReadinessCategoryResult> cloudReadinessCategoryResult = getCloudReadinessResultList(projectId, serviceIds);

        if (cloudReadinessCategoryResult == null || cloudReadinessCategoryResult.size() == 0) {
            throw new RoRoApiException(ErrorCode.CLOUD_READINESS_NOT_FOUND);
        }

        if (EXCEL_FILE_TYPE.equalsIgnoreCase(fileType)) {
            return cloudReadinessExcelExporter.createExcelReport(cloudReadinessCategoryResult);
        }

        return null;
    }

    public ByteArrayOutputStream getCloudReadinessTemplate(Long projectId) {
        List<ServiceDetail> serviceList = serviceMapper.selectServiceList(projectId);

        if (serviceList == null || serviceList.size() == 0) {
            throw new RoRoApiException(ErrorCode.INVENTORY_SERVICE_NOT_FOUND);
        }

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XSSFWorkbook workbook = new XSSFWorkbook(Objects.requireNonNull(
                    CloudReadinessExcelExporter.class.getResourceAsStream("/template/Cloud_Readiness_Survey_Template.xlsx")));

            cloudReadinessExcelExporter.writeServiceList(workbook, serviceList);

            workbook.write(out);

            return out;
        } catch (IOException e) {
            log.error("Unhandled exception occurred while create a survey template file.", e);
            throw new RoRoApiException(ErrorCode.EXCEL_CREATE_FAILED, e.getMessage());
        }
    }

    public Map<Long, List<CloudReadinessAnswer>> parseCloudReadiness(XSSFWorkbook workbook, List<CloudReadinessUploadFail> validationList) {
        Map<Long, List<CloudReadinessAnswer>> surveAnsweryMap = new HashMap<>();
        List<QuestionAnswer> questionAnswers = questionAnswerRepository.findAll();

        SurveyParser cloudReadinessSurveyParser = new CloudReadinessParser();
        surveAnsweryMap = cloudReadinessSurveyParser.parse(workbook, surveAnsweryMap, validationList);

        // quetion answer의 각 응답의 값와 매핑 한다.
        for (Long key : surveAnsweryMap.keySet()) {
            List<CloudReadinessAnswer> surveyAnswers = surveAnsweryMap.get(key);

            for (CloudReadinessAnswer cra : surveyAnswers) {
                List<QuestionAnswer> questionAnswer = questionAnswers.stream().filter(p -> p.getQuestionId() == cra.getQuestionId().intValue()).collect(Collectors.toList());
                if (cra.getAnswerId() != null) {
                    if (cra.getAnswerId() == 0 || cra.getAnswerId() > questionAnswer.size()) {
                        // 0 이거나 엑셀에서 드래그로 자동완성 되는 경우, 데이터 유효성 체크에 걸리지 않아서 별개의 validation 추가
                        CloudReadinessUploadFail cloudReadinessUploadFail = new CloudReadinessUploadFail();
                        cloudReadinessUploadFail.setSheet(cra.getQuestionId() > 5 ? "Step 02_Technical Factors" : "Step 01_Business Factors");
                        cloudReadinessUploadFail.setServiceId(key);
                        cloudReadinessUploadFail.setQuestion("Q. " + cra.getQuestionId());
                        cloudReadinessUploadFail.setFailDetail("This answer is not valid. (Your answer : " + cra.getAnswerId() + ", Available values : 1 to " + questionAnswer.size() + ")");
                        validationList.add(cloudReadinessUploadFail);

                    } else {
                        cra.setAnswerId(questionAnswer.get(cra.getAnswerId().intValue() - 1).getAnswerId());
                    }
                }
            }
        }

        return surveAnsweryMap;
    }

    public void validateCloudReadiness(Map<Long, List<CloudReadinessAnswer>> surveyQustionAnswerMap, List<CloudReadinessUploadFail> validationList, Long projectId) {
        CloudReadinessUploadFail cloudReadinessUploadFail;
        List<Long> serviceIdList = new ArrayList<>();

        Survey survey = surveyRepository.findTopByOrderBySurveyIdDesc();
        for (Long serviceId : surveyQustionAnswerMap.keySet()) {
            List<CloudReadinessAnswer> answers = surveyQustionAnswerMap.get(serviceId);
            List<SurveyQuestion> surveyQuestionList = surveyQuestionRepository.findBySurveyId(survey.getSurveyId());

            // 서비스만 있고 Cloud-Readiness 값이 없으면 Skip 대상이 된다.
            if (isCloudReadinessSkip(answers)) {
                serviceIdList.add(serviceId);
                continue;
            }

            // 서비스 ID 확인
            ServiceMaster serviceMaster = serviceMasterRepository.findByProjectIdAndServiceId(projectId, serviceId);
            if (serviceMaster == null) {
                cloudReadinessUploadFail = new CloudReadinessUploadFail();
                cloudReadinessUploadFail.setSheet("Step 01_Business Factors,Step 02_Technical Factors");
                cloudReadinessUploadFail.setServiceId(serviceId);
                cloudReadinessUploadFail.setQuestion("-");
                cloudReadinessUploadFail.setFailDetail("Service ID(" + serviceId + ") does not exist.");
                validationList.add(cloudReadinessUploadFail);
            }

            // PCR-5522 서비스 내 스캔 완료된 서버가 하나도 없으면 설문을 저장할 수 없다.
            // 서비스 내 스캔 완료된 서버가 있는지 확인.
            if (serviceMaster != null) {
                List<ServerResponse> serverResponseList = serverService.getServers(serviceMaster.getProjectId(), serviceId, false);
                boolean hasCompletedScan = false;
                for (ServerResponse sr : serverResponseList) {
                    if (sr.getLastCompleteScan() != null) {
                        hasCompletedScan = true;
                        break;
                    }
                }

                if (!hasCompletedScan) {
                    cloudReadinessUploadFail = new CloudReadinessUploadFail();
                    cloudReadinessUploadFail.setSheet("Step 01_Business Factors,Step 02_Technical Factors");
                    cloudReadinessUploadFail.setServiceId(serviceId);
                    cloudReadinessUploadFail.setQuestion("-");
                    cloudReadinessUploadFail.setFailDetail("Scan completed servers do not exist in Service ID(" + serviceId + ").");
                    validationList.add(cloudReadinessUploadFail);
                }
            }

            // 완성된 설문인지에 대한 validation
            if (surveyQuestionList.size() != answers.size()) {
                cloudReadinessUploadFail = new CloudReadinessUploadFail();
                cloudReadinessUploadFail.setSheet("Step 01_Business Factors,Step 02_Technical Factors");
                cloudReadinessUploadFail.setServiceId(serviceId);
                cloudReadinessUploadFail.setQuestion("-");
                cloudReadinessUploadFail.setFailDetail("Invalid Cloud-Readiness Excel data. The answer must equals the number of questions");
                validationList.add(cloudReadinessUploadFail);
            }

            for (CloudReadinessAnswer answer : answers) {
                if (answer.getAnswerId() == null) {
                    cloudReadinessUploadFail = new CloudReadinessUploadFail();
                    cloudReadinessUploadFail.setSheet(answer.getQuestionId() > 5 ? "Step 02_Technical Factors" : "Step 01_Business Factors");
                    cloudReadinessUploadFail.setServiceId(serviceId);
                    cloudReadinessUploadFail.setQuestion("Q." + answer.getQuestionId());
                    cloudReadinessUploadFail.setFailDetail("This answers cannot be empty.");
                    validationList.add(cloudReadinessUploadFail);
                }
            }
        }

        // Skip 대상 service Map 에서 제거
        for (Long serviceId : serviceIdList) {
            surveyQustionAnswerMap.remove(serviceId);
        }
    }

    public List<CloudReadinessUploadSuccess> uploadCloudReadiness(Map<Long, List<CloudReadinessAnswer>> surveyQustionAnswerMap) {
        List<CloudReadinessUploadSuccess> successList = new ArrayList<>();
        Survey survey = surveyRepository.findTopByOrderBySurveyIdDesc();
        Long surveyId = survey.getSurveyId();
        int newCount = 0;
        int updateCount = 0;
        for (Long serviceId : surveyQustionAnswerMap.keySet()) {
            List<CloudReadinessAnswer> answers = surveyQustionAnswerMap.get(serviceId);

            // 설문 진행 등록
            SurveyProcess surveyProcess = surveyProcessRepository.findBySurveyIdAndServiceId(surveyId, serviceId).orElse(new SurveyProcess());
            if (surveyProcess.getId() == null) {
                createNewSurveyProcess(surveyProcess, surveyId, serviceId);
                newCount++;
            } else {
                updateCount++;
                Instant instant = new Date().toInstant();
                surveyProcess.setModifyDatetime(instant);
            }

            // 기존 데이터 삭제
            surveyUserAnswerRepository.deleteAllBySurveyProcessId(surveyProcess.getId());

            // 설문 답변 등록
            List<SurveyUserAnswer> userAnswers = answers.stream().map(a -> {
                SurveyUserAnswerId id = makeSurveyUserAnswerId(surveyId, a, surveyProcess);
                SurveyUserAnswer userAnswer = new SurveyUserAnswer();
                userAnswer.setId(id);
                return userAnswer;
            }).collect(Collectors.toList());
            surveyUserAnswerRepository.saveAll(userAnswers);
            log.debug("answers : {}", userAnswers);
        }

        CloudReadinessUploadSuccess success = new CloudReadinessUploadSuccess();
        success.setSheet("Service");
        success.setNewCount(newCount);
        success.setUpdateCount(updateCount);
        success.setTotalCount(newCount + updateCount);
        successList.add(success);

        return successList;
    }

    /**
     * 서비스는 입력되저져 있으나, Answers가 모두 null인 경우에는 validation 하지 않고 Skip 대상으로 간주한다.
     */
    private boolean isCloudReadinessSkip(List<CloudReadinessAnswer> answers) {
        boolean isSkip = true;
        for (CloudReadinessAnswer answer : answers) {
            if (answer.getAnswerId() != null) {
                isSkip = false;
                break;
            }
        }

        return isSkip;
    }
}