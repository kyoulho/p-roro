package io.playce.roro.api.domain.common.service;

import io.playce.roro.api.common.error.exception.ResourceNotFoundException;
import io.playce.roro.api.common.util.WebUtil;
import io.playce.roro.api.websocket.listener.RoRoSessionListener;
import io.playce.roro.common.dto.common.setting.SettingRequest;
import io.playce.roro.common.dto.common.setting.SettingResponse;
import io.playce.roro.common.dto.common.setting.SettingResponse.Field;
import io.playce.roro.common.dto.common.setting.SettingResponse.SettingSubCategory;
import io.playce.roro.common.dto.common.setting.SettingResponse.Tooltip;
import io.playce.roro.common.dto.websocket.RoRoMessage;
import io.playce.roro.common.setting.SettingsHandler;
import io.playce.roro.common.util.GeneralCipherUtil;
import io.playce.roro.jpa.entity.Setting;
import io.playce.roro.jpa.repository.SettingRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static io.playce.roro.api.websocket.constants.WebSocketConstants.WS_CODE_MESSAGE_SETTINGS;
import static io.playce.roro.api.websocket.constants.WebSocketConstants.WS_QUEUE_NOTIFICATION;
import static io.playce.roro.common.setting.SettingsConstants.*;

@Slf4j
@AllArgsConstructor
@Service
public class SettingService {

    private final SettingRepository settingRepository;

    private final SimpMessagingTemplate simpMessagingTemplate;

    private final RoRoSessionListener sessionListener;

    public List<SettingResponse> getSettings() {
        List<SettingResponse> settingResponses = new ArrayList<>();
        List<Setting> settings = settingRepository.findByParentSettingIdOrderByDisplayOrder(null);

        for (Setting setting : settings) {
            SettingResponse settingResponse = new SettingResponse();
            List<SettingSubCategory> subCategories = new ArrayList<>();

            List<Setting> subSetting = settingRepository.findByParentSettingIdOrderByDisplayOrder(setting.getSettingId());

            // 카테고리 이름으로 일단 중복 제거를 한다.
            Set<String> subCategoryNameSet = new LinkedHashSet<>();
            for (Setting tempSetting : subSetting) {
                subCategoryNameSet.add(tempSetting.getCategoryName());
            }

            for (String subCategoryName : subCategoryNameSet) {
                SettingSubCategory settingSubCategory = new SettingSubCategory();
                List<SettingResponse.Setting> responseSettings = new ArrayList<>();
                Tooltip tooltip = new Tooltip();
                List<Field> fields = new ArrayList<>();

                for (Setting tempSetting : subSetting) {
                    if (tempSetting.getCategoryName().equals(subCategoryName)) {
                        SettingResponse.Setting responseSetting = new SettingResponse.Setting();
                        responseSetting.setSettingId(tempSetting.getSettingId());
                        responseSetting.setPropertyName(tempSetting.getPropertyName());
                        responseSetting.setPropertyAliasEng(tempSetting.getPropertyAliasEng());
                        responseSetting.setPropertyAliasKor(tempSetting.getPropertyAliasKor());

                        if (tempSetting.getPropertyName().equals(RORO_SSH_PASSWORD)) {
                            String decryptPassword = GeneralCipherUtil.decrypt(tempSetting.getPropertyValue());
                            responseSetting.setPropertyValue(GeneralCipherUtil.encryptWithPriv(decryptPassword));
                            // responseSetting.setPropertyValue(decryptPassword.replaceAll("(?<=.{0}).", "*"));
                        } else {
                            responseSetting.setPropertyValue(tempSetting.getPropertyValue());
                        }
                        responseSetting.setReadOnlyYn(tempSetting.getReadOnlyYn());
                        responseSetting.setDataType(tempSetting.getDataType());
                        responseSetting.setDataValues(tempSetting.getDataValues());
                        responseSetting.setPlaceholderEng(tempSetting.getPlaceholderEng());
                        responseSetting.setPlaceholderKor(tempSetting.getPlaceholderKor());
                        responseSetting.setDisplayOrder(tempSetting.getDisplayOrder());

                        responseSettings.add(responseSetting);

                        Field field = new Field();
                        field.setName(tempSetting.getPropertyName());
                        field.setPropertyAliasEng(tempSetting.getPropertyAliasEng());
                        field.setPropertyAliasKor(tempSetting.getPropertyAliasKor());
                        field.setDescriptionEng(getDescriptionEng(tempSetting.getTooltipEng()));
                        field.setDescriptionKor(getDescriptionKor(tempSetting.getTooltipKor()));
                        field.setOptionalEng(getOptionalDescriptionEng(tempSetting.getTooltipEng()));
                        field.setOptionalKor(getOptionalDescriptionKor(tempSetting.getTooltipKor()));
                        fields.add(field);
                    }
                }

                tooltip.setDescriptionEng(getTooltipDescription(subCategoryName, "eng"));
                tooltip.setDescriptionKor(getTooltipDescription(subCategoryName, "kor"));
                tooltip.setFields(fields);

                settingSubCategory.setSubCategoryName(subCategoryName);
                settingSubCategory.setTooltip(tooltip);
                settingSubCategory.setSettings(responseSettings);

                subCategories.add(settingSubCategory);
            }

            settingResponse.setCategoryName(setting.getCategoryName());
            settingResponse.setSubCategories(subCategories);
            settingResponses.add(settingResponse);
        }

        return settingResponses;
    }

