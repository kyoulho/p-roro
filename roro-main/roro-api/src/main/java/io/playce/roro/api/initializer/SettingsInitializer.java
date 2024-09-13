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
 * SangCheon Park   Aug 26, 2022		    First Draft.
 */
package io.playce.roro.api.initializer;

import io.playce.roro.common.property.CommonProperties;
import io.playce.roro.common.setting.SettingsHandler;
import io.playce.roro.common.util.GeneralCipherUtil;
import io.playce.roro.jpa.entity.Setting;
import io.playce.roro.jpa.repository.SettingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

import static io.playce.roro.common.setting.SettingsConstants.*;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
@DependsOn({"commonProperties"})
public class SettingsInitializer implements InitializingBean {

    private final SettingRepository settingRepository;

    private final String topCategoryNameGeneral = "General";
    private final String topCategoryNameAssessment = "Assessment";
    private final String topCategoryNameMigration = "Migration";

    @Override
    public void afterPropertiesSet() throws Exception {
        init();
    }

    private void init() {
        log.info("Initialize for Settings properties.");

        List<Setting> settings = settingRepository.findAll();

        if (CollectionUtils.isEmpty(settings)) {
            initSettingInsert();
        } else {
            if (!isTopCategory(settings, topCategoryNameGeneral) || !isTopCategory(settings, topCategoryNameAssessment) || !isTopCategory(settings, topCategoryNameMigration)) {
                log.debug("delete all setting config and init data.");
                settingRepository.deleteAll();
                initSettingInsert();
            } else {
                initSettingsMap(settings);
            }
        }

        // DB에 Settings 값이 없다면 초기 값으로 DB에 저장
        // CommonProperties.getProperty(RORO_WEB_TERMINAL) 를 사용하면 VM Option으로 주어진 값을 조회할 수 있다.

        // SettingHandler 에 값 등록
//        SettingsHandler.setSettingsValue("", "");
        // SettingsHandler.setSettingsValue(RORO_WEB_TERMINAL, CommonProperties.getProperty(RORO_WEB_TERMINAL));
        // SettingsHandler.setSettingsValue(ENABLE_MONITORING_SCHEDULE, CommonProperties.getProperty(ENABLE_MONITORING_SCHEDULE));
        // SettingsHandler.setSettingsValue(RORO_SSH_IP_ADDRESS, CommonProperties.getProperty(RORO_SSH_IP_ADDRESS));
        // SettingsHandler.setSettingsValue(RORO_SSH_PORT, CommonProperties.getProperty(RORO_SSH_PORT));
        // SettingsHandler.setSettingsValue(RORO_SSH_USER_NAME, CommonProperties.getProperty(RORO_SSH_USER_NAME));
        // SettingsHandler.setSettingsValue(RORO_SSH_PASSWORD, CommonProperties.getProperty(RORO_SSH_PASSWORD));
        // SettingsHandler.setSettingsValue(RORO_SSH_PPK_FILE_PATH, CommonProperties.getProperty(RORO_SSH_PPK_FILE_PATH));
        // SettingsHandler.setSettingsValue(ENABLE_SCHEDULED_SCAN, CommonProperties.getProperty(ENABLE_SCHEDULED_SCAN));
        // SettingsHandler.setSettingsValue(RORO_MIDDLEWARE_AUTO_SCAN, CommonProperties.getProperty(RORO_MIDDLEWARE_AUTO_SCAN));
        // SettingsHandler.setSettingsValue(RORO_APPLICATION_AUTO_SCAN, CommonProperties.getProperty(RORO_APPLICATION_AUTO_SCAN));
        // SettingsHandler.setSettingsValue(APPSCAN_FILE_EXTENSIONS, CommonProperties.getProperty(APPSCAN_FILE_EXTENSIONS));
        // SettingsHandler.setSettingsValue(APPSCAN_EXCLUDE_FILENAMES, CommonProperties.getProperty(APPSCAN_EXCLUDE_FILENAMES));
    }

    private void initSettingsMap(List<Setting> settings) {
        for (Setting setting : settings) {
            if (StringUtils.isNotEmpty(setting.getPropertyName())) {
                SettingsHandler.setSettingsValue(setting.getPropertyName(), setting.getPropertyValue());
            }
        }
    }

