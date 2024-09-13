package io.playce.roro.common.dto.migration;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString
public class MigrationJobDto {

    private Long inventoryProcessId;
    private String inventoryName;
    private String serverIp;
    private String credentialTypeName;
    private String credentialTypeCode;
    private String instanceName;
    private String publicIp;
    private String privateIp;
    private Integer estimateTime;
    private Long elapsedTime;
    private Double progress;
    private Date inventoryProcessStartDatetime;
    private Date inventoryProcessEndDatetime;
    private String inventoryProcessResultCode;
    private String inventoryProcessResultTxt;

}
