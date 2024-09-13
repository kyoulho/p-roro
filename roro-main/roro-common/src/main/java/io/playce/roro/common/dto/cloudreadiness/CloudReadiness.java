package io.playce.roro.common.dto.cloudreadiness;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString
public class CloudReadiness {

    private long surveyProcessId;
    private long serviceId;
    private String serviceName;
    private boolean hasCompletedScan;
    private Date registDatetime;
    private Date modifyDatetime;
    private String excelFileName;
    private String excelFilePath;
    private String pptFileName;
    private String pptFilePath;
    private String surveyProcessResultCode;
    private String surveyResult;

    @JsonIgnore
    private float businessScore;
    @JsonIgnore
    private float technicalScore;

}