    @SuppressWarnings("DuplicatedCode")
    private void initSettingInsert() {
        final String subCategoryNameMigration = "Migration";
        final String subCategoryNameWebTerminal = "Web Terminal";
        final String subCategoryNameMonitoring = "Monitoring";
        final String subCategoryNameWinApplication = "SSH Connection for Windows Application";
        final String subCategoryNameSshConnection = "SSH Connection";
        final String subCategoryNameScheduledScan = "Scheduled Scan";
        final String subCategoryNameAutoScan = "Auto Scan";
        final String subCategoryNameAssessmentApplication = "Application Scan";
        final String subCategoryNameWindowsScan = "Windows Scan";

        final String dataTypeString = "String";
        final String dataTypePassword = "Password";
        final String dataTypeBoolean = "Boolean";
        final String dataTypeInteger = "Integer";

        final String dataValuesBoolean = "true,false";

        // 영문 Property Alias
        final String webTerminalAliasEng = "Use Web Terminal";
        final String monitoringScheduleAliasEng = "Use Monitoring Schedule";
        final String sshIpAddressAliasEng = "RoRo Server IP Address";
        final String sshPortAliasEng = "RoRo Server SSH Port";
        final String sshUserNameAliasEng = "RoRo Server User Account";
        final String sshPasswordAliasEng = "RoRo Server Password";
        final String sshPpkFilePathAliasEng = "RoRo Server Private Key File Path";
        final String sshConnectionTimeOutAliasEng = "SSH Session Timeout(Seconds)";
        final String sshUseBouncyCastleProviderAliasEng = "Use BouncyCastleProvider";
        final String sshSkipMessagesAliasEng = "Filtering System Messages in SSH commands";
        final String enableScheduledScanAliasEng = "Use Scheduled Scan";
        final String roroMiddlewareAutoScanAliasEng = "Newly registered Middleware Auto Scan";
        final String roroMiddlewareAutoScanAfterServerScanAliasEng = "Middleware Auto Scan after server scan";
        final String roroApplicationAutoScanAliasEng = "Application Auto Scan";
        final String appScanFileExtensionsAliasEng = "Application scan target extensions";
        final String appScanCopyOnlyMatchedExtensionsAliasEng = "Download only application scan target extensions";
        final String appScanExcludeFilenamesAliasEng = "Excluded file from the application scan";
        final String appScanExcludeDomainsAliasEng = "Excluded domain from the application scan";
        final String appScanRemoveFilesAfterScanAliasEng = "Delete the download file after the application scan";
        final String appScanCopyIgnoreFilenamesAliasEng = "Files to exclude when downloading applications";
        final String windowsPowershellUseOutfileAliasEng = "Save Windows Server Scan Results File";
        final String roroMigrationEnabledAliasEng = "Use Migration";
        final String roroMigrationDirRemoveAliasEng = "Delete the server copy after Migration";
        final String roroMigrationBucketNameAliasEng = "Bucket Name ";
        final String roroMigrationBucketRegionAliasEng = "Bucket Region ";
        final String roroMigrationBucketRemoveAliasEng = "Delete bucket after Migration ";
        final String roroMigrationIncludeSystemUidAliasEng = "Show System User/Group in Pre-Configuration";

        // 국문 Property Alias
        final String webTerminalAliasKor = "웹 터미널 사용";
        final String monitoringScheduleAliasKor = "모니터링 일정 사용";
        final String sshIpAddressAliasKor = "RoRo 서버 IP 주소";
        final String sshPortAliasKor = "RoRo 서버 SSH 포트";
        final String sshUserNameAliasKor = "RoRo 서버 사용자 계정";
        final String sshPasswordAliasKor = "RoRo 서버 비밀번호";
        final String sshPpkFilePathAliasKor = "RoRo 서버 프라이빗 키 파일 경로";
        final String sshConnectionTimeOutAliasKor = "SSH 세션 타임아웃(초)";
        final String sshUseBouncyCastleProviderAliasKor = "BouncyCastleProvider 사용";
        final String sshSkipMessagesAliasKor = "SSH 명령 실행 시 시스템 메시지 필터링";
        final String enableScheduledScanAliasKor = "정기 검사 사용";
        final String roroMiddlewareAutoScanAliasKor = "신규 등록 미들웨어 자동 검사";
        final String roroMiddlewareAutoScanAfterServerScanAliasKor = "서버 검사 후 미들웨어 자동 검사";
        final String roroApplicationAutoScanAliasKor = "애플리케이션 자동 검사";
        final String appScanFileExtensionsAliasKor = "애플리케이션 검사 대상 확장자";
        final String appScanCopyOnlyMatchedExtensionsAliasKor = "애플리케이션 검사 대상 확장자 파일만 다운로드";
        final String appScanExcludeFilenamesAliasKor = "애플리케이션 검사 제외 파일 이름";
        final String appScanExcludeDomainsAliasKor = "애플리케이션 검사 제외 도메인";
        final String appScanRemoveFilesAfterScanAliasKor = "애플리케이션 검사 후 다운로드 파일 삭제";
        final String appScanCopyIgnoreFilenamesAliasKor = "애플리케이션 다운로드 제외 파일 이름";
        final String windowsPowershellUseOutfileAliasKor = "Windows 서버 검사 결과 파일 저장";
        final String roroMigrationEnabledAliasKor = "마이그레이션 사용";
        final String roroMigrationDirRemoveAliasKor = "마이그레이션 후 서버 복사본 삭제";
        final String roroMigrationBucketNameAliasKor = "버킷 이름";
        final String roroMigrationBucketRegionAliasKor = "버킷 리전";
        final String roroMigrationBucketRemoveAliasKor = "마이그레이션 완료 후 버킷 삭제";
        final String roroMigrationIncludeSystemUidAliasKor = "사전 구성 시 System User/Group 표시";

        // 영문 Placeholder
        final String sshIpAddressPlaceholderEng = "e.g. 0.0.0.0";
        final String sshPortPlaceholderEng = "Enter Port Number";
        final String sshUserNamePlaceholderEng = "Enter User Account";
        final String sshPasswordPlaceholderEng = "Enter Password";
        final String sshPpkFilePathPlaceholderEng = "Enter File Path";
        final String sshConnectionTimeOutPlaceholderEng = "Enter SSH connection Timeout(seconds)";
        final String sshUseBouncyCastleProviderPlaceholderEng = "Use BouncyCastleProvider as first(Use when DH key size is not multiple of 64)";
        final String sshSkipMessagesPlaceholderEng = "Enter the keywords to exclude them from the SSH command output(comma-separated)";
        final String appScanFileExtensionsPlaceholderEng = "Enter comma-separated File Extensions";
        final String appScanExcludeFileNamesPlaceholderEng = "Enter last part of file name to exclude(comma-separated)";
        final String appScanExcludeDomainsPlaceholderEng = "Enter IP or Domains to exclude(comma-separated)";
        final String appScanRemoveFilesAfterScanPlaceholderEng = "Used to delete a downloaded file after scanning an application";
        final String appScanCopyIgnoreFilenamesPlaceholderEng = "Enter file name to exclude when downloading the application(comma-separated)";
        final String windowsPowershellUseOutfilePlaceholderEng = "Use when Windows scan result has garbled characters";
        final String roroMigrationBucketNameEng = "Enter bucket name";
        final String roroMigrationBucketRegionEng = "Enter bucket region";

        // 국문 Placeholder
        final String sshIpAddressPlaceholderKor = "예. 0.0.0.0";
        final String sshPortPlaceholderKor = "포트 번호 입력";
        final String sshUserNamePlaceholderKor = "사용자 계정 입력";
        final String sshPasswordPlaceholderKor = "비밀번호 입력";
        final String sshPpkFilePathPlaceholderKor = "파일 경로 입력";
        final String sshConnectionTimeOutPlaceholderKor = "SSH 연결 제한 시간 입력(초)";
        final String sshUseBouncyCastleProviderPlaceholderKor = "\"DH key size is not multiple of 64\" 오류가 발생하는 경우 BouncyCastleProvider 사용";
        final String sshSkipMessagesPlaceholderKor = "SSH 명령 실행 결과에 제외할 키워드 입력(쉼표로 구분)";
        final String appScanFileExtensionsPlaceholderKor = "파일 확장자를 쉼표로 구분하여 입력";
        final String appScanExcludeFileNamesPlaceholderKor = "제외할 파일 이름의 마지막 부분 입력(쉼표로 구분)";
        final String appScanExcludeDomainsPlaceholderKor = "제외할 IP 또는 도메인 입력(쉼표로 구분)";
        final String appScanRemoveFilesAfterScanPlaceholderKor = "애플리케이션 검사 후 다운로드한 파일을 삭제하는 경우 사용";
        final String appScanCopyIgnoreFilenamesPlaceholderKor = "애플리케이션 다운로드 시 제외할 파일 이름 입력(쉼표로 구분)";
        final String windowsPowershellUseOutfilePlaceholderKor = "Windows 검사 결과에 한글이 깨지는 경우 사용";
        final String roroMigrationBucketNameKor = "버킷 이름 입력";
        final String roroMigrationBucketRegionKor = "버킷 리전 입력";

        // 영문 Tooltip
        final String webTerminalTooltipEng = "Select whether to use the RoRo server's web terminal. If you select 'Yes', you can access the terminal from the server menu under the inventory. Currently not supported on Windows. Optional - Yes(default), No";
        final String monitoringScheduleTooltipEng = "Select whether to use the monitoring schedule function for all servers registered in the inventory. If you select 'Yes', you can enable/disable the monitoring function for each server. Optional - Yes, No(default)";
        final String sshIpAddressTooltipEng = "Specifies the IP address of the RoRo server.";
        final String sshPortTooltipEng = "Specifies the SSH port number of the RoRo server. Default is 22.";
        final String sshUserNameTooltipEng = "Specifies the User Account of the RoRo server.";
        final String sshPasswordTooltipEng = "Specifies the User Password of the RoRo server.";
        final String sshPpkFilePathTooltipEng = "Specifies the Private Key File Path in ppk format of RoRo server in Windows server.";
        final String sshConnectionTimeOutTooltipEng = "Set the SSH Session Timeout value. Default is 10 seconds.";
        final String sshUseBouncyCastleProviderTooltipEng = "Choose whether or not to use BouncyCastleProvider if you get a \"DH key size is not multiple of 64\" error. Optional - Yes, No(default)";
        final String sshSkipMessagesTooltipEng = "Enter the keywords of the lines you want to exclude from the SSH execution result, separated by commas. Any lines containing the entered keywords will be excluded.";
        final String enableScheduledScanTooltipEng = "Select whether to use the Scheduled Scan for all servers registered in the inventory. If you select 'Yes', you can enable/disable the scheduled scan function for each server. Optional - Yes, No(default)";
        final String roroMiddlewareAutoScanTooltipEng = "Select whether to use the Auto Scan for newly registered middleware. Optional - Yes(default), No";
        final String roroMiddlewareAutoScanAfterServerScanTooltipEng = "When the server scan is complete, select whether to use the Auto Scan for middleware in the server. Optional - Yes(default), No";
        final String roroApplicationAutoScanTooltipEng = "Select whether or not to use the Auto Scan for applications registered by the user. Optional - Yes(default), No";
        final String appScanFileExtensionsTooltipEng = "Enter file extensions separated by commas that should be included in the application scan. Default - java,class,jsp,jspx,html,htm,js,properties,sql,xml,xmi,yml,yaml,json,sh,bat,scala,groovy,c,pc,cpp";
        final String appScanCopyOnlyMatchedExtensionsTooltipEng = "Choose whether or not to download only files with extensions specified in 'Download only application scan target extensions'. (Not supported on Windows) Optional - Yes, No(default)";
        final String appScanExcludeFileNamesTooltipEng = "Enter file names to be excluded from application scanning, separated by commas. Default - .chunk.js,.min.js";
        final String appScanExcludeDomainsTooltipEng = "Enter IP or Domains to be excluded from application scanning, separated by commas. Default - apache.org,w3.org,mvnrepository.com,springframework.org,springmodules.org,mybatis.org,egovframe.go.kr,egovframework.gov,java.sun.com,jcp.org,npmjs.org,yarnpkg.com,mozilla.org";
        final String appScanRemoveFilesAfterScanTooltipEng = "Choose whether or not to delete downloaded files after scanning the application. Optional - Yes(default), No";
        final String appScanCopyIgnoreFilenamesTooltipEng = "Enter the file names to be excluded when downloading the application, separated by commas.";
        final String windowsPowershellUseOutfileTooltipEng = "Used when Korean characters are broken in the Windows scan result. Optional - Yes, No(default)";
        final String roroMigrationEnabledTooltipEng = "Choose whether or not to use the Migration. Optional - Yes, No(default)";
        final String roroMigrationDirRemoveTooltipEng = "Choose whether to delete the server copy after migration. Optional - Yes(default), No";
        final String roroMigrationBucketRegionTooltipEng = "Enter the Bucket Name to be used for rehost migration.";
        final String roroMigrationBucketNameTooltipEng = "Enter the Bucket Region to be used for rehost migration.";
        final String roroMigrationBucketRemoveTooltipEng = "Choose whether to delete the bucket after rehost migration. Optional - Yes(default), No";
        final String roroMigrationIncludeSystemUidTooltipEng = "Choose whether or not to display System User / Group during Preconfiguration for Replatform migration. Optional - Yes, No(default)";

        // 국문 Tooltip
        final String webTerminalTooltipKor = "RoRo 서버의 웹 터미널 사용 여부를 선택합니다. '예'를 선택하면, 인벤토리에 등록된 서버에서 터미널을 사용할 수 있습니다. 단, Windows 서버는 이 기능이 지원되지 않습니다. 선택 항목 - 예(기본값), 아니요";
        final String monitoringScheduleTooltipKor = "인벤토리에 등록된 서버의 모니터링 일정 사용 여부를 선택합니다. '예'를 선택하면, 서버 추가 설정 시에 모니터링 기능을 활성화/비활성화할 수 있습니다. 선택 항목 - 예, 아니요(기본값)";
        final String sshIpAddressTooltipKor = "RoRo 서버의 IP 주소를 지정합니다.";
        final String sshPortTooltipKor = "RoRo 서버의 SSH 포트 번호를 지정합니다. 기본값은 22입니다.";
        final String sshUserNameTooltipKor = "RoRo 서버의 사용자 계정을 입력합니다.";
        final String sshPasswordTooltipKor = "RoRo 서버의 사용자 비밀번호를 입력합니다.";
        final String sshPpkFilePathTooltipKor = "Windows 서버에 저장된 RoRo 서버의 프라이빗 키 파일 경로를 지정합니다.(ppk 확장자)";
        final String sshConnectionTimeOutTooltipKor = "SSH 세션 타임아웃 값을 설정합니다. 기본값은 10초 입니다.";
        final String sshUseBouncyCastleProviderTooltipKor = "\"DH key size is not multiple of 64\" 오류 발생 시 BouncyCastleProvider 사용 여부를 선택합니다. 선택 항목 - 예, 아니요(기본값)";
        final String sshSkipMessagesTooltipKor = "SSH 명령 실행 결과 중 시스템 메시지 등 제외해야 하는 메시지의 키워드를 등록하면, 해당 키워드가 포함된 시스템 메시지를 SSH 실행 결과에서 제외합니다. 제외할 키워드는 쉼표로 구분하여 입력합니다.";
        final String enableScheduledScanTooltipKor = "인벤토리에 등록된 서버의 정기 검사 사용 여부를 선택합니다. '예'를 선택하면, 서버 추가 설정 시에 정기 검사 기능을 활성화/비활성화할 수 있습니다. 선택 항목 - 예, 아니요(기본값)";
        final String roroMiddlewareAutoScanTooltipKor = "새로 추가한 미들웨어의 자동 검사 사용 여부를 선택합니다. 선택 항목 - 예(기본값), 아니요";
        final String roroMiddlewareAutoScanAfterServerScanTooltipKor = "서버 검사 완료 후 해당 서버에 설치된 미들웨어의 자동 검사 사용 여부를 선택합니다. 선택 항목 - 예(기본값), 아니요 ";
        final String roroApplicationAutoScanTooltipKor = "사용자가 직접 추가한 애플리케이션의 자동 검사 사용 여부를 선택합니다. 선택 항목 - 예(기본값), 아니요";
        final String appScanFileExtensionsTooltipKor = "애플리케이션 검사 시에 검사 대상 파일의 확장자를 쉼표로 구분하여 입력합니다. 기본값 - java,class,jsp,jspx,html,htm,js,properties,sql,xml,xmi,yml,yaml,json,sh,bat,scala,groovy,c,pc,cpp";
        final String appScanCopyOnlyMatchedExtensionsTooltipKor = "애플리케이션 검사 대상 확장자에서 설정한 확장자 파일만 다운로드할지를 선택합니다.(Windows에서는 지원되지 않음) 선택 항목 - 예, 아니요(기본값)";
        final String appScanExcludeFileNamesTooltipKor = "애플리케이션 검사에서 제외할 파일 이름을 쉼표로 구부하여 입력합니다. 기본값 - .chunk.js,.min.js";
        final String appScanExcludeDomainsTooltipKor = "애플리케이션 검사 시 제외할 IP 또는 도메인을 쉼표로 구분하여 입력합니다. 기본값 - apache.org,w3.org,mvnrepository.com,springframework.org,springmodules.org,mybatis.org,egovframe.go.kr,egovframework.gov,java.sun.com,jcp.org,npmjs.org,yarnpkg.com,mozilla.org";
        final String appScanRemoveFilesAfterScanTooltipKor = "애플리케이션 검사 후 다운로드한 파일의 삭제 여부를 선택합니다. 선택 항목 - 예(기본값), 아니요";
        final String appScanCopyIgnoreFilenamesTooltipKor = "애플리케이션 다운로드 시 제외할 파일 이름을 쉼표로 구분하여 입력합니다.";
        final String windowsPowershellUseOutfileTooltipKor = "Windows 검사 결과에서 한글이 깨지는 경우 사용합니다. 선택 항목 - 예, 아니요(기본값)";
        final String roroMigrationEnabledTooltipKor = "마이그레이션 기능 사용 여부를 선택합니다. 선택 항목 - 예, 아니요(기본값)";
        final String roroMigrationDirRemoveTooltipKor = "마이그레이션 후 서버 복사본의 삭제 여부를 선택합니다. 선택 항목 - 예(기본값), 아니요";
        final String roroMigrationBucketRegionTooltipKor = "Rehost 마이그레이션에서 사용할 버킷 이름을 입력합니다.";
        final String roroMigrationBucketNameTooltipKor = "Rehost 마이그레이션에서 사용할 버킷 리전을 입력합니다.";
        final String roroMigrationBucketRemoveTooltipKor = "Rehost 마이그레이션 후 버킷 삭제 여부를 선택합니다. 선택 항목 - 예(기본값), 아니요";
        final String roroMigrationIncludeSystemUidTooltipKor = "Replatform 마이그레이션을 위한 사전 환경 구성 시에 System User/Group 표시 여부를 선택합니다. 선택 항목 - 예(기본값), 아니요";

        Setting generalCategorySetting = settingRepository.save(getCategorySetting(topCategoryNameGeneral));
        Setting assessmentCategorySetting = settingRepository.save(getCategorySetting(topCategoryNameAssessment));
        Setting migrationCategorySetting = settingRepository.save(getCategorySetting(topCategoryNameMigration));

        //General
        settingRepository.save(
                getPropertySetting(generalCategorySetting.getSettingId(), subCategoryNameWebTerminal, RORO_WEB_TERMINAL,
                        webTerminalAliasEng, webTerminalAliasKor,
                        CommonProperties.getProperty(RORO_WEB_TERMINAL), "N",
                        dataTypeBoolean, dataValuesBoolean, "", "",
                        webTerminalTooltipEng, webTerminalTooltipKor, 1L));
        SettingsHandler.setSettingsValue(RORO_WEB_TERMINAL, CommonProperties.getProperty(RORO_WEB_TERMINAL));

        settingRepository.save(
                getPropertySetting(generalCategorySetting.getSettingId(), subCategoryNameMonitoring, ENABLE_MONITORING_SCHEDULE,
                        monitoringScheduleAliasEng, monitoringScheduleAliasKor,
                        CommonProperties.getProperty(ENABLE_MONITORING_SCHEDULE), "N",
                        dataTypeBoolean, dataValuesBoolean, "", "",
                        monitoringScheduleTooltipEng, monitoringScheduleTooltipKor, 1L));
        SettingsHandler.setSettingsValue(ENABLE_MONITORING_SCHEDULE, CommonProperties.getProperty(ENABLE_MONITORING_SCHEDULE));

        settingRepository.save(
                getPropertySetting(generalCategorySetting.getSettingId(), subCategoryNameWinApplication, RORO_SSH_IP_ADDRESS,
                        sshIpAddressAliasEng, sshIpAddressAliasKor,
                        CommonProperties.getProperty(RORO_SSH_IP_ADDRESS), "N",
                        dataTypeString, null, sshIpAddressPlaceholderEng, sshIpAddressPlaceholderKor,
                        sshIpAddressTooltipEng, sshIpAddressTooltipKor, 1L));
        SettingsHandler.setSettingsValue(RORO_SSH_IP_ADDRESS, CommonProperties.getProperty(RORO_SSH_IP_ADDRESS));

        settingRepository.save(
                getPropertySetting(generalCategorySetting.getSettingId(), subCategoryNameWinApplication, RORO_SSH_PORT,
                        sshPortAliasEng, sshPortAliasKor,
                        CommonProperties.getProperty(RORO_SSH_PORT), "N",
                        dataTypeInteger, null, sshPortPlaceholderEng, sshPortPlaceholderKor,
                        sshPortTooltipEng, sshPortTooltipKor, 2L));
        SettingsHandler.setSettingsValue(RORO_SSH_PORT, CommonProperties.getProperty(RORO_SSH_PORT));

        settingRepository.save(
                getPropertySetting(generalCategorySetting.getSettingId(), subCategoryNameWinApplication, RORO_SSH_USER_NAME,
                        sshUserNameAliasEng, sshUserNameAliasKor,
                        CommonProperties.getProperty(RORO_SSH_USER_NAME), "N",
                        dataTypeString, null, sshUserNamePlaceholderEng, sshUserNamePlaceholderKor,
                        sshUserNameTooltipEng, sshUserNameTooltipKor, 3L));
        SettingsHandler.setSettingsValue(RORO_SSH_USER_NAME, CommonProperties.getProperty(RORO_SSH_USER_NAME));

        settingRepository.save(
                getPropertySetting(generalCategorySetting.getSettingId(), subCategoryNameWinApplication, RORO_SSH_PASSWORD,
                        sshPasswordAliasEng, sshPasswordAliasKor,
                        GeneralCipherUtil.encrypt(CommonProperties.getProperty(RORO_SSH_PASSWORD)), "N",
                        dataTypePassword, null, sshPasswordPlaceholderEng, sshPasswordPlaceholderKor,
                        sshPasswordTooltipEng, sshPasswordTooltipKor, 4L));
        SettingsHandler.setSettingsValue(RORO_SSH_PASSWORD, GeneralCipherUtil.encrypt(CommonProperties.getProperty(RORO_SSH_PASSWORD)));

        settingRepository.save(
                getPropertySetting(generalCategorySetting.getSettingId(), subCategoryNameWinApplication, RORO_SSH_PPK_FILE_PATH,
                        sshPpkFilePathAliasEng, sshPpkFilePathAliasKor,
                        CommonProperties.getProperty(RORO_SSH_PPK_FILE_PATH), "N",
                        dataTypeString, null, sshPpkFilePathPlaceholderEng, sshPpkFilePathPlaceholderKor,
                        sshPpkFilePathTooltipEng, sshPpkFilePathTooltipKor, 5L));
        SettingsHandler.setSettingsValue(RORO_SSH_PPK_FILE_PATH, CommonProperties.getProperty(RORO_SSH_PPK_FILE_PATH));

        settingRepository.save(
                getPropertySetting(generalCategorySetting.getSettingId(), subCategoryNameSshConnection, RORO_SSH_CONNECT_TIMEOUT,
                        sshConnectionTimeOutAliasEng, sshConnectionTimeOutAliasKor,
                        10 + "", "N",
                        dataTypeInteger, null, sshConnectionTimeOutPlaceholderEng, sshConnectionTimeOutPlaceholderKor,
                        sshConnectionTimeOutTooltipEng, sshConnectionTimeOutTooltipKor, 1L));
        SettingsHandler.setSettingsValue(RORO_SSH_CONNECT_TIMEOUT, CommonProperties.getProperty(RORO_SSH_CONNECT_TIMEOUT));

        settingRepository.save(
                getPropertySetting(generalCategorySetting.getSettingId(), subCategoryNameSshConnection, RORO_SSH_USE_BOUNCY_CASTLE_PROVIDER,
                        sshUseBouncyCastleProviderAliasEng, sshUseBouncyCastleProviderAliasKor,
                        "false", "N",
                        dataTypeBoolean, dataValuesBoolean, sshUseBouncyCastleProviderPlaceholderEng, sshUseBouncyCastleProviderPlaceholderKor,
                        sshUseBouncyCastleProviderTooltipEng, sshUseBouncyCastleProviderTooltipKor, 2L));
        SettingsHandler.setSettingsValue(RORO_SSH_USE_BOUNCY_CASTLE_PROVIDER, CommonProperties.getProperty(RORO_SSH_USE_BOUNCY_CASTLE_PROVIDER));

        settingRepository.save(
                getPropertySetting(generalCategorySetting.getSettingId(), subCategoryNameSshConnection, RORO_SSH_SKIP_MESSAGE,
                        sshSkipMessagesAliasEng, sshSkipMessagesAliasKor,
                        "", "N",
                        dataTypeString, null, sshSkipMessagesPlaceholderEng, sshSkipMessagesPlaceholderKor,
                        sshSkipMessagesTooltipEng, sshSkipMessagesTooltipKor, 3L));
        SettingsHandler.setSettingsValue(RORO_SSH_SKIP_MESSAGE, CommonProperties.getProperty(RORO_SSH_SKIP_MESSAGE));

        // Assessment
        settingRepository.save(
                getPropertySetting(assessmentCategorySetting.getSettingId(), subCategoryNameScheduledScan, ENABLE_SCHEDULED_SCAN,
                        enableScheduledScanAliasEng, enableScheduledScanAliasKor,
                        CommonProperties.getProperty(ENABLE_SCHEDULED_SCAN), "N",
                        dataTypeBoolean, dataValuesBoolean, "", "",
                        enableScheduledScanTooltipEng, enableScheduledScanTooltipKor, 1L));
        SettingsHandler.setSettingsValue(ENABLE_SCHEDULED_SCAN, CommonProperties.getProperty(ENABLE_SCHEDULED_SCAN));

        settingRepository.save(
                getPropertySetting(assessmentCategorySetting.getSettingId(), subCategoryNameAutoScan, RORO_MIDDLEWARE_AUTO_SCAN,
                        roroMiddlewareAutoScanAliasEng, roroMiddlewareAutoScanAliasKor,
                        CommonProperties.getProperty(RORO_MIDDLEWARE_AUTO_SCAN), "N",
                        dataTypeBoolean, dataValuesBoolean, "", "",
                        roroMiddlewareAutoScanTooltipEng, roroMiddlewareAutoScanTooltipKor, 1L));
        SettingsHandler.setSettingsValue(RORO_MIDDLEWARE_AUTO_SCAN, CommonProperties.getProperty(RORO_MIDDLEWARE_AUTO_SCAN));

        settingRepository.save(
                getPropertySetting(assessmentCategorySetting.getSettingId(), subCategoryNameAutoScan, RORO_MIDDLEWARE_AUTO_SCAN_AFTER_SERVER_SCAN,
                        roroMiddlewareAutoScanAfterServerScanAliasEng, roroMiddlewareAutoScanAfterServerScanAliasKor,
                        CommonProperties.getProperty(RORO_MIDDLEWARE_AUTO_SCAN_AFTER_SERVER_SCAN, "true"), "N",
                        dataTypeBoolean, dataValuesBoolean, "", "",
                        roroMiddlewareAutoScanAfterServerScanTooltipEng, roroMiddlewareAutoScanAfterServerScanTooltipKor, 2L));
        SettingsHandler.setSettingsValue(RORO_MIDDLEWARE_AUTO_SCAN_AFTER_SERVER_SCAN, CommonProperties.getProperty(RORO_MIDDLEWARE_AUTO_SCAN_AFTER_SERVER_SCAN, "true"));

        settingRepository.save(
                getPropertySetting(assessmentCategorySetting.getSettingId(), subCategoryNameAutoScan, RORO_APPLICATION_AUTO_SCAN,
                        roroApplicationAutoScanAliasEng, roroApplicationAutoScanAliasKor,
                        CommonProperties.getProperty(RORO_APPLICATION_AUTO_SCAN), "N",
                        dataTypeBoolean, dataValuesBoolean, "", "",
                        roroApplicationAutoScanTooltipEng, roroApplicationAutoScanTooltipKor, 3L));
        SettingsHandler.setSettingsValue(RORO_APPLICATION_AUTO_SCAN, CommonProperties.getProperty(RORO_APPLICATION_AUTO_SCAN));

        settingRepository.save(
                getPropertySetting(assessmentCategorySetting.getSettingId(), subCategoryNameAssessmentApplication, APPSCAN_FILE_EXTENSIONS,
                        appScanFileExtensionsAliasEng, appScanFileExtensionsAliasKor,
                        CommonProperties.getProperty(APPSCAN_FILE_EXTENSIONS), "N",
                        dataTypeString, null, appScanFileExtensionsPlaceholderEng, appScanFileExtensionsPlaceholderKor,
                        appScanFileExtensionsTooltipEng, appScanFileExtensionsTooltipKor, 1L));
        SettingsHandler.setSettingsValue(APPSCAN_FILE_EXTENSIONS, CommonProperties.getProperty(APPSCAN_FILE_EXTENSIONS));

        settingRepository.save(
                getPropertySetting(assessmentCategorySetting.getSettingId(), subCategoryNameAssessmentApplication, APPSCAN_COPY_ONLY_MATCHED_EXTENSIONS,
                        appScanCopyOnlyMatchedExtensionsAliasEng, appScanCopyOnlyMatchedExtensionsAliasKor,
                        CommonProperties.getProperty(APPSCAN_COPY_ONLY_MATCHED_EXTENSIONS), "N",
                        dataTypeBoolean, dataValuesBoolean, "", "",
                        appScanCopyOnlyMatchedExtensionsTooltipEng, appScanCopyOnlyMatchedExtensionsTooltipKor, 2L));
        SettingsHandler.setSettingsValue(APPSCAN_COPY_ONLY_MATCHED_EXTENSIONS, CommonProperties.getProperty(APPSCAN_COPY_ONLY_MATCHED_EXTENSIONS));

        settingRepository.save(
                getPropertySetting(assessmentCategorySetting.getSettingId(), subCategoryNameAssessmentApplication, APPSCAN_EXCLUDE_FILENAMES,
                        appScanExcludeFilenamesAliasEng, appScanExcludeFilenamesAliasKor,
                        CommonProperties.getProperty(APPSCAN_EXCLUDE_FILENAMES), "N",
                        dataTypeString, null, appScanExcludeFileNamesPlaceholderEng, appScanExcludeFileNamesPlaceholderKor,
                        appScanExcludeFileNamesTooltipEng, appScanExcludeFileNamesTooltipKor, 3L));
        SettingsHandler.setSettingsValue(APPSCAN_EXCLUDE_FILENAMES, CommonProperties.getProperty(APPSCAN_EXCLUDE_FILENAMES));

        settingRepository.save(
                getPropertySetting(assessmentCategorySetting.getSettingId(), subCategoryNameAssessmentApplication, APPSCAN_EXCLUDE_DOMAINS,
                        appScanExcludeDomainsAliasEng, appScanExcludeDomainsAliasKor,
                        CommonProperties.getProperty(APPSCAN_EXCLUDE_DOMAINS), "N",
                        dataTypeString, null, appScanExcludeDomainsPlaceholderEng, appScanExcludeDomainsPlaceholderKor,
                        appScanExcludeDomainsTooltipEng, appScanExcludeDomainsTooltipKor, 4L));
        SettingsHandler.setSettingsValue(APPSCAN_EXCLUDE_DOMAINS, CommonProperties.getProperty(APPSCAN_EXCLUDE_DOMAINS));

        settingRepository.save(
                getPropertySetting(assessmentCategorySetting.getSettingId(), subCategoryNameAssessmentApplication, APPSCAN_REMOVE_FILES_AFTER_SCAN,
                        appScanRemoveFilesAfterScanAliasEng, appScanRemoveFilesAfterScanAliasKor,
                        CommonProperties.getProperty(APPSCAN_REMOVE_FILES_AFTER_SCAN), "N",
                        dataTypeBoolean, dataValuesBoolean, appScanRemoveFilesAfterScanPlaceholderEng, appScanRemoveFilesAfterScanPlaceholderKor,
                        appScanRemoveFilesAfterScanTooltipEng, appScanRemoveFilesAfterScanTooltipKor, 5L));
        SettingsHandler.setSettingsValue(APPSCAN_REMOVE_FILES_AFTER_SCAN, CommonProperties.getProperty(APPSCAN_REMOVE_FILES_AFTER_SCAN));

        settingRepository.save(
                getPropertySetting(assessmentCategorySetting.getSettingId(), subCategoryNameAssessmentApplication, APPSCAN_COPY_IGNORE_FILENAMES,
                        appScanCopyIgnoreFilenamesAliasEng, appScanCopyIgnoreFilenamesAliasKor,
                        "", "N",
                        dataTypeString, null, appScanCopyIgnoreFilenamesPlaceholderEng, appScanCopyIgnoreFilenamesPlaceholderKor,
                        appScanCopyIgnoreFilenamesTooltipEng, appScanCopyIgnoreFilenamesTooltipKor, 6L));
        SettingsHandler.setSettingsValue(APPSCAN_COPY_IGNORE_FILENAMES, CommonProperties.getProperty(APPSCAN_COPY_IGNORE_FILENAMES));

        settingRepository.save(
                getPropertySetting(assessmentCategorySetting.getSettingId(), subCategoryNameWindowsScan, WINDOWS_POWERSHELL_USE_OUTFILE,
                        windowsPowershellUseOutfileAliasEng, windowsPowershellUseOutfileAliasKor,
                        "false", "N",
                        dataTypeBoolean, dataValuesBoolean, windowsPowershellUseOutfilePlaceholderEng, windowsPowershellUseOutfilePlaceholderKor,
                        windowsPowershellUseOutfileTooltipEng, windowsPowershellUseOutfileTooltipKor, 1L));
        SettingsHandler.setSettingsValue(WINDOWS_POWERSHELL_USE_OUTFILE, CommonProperties.getProperty(WINDOWS_POWERSHELL_USE_OUTFILE));

        // Migration
        settingRepository.save(
                getPropertySetting(migrationCategorySetting.getSettingId(), subCategoryNameMigration, RORO_MIGRATION_ENABLED,
                        roroMigrationEnabledAliasEng, roroMigrationEnabledAliasKor,
                        "false", "N",
                        dataTypeBoolean, dataValuesBoolean, "", "",
                        roroMigrationEnabledTooltipEng, roroMigrationEnabledTooltipKor, 1L));
        SettingsHandler.setSettingsValue(RORO_MIGRATION_ENABLED, CommonProperties.getProperty(RORO_MIGRATION_ENABLED));

        settingRepository.save(
                getPropertySetting(migrationCategorySetting.getSettingId(), subCategoryNameMigration, RORO_MIGRATION_DIR_REMOVE,
                        roroMigrationDirRemoveAliasEng, roroMigrationDirRemoveAliasKor,
                        CommonProperties.getProperty(RORO_MIGRATION_DIR_REMOVE), "N",
                        dataTypeBoolean, dataValuesBoolean, "", "",
                        roroMigrationDirRemoveTooltipEng, roroMigrationDirRemoveTooltipKor, 2L));
        SettingsHandler.setSettingsValue(RORO_MIGRATION_DIR_REMOVE, CommonProperties.getProperty(RORO_MIGRATION_DIR_REMOVE));

        settingRepository.save(
                getPropertySetting(migrationCategorySetting.getSettingId(), subCategoryNameMigration, RORO_MIGRATION_BUCKET_NAME,
                        roroMigrationBucketNameAliasEng, roroMigrationBucketNameAliasKor,
                        CommonProperties.getProperty(RORO_MIGRATION_BUCKET_NAME), "N",
                        dataTypeString, null, roroMigrationBucketNameEng, roroMigrationBucketNameKor,
                        roroMigrationBucketNameTooltipEng, roroMigrationBucketNameTooltipKor, 3L));
        SettingsHandler.setSettingsValue(RORO_MIGRATION_BUCKET_NAME, CommonProperties.getProperty(RORO_MIGRATION_BUCKET_NAME));

        settingRepository.save(
                getPropertySetting(migrationCategorySetting.getSettingId(), subCategoryNameMigration, RORO_MIGRATION_BUCKET_REGION,
                        roroMigrationBucketRegionAliasEng, roroMigrationBucketRegionAliasKor,
                        CommonProperties.getProperty(RORO_MIGRATION_BUCKET_REGION), "N",
                        dataTypeString, null, roroMigrationBucketRegionEng, roroMigrationBucketRegionKor,
                        roroMigrationBucketRegionTooltipEng, roroMigrationBucketRegionTooltipKor, 4L));
        SettingsHandler.setSettingsValue(RORO_MIGRATION_BUCKET_REGION, CommonProperties.getProperty(RORO_MIGRATION_BUCKET_REGION));

        settingRepository.save(
                getPropertySetting(migrationCategorySetting.getSettingId(), subCategoryNameMigration, RORO_MIGRATION_BUCKET_REMOVE,
                        roroMigrationBucketRemoveAliasEng, roroMigrationBucketRemoveAliasKor,
                        CommonProperties.getProperty(RORO_MIGRATION_BUCKET_REMOVE), "N",
                        dataTypeBoolean, dataValuesBoolean, "", "",
                        roroMigrationBucketRemoveTooltipEng, roroMigrationBucketRemoveTooltipKor, 5L));
        SettingsHandler.setSettingsValue(RORO_MIGRATION_BUCKET_REMOVE, CommonProperties.getProperty(RORO_MIGRATION_BUCKET_REMOVE));

        settingRepository.save(
                getPropertySetting(migrationCategorySetting.getSettingId(), subCategoryNameMigration, RORO_MIGRATION_INCLUDE_SYSTEM_UID,
                        roroMigrationIncludeSystemUidAliasEng, roroMigrationIncludeSystemUidAliasKor,
                        CommonProperties.getProperty(RORO_MIGRATION_INCLUDE_SYSTEM_UID, "false"), "N",
                        dataTypeBoolean, dataValuesBoolean, "", "",
                        roroMigrationIncludeSystemUidTooltipEng, roroMigrationIncludeSystemUidTooltipKor, 6L));
        SettingsHandler.setSettingsValue(RORO_MIGRATION_INCLUDE_SYSTEM_UID, CommonProperties.getProperty(RORO_MIGRATION_INCLUDE_SYSTEM_UID, "false"));
    }