    @Transactional
    public void modifySettingPropertyValue(List<SettingRequest> settingRequests) {
        boolean needToSend = false;
        for (SettingRequest settingRequest : settingRequests) {
            Setting setting = settingRepository.findById(settingRequest.getSettingId())
                    .orElseThrow(() -> new ResourceNotFoundException("Setting ID " + settingRequest.getSettingId() + " Not Found."));

            if (isContainPropertyName(setting.getPropertyName(), settingRequest.getPropertyValue())) {
                setting.setPropertyValue(settingRequest.getPropertyValue());
                setting.setModifyUserId(WebUtil.getUserId());
                setting.setModifyDatetime(new Date());

                settingRepository.save(setting);
            }

            if (setting.getPropertyName().equals(RORO_WEB_TERMINAL) ||
                    setting.getPropertyName().equals(ENABLE_MONITORING_SCHEDULE) ||
                    setting.getPropertyName().equals(ENABLE_SCHEDULED_SCAN) ||
                    setting.getPropertyName().equals(RORO_MIGRATION_ENABLED)
            ) {
                needToSend = true;
            }
        }

        // send websocket message
        if (needToSend) {
            sendUpdateSettings();
        }
    }

    private String getDescriptionEng(String description) {
        if (description.indexOf("Optional") > 0) {
            return description.substring(0, description.indexOf("Optional") - 1);
        }

        return description;
    }

    private String getDescriptionKor(String description) {
        if (description.indexOf("선택 항목") > 0) {
            return description.substring(0, description.indexOf("선택 항목") - 1);
        }

        return description;
    }

    private String getOptionalDescriptionEng(String description) {
        if (description.indexOf("Optional") > 0) {
            return description.substring(description.indexOf("Optional"));
        }

        return "";
    }

    private String getOptionalDescriptionKor(String description) {
        if (description.indexOf("선택 항목") > 0) {
            return description.substring(description.indexOf("선택 항목"));
        }

        return "";
    }

    private String getTooltipDescription(String subCategoryName, String lang) {
        final String sshConnectionForWindowsApplicationTooltipEng =
                "In order to scan applications found in a Windows environment, SSH access from the Windows server to the RoRo server must be available. To do this, specify default values for SSH access to the RoRo server.";
        final String sshConnectionForWindowsApplicationTooltipKor =
                "윈도우 환경에서 발견된 애플리케이션을 검사하려면 윈도우 서버에서 RoRo 서버로의 SSH 접속이 가능해야 합니다. 이를 위해 RoRo 서버의 SSH 접속을 위한 기본값을 지정합니다.";
        final String sshConnectionTooltipEng = "Manage settings to minimize problems with SSH access.";
        final String sshConnectionTooltipKor = "SSH 접속 관련 문제점을 최소화하기 위하여 설정을 관리합니다.";
        final String autoScanTooltipEng = "Choose whether to use the automatic scan function for all middleware and applications registered in the inventory.";
        final String autoScanTooltipKor = "인벤토리에 등록된 모든 미들웨어와 애플리케이션에 대한 자동 검사 기능의 사용 여부를 선택합니다.";
        final String applicationScanTooltipEng = "Manage settings required when scanning all inventoried applications.";
        final String applicationScanTooltipKor = "인벤토리에 등록된 모든 애플리케이션 검사 시 필요한 설정을 관리합니다.";
        final String migrationTooltipEng = "Manage necessary settings related to migration.";
        final String migrationTooltipKor = "마이그레이션 관련 필요한 설정을 관리합니다.";

        String tooltipDescription = "";

        if (lang.equals("eng")) {
            if (subCategoryName.equals("SSH Connection for Windows Application")) {
                return sshConnectionForWindowsApplicationTooltipEng;
            } else if (subCategoryName.equals("SSH Connection")) {
                return sshConnectionTooltipEng;
            } else if (subCategoryName.equals("Auto Scan")) {
                return autoScanTooltipEng;
            } else if (subCategoryName.equals("Application Scan")) {
                return applicationScanTooltipEng;
            } else if (subCategoryName.equals("Migration")) {
                return migrationTooltipEng;
            }
        } else {
            if (subCategoryName.equals("SSH Connection for Windows Application")) {
                return sshConnectionForWindowsApplicationTooltipKor;
            } else if (subCategoryName.equals("SSH Connection")) {
                return sshConnectionTooltipKor;
            } else if (subCategoryName.equals("Auto Scan")) {
                return autoScanTooltipKor;
            } else if (subCategoryName.equals("Application Scan")) {
                return applicationScanTooltipKor;
            } else if (subCategoryName.equals("Migration")) {
                return migrationTooltipKor;
            }
        }

        return tooltipDescription;
    }

