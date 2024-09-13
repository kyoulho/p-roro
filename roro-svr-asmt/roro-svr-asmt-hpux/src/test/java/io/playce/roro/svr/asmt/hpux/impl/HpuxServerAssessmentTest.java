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
 * Hoon Oh       1월 21, 2022            First Draft.
 */
package io.playce.roro.svr.asmt.hpux.impl;

import io.playce.roro.svr.asmt.dto.common.interfaces.InterfaceInfo;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HpuxServerAssessmentTest {
//
//     public static void main(String[] args) {
//         TargetHost targetHost = new TargetHost();
//         targetHost.setIpAddress("192.168.4.69");
//         targetHost.setPort(22);
//         targetHost.setUsername("root");
//         targetHost.setPassword("root");
//
//         ServerAssessment assessment = new HpuxServerAssessment();
//         HpuxAssessmentResult result = assessment.assessment(targetHost);
//
//         try {
//             System.out.println(JsonUtil.objToJson(result, true));
//         } catch (IOException e) {
//             e.printStackTrace();
//         }
//
//     }

    public static void main(String[] args) {
        String ifResult = "lo0: flags=849<UP,LOOPBACK,RUNNING,MULTICAST>\n" +
                "        inet 127.0.0.1 netmask ff000000 \n" +
                "lan901: flags=1843<UP,BROADCAST,RUNNING,MULTICAST,CKO>\n" +
                "        inet 7.7.7.1 netmask ffffff00 broadcast 7.7.7.255\n" +
                "lan900: flags=1843<UP,BROADCAST,RUNNING,MULTICAST,CKO>\n" +
                "        inet 10.10.10.208 netmask ffffff00 broadcast 10.10.10.255\n" +
                "lan11: flags=1843<UP,BROADCAST,RUNNING,MULTICAST,CKO>\n" +
                "        inet 172.17.81.203 netmask ffffff00 broadcast 172.17.81.255\n" +
                "lan900:1: flags=8000000000001843<UP,BROADCAST,RUNNING,MULTICAST,CKO,PORT>\n" +
                "        inet 10.10.10.209 netmask ffffff00 broadcast 10.10.10.255\n" +
                "lan900:801: flags=8000000000001843<UP,BROADCAST,RUNNING,MULTICAST,CKO,PORT>\n" +
                "        inet 169.254.216.133 netmask ffff0000 broadcast 169.254.255.255\n" +
                "lan900:2: flags=8000000000001843<UP,BROADCAST,RUNNING,MULTICAST,CKO,PORT>\n" +
                "        inet 10.10.10.210 netmask ffffff00 broadcast 10.10.10.255";

        String gwResult = "127.0.0.1             127.0.0.1          UH    0    lo0       32808\n" +
                "10.10.41.31           10.10.41.31        UH    0    lan900    32808\n" +
                "10.10.41.0            10.10.41.31        U     2    lan900     1500\n" +
                "127.0.0.0             127.0.0.1          U     0    lo0       32808\n" +
                "default               10.10.41.1         UG    0    lan900     1500";

        InterfaceInfo iterInfo = null;
        if (StringUtils.isNotEmpty(ifResult)) {
            for (String line : ifResult.split("\n")) {

                if (StringUtils.isNotEmpty(line)) {

                    String[] words = line.trim().split("\\s+");

                    Pattern p = Pattern.compile("^\\w*\\d*:");
                    Matcher m = p.matcher(line);
                    if (m.find()) {
                        // System.err.println(words[0]);
                        iterInfo = initInterface(words);
                        getDefaultGateway(iterInfo, gwResult);
                        System.err.println(iterInfo.getDevice() + " : " + iterInfo.getGateway());
                    } else if (words[0].startsWith("options=")) {
                        // parseOptionsLine(words, iterInfo);
                    } else if (words[0].equals("nd6")) {
                        // parseOptionsLine(words, iterInfo);
                    } else if (words[0].equals("media")) {
                        // parseOptionsLine(words, iterInfo);
                    } else if (words[0].equals("lladdr")) {
                        // parseOptionsLine(words, iterInfo);
                    } else if (words[0].equals("status")) {
                        // parseOptionsLine(words, iterInfo);
                    } else if (words[0].equals("ether")) {
                        //parseEtherLine(words, iterInfo);
                    } else if (words[0].equals("inet")) {
                        //parseInetLine(words, iterInfo);
                    } else if (words[0].equals("inet6")) {
                        // parseInet6line(words, iterInfo);
                    }
                }
            }
        }
    }

    public static InterfaceInfo initInterface(String[] words) {
        InterfaceInfo info = new InterfaceInfo();
        info.setDevice(words[0].substring(0, words[0].length() - 1));
        info.setGateway("unknown");
        info.setScript("unknown");
        info.setMacaddress("unknown");
        return info;
    }

    public static void getDefaultGateway(InterfaceInfo iterInfo, String result) {
        if (StringUtils.isNotEmpty(result)) {
            String gw = null;
            String[] lines = result.split("\n");

            for (String line : lines) {
                String[] data = line.split("\\s+");

                if (data.length > 5) {
                    if (data[4].equals(iterInfo.getDevice())) {
                        gw = data[1];
                    }
                }
            }

            if (StringUtils.isEmpty(gw)) {
                for (String line : lines) {
                    String[] data = line.split("\\s+");

                    if (data.length > 5) {
                        if (iterInfo.getDevice().startsWith(data[4])) {
                            gw = data[1];
                        }
                    }
                }
            }

            if (StringUtils.isNotEmpty(gw)) {
                iterInfo.setGateway(gw);
            }
        }
    }
}