    private boolean isTopCategory(List<Setting> settings, String categoryName) {
        for (Setting setting : settings) {
            if (setting.getCategoryName().equals(categoryName) && setting.getParentSettingId() == null) {
                return true;
            }
        }

        return false;
    }

    private Setting getCategorySetting(String categoryName) {
        Setting categorySetting = new Setting();
        categorySetting.setCategoryName(categoryName);
        categorySetting.setRegistUserId(1L);
        categorySetting.setRegistDatetime(new Date());
        categorySetting.setModifyUserId(1L);
        categorySetting.setModifyDatetime(new Date());

        return categorySetting;
    }

    private Setting getPropertySetting(Long parentSettingId, String categoryName, String propertyName, String propertyAliasEng, String propertyAliasKor,
                                       String propertyValue, String readOnlyYn, String dataType, String dataValues,
                                       String placeholderEng, String placeholderKor,
                                       String tooltipEng, String tooltipKor,
                                       Long displayOrder) {
        Setting propertySetting = new Setting();
        propertySetting.setParentSettingId(parentSettingId);
        propertySetting.setCategoryName(categoryName);
        propertySetting.setPropertyName(propertyName);
        propertySetting.setPropertyAliasEng(propertyAliasEng);
        propertySetting.setPropertyAliasKor(propertyAliasKor);
        propertySetting.setPropertyValue(propertyValue);
        propertySetting.setReadOnlyYn(readOnlyYn);
        propertySetting.setDataType(dataType);
        propertySetting.setDataValues(dataValues);
        propertySetting.setPlaceholderEng(placeholderEng);
        propertySetting.setPlaceholderKor(placeholderKor);
        propertySetting.setTooltipEng(tooltipEng);
        propertySetting.setTooltipKor(tooltipKor);
        propertySetting.setDisplayOrder(displayOrder);
        propertySetting.setRegistUserId(1L);
        propertySetting.setRegistDatetime(new Date());
        propertySetting.setModifyUserId(1L);
        propertySetting.setModifyDatetime(new Date());

        return propertySetting;
    }

}