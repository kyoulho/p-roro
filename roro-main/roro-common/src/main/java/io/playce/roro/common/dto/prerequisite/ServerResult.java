package io.playce.roro.common.dto.prerequisite;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

import static io.playce.roro.common.dto.prerequisite.CheckStatus.Icon;
import static io.playce.roro.common.dto.prerequisite.CheckStatus.Result;

@Getter
@Setter
@Slf4j
@ToString
public class ServerResult {
    private int lastStep = 0;
    private Map<Integer, Icon> iconMap = new TreeMap<>();
    private Map<Integer, Result> resultMap = new HashMap<>();
    private Map<Integer, List<String>> messageMap = new HashMap<>();
    private String userName;

    public ServerResult(String userName) {
        this.userName = userName;
    }

    public void increaseStep() {
        this.lastStep++;
    }

    public List<String> getMessage(int step) {
        return messageMap.get(step);
    }

    private void setResult(Result result) {
        setResult(this.lastStep, result);
    }

    private void setResult(int step, Result result) {
        resultMap.put(step, result);
        messageMap.computeIfAbsent(step, k -> new ArrayList<>());
    }

    public void addMessage(String message) {
        addMessage(message, true);
    }

    private void addMessage(String message, boolean append) {
        addMessage(this.lastStep, message, append);
    }

    private void addMessage(int step, String message, boolean append) {
        if (!append) {
            messageMap.put(step, new ArrayList<>());
        }
        this.messageMap.get(step).add(message);
    }

    public void setIcon(Icon status) {
        setIcon(this.lastStep, status);
    }

    private void setIcon(int step, Icon icon) {
        iconMap.put(step, icon);
    }

    public Icon getIcon(int step) {
        return iconMap.get(step);
    }

    public void updateState(Icon icon, Result result) {
        setIcon(icon);
        setResult(result);
    }

    public void updateState(Icon icon, Result result, String message) {
        updateState(icon, result, message, true);
    }

    public void updateState(Icon icon, Result result, String message, boolean append) {
        updateState(icon, result);
        addMessage(message, append);
    }

    public void addStep4MessageList() {
        messageMap.put(this.lastStep, new ArrayList<>());
    }

    public static PrerequisiteJson prerequisiteJson(ServerResult serverResult, Date date, boolean isWindows) {
        PrerequisiteJson prerequisiteJson = new PrerequisiteJson();
        prerequisiteJson.setUserName(serverResult.getUserName());
        List<CheckStatus> checkStatuses = new ArrayList<>();
        Map<Integer, Icon> iconMap = serverResult.getIconMap();
        Map<Integer, Result> resultMap = serverResult.getResultMap();
        Map<Integer, List<String>> messageMap = serverResult.getMessageMap();
        List<String> statusMessageList = new ArrayList<>();

        boolean assessmentEnable = true;
        for (Integer step : iconMap.keySet()) {
            CheckStatus checkStatus = CheckStatus.builder()
                    .icon(iconMap.get(step))
                    .status(resultMap.get(step))
                    .message(getStaticMessage(step, isWindows))
                    .build();
            checkStatuses.add(checkStatus);
            List<String> messages = messageMap.get(step);
            if (!messages.isEmpty() && iconMap.get(step) != Icon.SUCCESS) {
                statusMessageList.addAll(messages.stream()
                        .map(m -> String.format("step: %d - %s", step, m))
                        .collect(Collectors.toList()));
            }
            if (step < 3) {
                assessmentEnable = assessmentEnable && iconMap.get(step) == Icon.SUCCESS;
            }
        }
        prerequisiteJson.setCheckStatus(checkStatuses);
        prerequisiteJson.setStatusMessage(statusMessageList);
        prerequisiteJson.setCheckedDate(date.getTime());
        prerequisiteJson.setAssessmentEnabled(assessmentEnable ? "Y" : "N");

        return prerequisiteJson;
    }

    public static String getStaticMessage(Integer step, boolean isWindows) {
        switch (step) {
            case 1:
                return "Check connectivity to server";
            case 2:
                return "Check access to your account";
            case 3:
                return "Check run as administrator privileges";
            case 4:
                if (isWindows) {
                    return "Check wmic status";
                } else {
                    return "Check if package file are installed";
                }
        }
        return null;
    }

    /**
     * <pre>
     *
     * </pre>
     *
     * @author Dong-Heon Han
     * @version 3.0
     */
    @Setter
    @Getter
    @ToString
    public static class PrerequisiteJson {
        private String userName;
        private List<CheckStatus> checkStatus;
        private List<String> statusMessage;
        private String assessmentEnabled;
        private Long checkedDate;
    }
}

