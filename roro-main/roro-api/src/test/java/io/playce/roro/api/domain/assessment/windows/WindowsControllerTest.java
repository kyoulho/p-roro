package io.playce.roro.api.domain.assessment.windows;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import io.playce.roro.asmt.windows.command.PowerShellVersion2UnderCommand;
import io.playce.roro.asmt.windows.dto.WindowsAssessmentDto;
import io.playce.roro.asmt.windows.impl.WindowsAssessment;
import io.playce.roro.common.dto.prerequisite.ServerResult;
import io.playce.roro.common.util.support.TargetHost;
import io.playce.roro.mw.asmt.component.CommandConfig;
import io.playce.roro.prerequisite.PrerequisiteComponent;
import io.playce.roro.prerequisite.server.ServerInfo;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import javax.transaction.Transactional;
import java.io.File;
import java.io.FileInputStream;
import java.util.*;

import static io.playce.roro.asmt.windows.impl.factory.PowerShellParseUtil.splitToArrayByCrlf;
import static io.playce.roro.common.util.WinRmUtils.executePsShell;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WindowsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WindowsAssessment windowsAssessment;

    @Autowired
    private PrerequisiteComponent prerequisiteComponent;

    @Autowired
    private ObjectMapper objectMapper;

    final String accessToken = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJSb1JvIFVzZXIgSW5mby4iLCJpc3MiOiJodHRwczovL3d3dy5wbGF5LWNlLmlvIiwidXNlciI6eyJ1c2VySWQiOjEsInVzZXJMb2dpbklkIjoiYWRtaW4iLCJ1c2VybmFtZSI6ImFkbWluIiwidXNlck5hbWVLb3JlYW4iOiLqtIDrpqzsnpAiLCJ1c2VyTmFtZUVuZ2xpc2giOiJBZG1pbiIsInVzZXJFbWFpbCI6ImFkbWluQG9zY2kua3IifSwicm9sZXMiOlsiUk9MRV9BRE1JTiJdLCJpYXQiOjE2NzMyMjYyODEsImV4cCI6MTc2NzgzNDI4MX0.lT6znRkSfQKkH4MDmr_TEfQ_2ZNFKCAzTEuy7gfaujoL0XIs1bV5zAjZLmn5RnCxrKSlWrG7nyNnHfkiZUpmBQ";


    @Autowired
    private CommandConfig commandConfig;

    @Test
    void 윈도우즈_테스트() throws Exception {

        TargetHost targetHost = new TargetHost();
//        targetHost.setIpAddress("128.134.105.19");
//        targetHost.setUsername("Administrator");
//        targetHost.setPassword("Jan01jan");
//        targetHost.setPort(5985);

//        targetHost.setIpAddress("192.168.1.158");
//        targetHost.setIpAddress("192.168.1.108");
//        targetHost.setIpAddress("192.168.1.157");
        targetHost.setIpAddress("192.168.1.95");
//        targetHost.setIpAddress("192.168.1.106");
//        targetHost.setIpAddress("192.168.1.107");
        targetHost.setUsername("Administrator");
        targetHost.setPassword("P@ssw0rd");
        targetHost.setPort(5985);

        windowsAssessment.assessment(targetHost);

        System.out.println(objectMapper.writeValueAsString(windowsAssessment.assessment(targetHost)));

    }

    @Test
    @Transactional
    void 윈도우즈_수동업로드_테스트() throws Exception {

        FileInputStream fis = new FileInputStream(new File("/Users/jeonghobaek/Downloads/result.json"));
        MockMultipartFile mockMultipartFile = new MockMultipartFile("assessmentFile", fis);

        mockMvc.perform(multipart("/api/projects/{projectId}/inventory/servers/{serverId}/assessment", 1, 76)
                        .file(mockMultipartFile)
                        .header(HttpHeaders.AUTHORIZATION, accessToken))
                .andDo(print())
                .andExpect(status().isOk());

    }

    @Test
    void 윈도우즈_사전체크() throws Exception {

        TargetHost targetHost = new TargetHost();
        targetHost.setIpAddress("192.168.1.158");  // 2008
        // targetHost.setIpAddress("192.168.1.108");  // 2008R2
        //  targetHost.setIpAddress("192.168.1.157");  // 2012
        // targetHost.setIpAddress("192.168.1.95");  // 2012R2
        // targetHost.setIpAddress("192.168.1.106");  // 2016
        // targetHost.setIpAddress("192.168.1.107");  // 2019
        //  targetHost.setUsername("Administrator");
        //  targetHost.setPassword("P@ssw0rd");

        targetHost.setUsername("roro4");
        targetHost.setPassword("jan01jan");

        targetHost.setPort(5985);

        ServerInfo serverInfo = ServerInfo.builder()
                .inventoryProcessConnectionInfo(null)
                .host(targetHost)
                .config(null)
                .window(true)
                .build();

        ServerResult serverResult = new ServerResult("admin");

        prerequisiteComponent.executeCheckServer(serverInfo, serverResult);

    }

    public static void main(String[] args) throws Exception {
        String result = "# Copyright (c) 1993-2006 Microsoft Corp.\n" +
                "#\n" +
                "# This is a sample HOSTS file used by Microsoft TCP/IP for Windows.\n" +
                "#\n" +
                "# This file contains the mappings of IP addresses to host names. Each\n" +
                "# entry should be kept on an individual line. The IP address should\n" +
                "# be placed in the first column followed by the corresponding host name.\n" +
                "# The IP address and the host name should be separated by at least one\n" +
                "# space.\n" +
                "#\n" +
                "# Additionally, comments (such as these) may be inserted on individual\n" +
                "# lines or following the machine name denoted by a '#' symbol.\n" +
                "#\n" +
                "# For example:\n" +
                "#\n" +
                "#      102.54.94.97     rhino.acme.com          # source server\n" +
                "#       38.25.63.10     x.acme.com              # x client host\n" +
                "\n" +
                "127.0.0.1       localhost\n" +
                "::1             localhost\n";


        List<String> hostContents = new ArrayList<>();

        String[] stringArrays = splitToArrayByCrlf(result);
        for (String temp : stringArrays) {
            if (!(temp.trim().startsWith("#") || temp.trim().equals(StringUtils.EMPTY))) {
                hostContents.add(temp.trim().replaceAll("\\s+", StringUtils.SPACE));
            }
        }

        WindowsAssessmentDto.Hosts hosts = new WindowsAssessmentDto.Hosts();

        Map<String, List<String>> hostMap = new HashMap<>();
        for (String hostContent : hostContents) {
            if (hostContent.contains(StringUtils.SPACE)) {
                String[] tempString = hostContent.split(StringUtils.SPACE, 2);
                hostMap.put(tempString[0], new ArrayList<>(Arrays.asList(tempString[1].split(StringUtils.SPACE))));
            }
        }

        hosts.setContents(result);
        hosts.setMappings(hostMap);

        System.out.println(hosts);

    }

}
