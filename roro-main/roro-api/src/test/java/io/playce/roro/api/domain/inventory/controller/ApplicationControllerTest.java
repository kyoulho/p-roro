package io.playce.roro.api.domain.inventory.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.playce.roro.common.dto.assessment.ApplicationDto;
import io.playce.roro.common.dto.common.RemoteExecResult;
import io.playce.roro.common.util.SSHUtil;
import io.playce.roro.common.util.WinRmUtils;
import io.playce.roro.common.util.support.TargetHost;
import io.playce.roro.mw.asmt.component.CommandConfig;
import io.playce.roro.mw.asmt.enums.COMMAND;
import io.playce.roro.mw.asmt.util.GetInfoStrategy;
import io.playce.roro.mw.asmt.util.MWCommonUtil;
import io.playce.roro.mw.asmt.util.UnixLikeInfoStrategy;
import io.playce.roro.mw.asmt.util.WindowsInfoStrategy;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.Map;

import static io.playce.roro.mw.asmt.util.MWCommonUtil.getJavaVendorProperty;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApplicationControllerTest {

    @Autowired
    public WebApplicationContext context;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CommandConfig commandConfig;

    @Before
    public void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
                .apply(springSecurity())  // Security 사용 시 등록
                .build();
    }

    final String accessToken = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJSb1JvIFVzZXIgSW5mby4iLCJpc3MiOiJodHRwczovL3d3dy5wbGF5LWNlLmlvIiwidXNlciI6eyJ1c2VySWQiOjEsInVzZXJMb2dpbklkIjoiYWRtaW4iLCJ1c2VybmFtZSI6ImFkbWluIiwidXNlck5hbWVLb3JlYW4iOiLqtIDrpqzsnpAiLCJ1c2VyTmFtZUVuZ2xpc2giOiJBZG1pbiIsInVzZXJFbWFpbCI6ImFkbWluQG9zY2kua3IifSwicm9sZXMiOlsiUk9MRV9BRE1JTiJdLCJpYXQiOjE2NzMyMjYyODEsImV4cCI6MTc2NzgzNDI4MX0.lT6znRkSfQKkH4MDmr_TEfQ_2ZNFKCAzTEuy7gfaujoL0XIs1bV5zAjZLmn5RnCxrKSlWrG7nyNnHfkiZUpmBQ";

    @Test
    void 애플리케이션_데이터소스() throws Exception {
        mockMvc.perform(get("/api/projects/{projectId}/inventory/applications/{applicationId}/datasources", 1, 34)
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andDo(print())
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        ;
    }

    @Test
    void 애플리케이션_자바() throws Exception {
        // TargetHost targetHost = new TargetHost();
        // targetHost.setIpAddress("192.168.4.77");
        // targetHost.setPort(22);
        // targetHost.setUsername("root");
        // targetHost.setPassword("jan01jan");
        //
        // ApplicationDto applicationDto = new ApplicationDto();
        // applicationDto.setTargetHost(targetHost);
        // applicationDto.setWindowsYn("N");
        //
        // String deployPath = "/opt/playce/playce-roro";
        // String command = "ps -ef | grep -v 'grep'| grep java | grep " + deployPath;
        // String response = SSHUtil.executeCommand(targetHost, command).replaceAll("\\s+", " ");
        //
        // System.out.println(getJavaVersion(response, applicationDto));
        // System.out.println(getJavaVendor(response, applicationDto));

        // TargetHost targetHost = new TargetHost();
        // targetHost.setIpAddress("192.168.1.158");
        // targetHost.setPort(5985);
        // targetHost.setUsername("Administrator");
        // targetHost.setPassword("P@ssw0rd");
        //
        // ApplicationDto applicationDto = new ApplicationDto();
        // applicationDto.setTargetHost(targetHost);
        // applicationDto.setWindowsYn("Y");
        //
        // String deployPath = "C:\\\\apache-tomcat-7.0.32";
        // String command = "wmic process where 'CommandLine like \"%java%\" and CommandLine like \"%" + deployPath + "%\" and not CommandLine like \"%wmic%\"' get CommandLine /format:list";
        // String response = WinRmUtils.executeCommand(targetHost, command)
        //         .replaceAll("\\s+", " ")
        //         .replaceAll("\"", "");
        //
        // if (StringUtils.isNotEmpty(response)) {
        //     response = StringUtils.defaultString(response.substring("CommandLine=".length()));
        // } else {
        //     response = StringUtils.EMPTY;
        // }
        //
        // System.out.println(getJavaVersion(response, applicationDto));
        // System.out.println(getJavaVendor(response, applicationDto));


    }

    private String getJavaVersion(String response, ApplicationDto applicationDto) throws InterruptedException {
        GetInfoStrategy strategy = GetInfoStrategy.getStrategy(applicationDto.getWindowsYn().equals("Y"));

        if (StringUtils.isNotEmpty(response)) {
            String[] responseArray = response.split(" ");
            for (String splitString : responseArray) {
                if (splitString.contains("/bin/java") || splitString.contains("\\bin\\java")) {
                    boolean sudo = !strategy.isWindows() && SSHUtil.isSudoer(applicationDto.getTargetHost());

                    Map<String, String> commandMap = Map.of(COMMAND.JAVA_VERSION.name(), COMMAND.JAVA_VERSION.command(commandConfig, strategy.isWindows(), splitString));
                    Map<String, RemoteExecResult> resultMap = MWCommonUtil.executeCommand(applicationDto.getTargetHost(), commandMap, sudo, strategy);
                    RemoteExecResult result = resultMap.get(COMMAND.JAVA_VERSION.name());

                    if (!result.isErr()) {
                        return result.getResult().trim();
                    }
                }
            }
        }

        return null;
    }

    private String getJavaVendor(String response, ApplicationDto applicationDto) throws InterruptedException {
        GetInfoStrategy strategy = GetInfoStrategy.getStrategy(applicationDto.getWindowsYn().equals("Y"));

        if (StringUtils.isNotEmpty(response)) {
            String[] responseArray = response.split(" ");
            for (String splitString : responseArray) {
                if (splitString.contains("/bin/java") || splitString.contains("\\bin\\java")) {
                    boolean sudo = !strategy.isWindows() && SSHUtil.isSudoer(applicationDto.getTargetHost());

                    Map<String, String> commandMap = Map.of(COMMAND.JAVA_VENDOR.name(), COMMAND.JAVA_VENDOR.command(commandConfig, strategy.isWindows(), splitString));
                    Map<String, RemoteExecResult> resultMap = MWCommonUtil.executeCommand(applicationDto.getTargetHost(), commandMap, sudo, strategy);
                    RemoteExecResult result = resultMap.get(COMMAND.JAVA_VENDOR.name());

                    if (!result.isErr()) {
                        return getJavaVendorProperty(result.getResult().trim());
                    }
                }
            }
        }
        return null;

    }

}