package io.playce.roro.jpa.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "survey_process")
public class SurveyProcess {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SURVEY_PROCESS_ID", nullable = false)
    private Long id;

    @Column(name = "SURVEY_ID", nullable = false)
    private Long surveyId;

    @Column(name = "SERVICE_ID")
    private Long serviceId;

    @Column(name = "PPT_FILE_NAME", length = 512)
    private String pptFileName;

    @Column(name = "PPT_FILE_PATH", length = 1024)
    private String pptFilePath;

    @Column(name = "EXCEL_FILE_NAME", length = 512)
    private String excelFileName;

    @Column(name = "EXCEL_FILE_PATH", length = 1024)
    private String excelFilePath;

    @Column(name = "SURVEY_PROCESS_RESULT_CODE", length = 20)
    private String surveyProcessResultCode;

    @Column(name = "REGIST_USER_ID", nullable = false)
    private Long registUserId;

    @Column(name = "REGIST_DATETIME", nullable = false)
    private Instant registDatetime;

    @Column(name = "MODIFY_USER_ID", nullable = false)
    private Long modifyUserId;

    @Column(name = "MODIFY_DATETIME", nullable = false)
    private Instant modifyDatetime;

}