    private void sendUpdateSettings() {
        String msg = "Settings property has been updated.";

        // Map<String, String> map = new HashMap<>();
        // map.put("title", "Settings");
        // map.put("message", msg);

        RoRoMessage message = new RoRoMessage();
        message.setCode(WS_CODE_MESSAGE_SETTINGS);
        message.setMessage(msg);
        message.setTimestamp(System.currentTimeMillis());
        message.setStatus(null);
        message.setData(null);

        List<String> sessionIdList = sessionListener.getAllBrowserSessionIds();

        for (String sessionId : sessionIdList) {
            simpMessagingTemplate.convertAndSendToUser(sessionId, WS_QUEUE_NOTIFICATION + "/" + sessionId, message, sessionListener.createHeaders(sessionId));
        }
    }

    private boolean isContainPropertyName(String propertyName, String propertyValue) {
        if (propertyName.equals(RORO_WEB_TERMINAL) ||
                propertyName.equals(ENABLE_MONITORING_SCHEDULE) ||
                propertyName.equals(RORO_SSH_IP_ADDRESS) ||
                propertyName.equals(RORO_SSH_PORT) ||
                propertyName.equals(RORO_SSH_USER_NAME) ||
                propertyName.equals(RORO_SSH_PASSWORD) ||
                propertyName.equals(RORO_SSH_PPK_FILE_PATH) ||
                propertyName.equals(RORO_SSH_CONNECT_TIMEOUT) ||
                propertyName.equals(RORO_SSH_USE_BOUNCY_CASTLE_PROVIDER) ||
                propertyName.equals(RORO_SSH_SKIP_MESSAGE) ||
                propertyName.equals(ENABLE_SCHEDULED_SCAN) ||
                propertyName.equals(RORO_MIDDLEWARE_AUTO_SCAN) ||
                propertyName.equals(RORO_MIDDLEWARE_AUTO_SCAN_AFTER_SERVER_SCAN) ||
                propertyName.equals(RORO_APPLICATION_AUTO_SCAN) ||
                propertyName.equals(APPSCAN_FILE_EXTENSIONS) ||
                propertyName.equals(APPSCAN_COPY_ONLY_MATCHED_EXTENSIONS) ||
                propertyName.equals(APPSCAN_EXCLUDE_FILENAMES) ||
                propertyName.equals(APPSCAN_EXCLUDE_DOMAINS) ||
                propertyName.equals(APPSCAN_REMOVE_FILES_AFTER_SCAN) ||
                propertyName.equals(APPSCAN_COPY_IGNORE_FILENAMES) ||
                propertyName.equals(WINDOWS_POWERSHELL_USE_OUTFILE) ||
                propertyName.equals(RORO_MIGRATION_ENABLED) ||
                propertyName.equals(RORO_MIGRATION_DIR_REMOVE) ||
                propertyName.equals(RORO_MIGRATION_BUCKET_NAME) ||
                propertyName.equals(RORO_MIGRATION_BUCKET_REGION) ||
                propertyName.equals(RORO_MIGRATION_BUCKET_REMOVE) ||
                propertyName.equals(RORO_MIGRATION_INCLUDE_SYSTEM_UID)
        ) {

            SettingsHandler.setSettingsValue(propertyName, propertyValue);

            return true;
        } else {
            return false;
        }
    }

}
