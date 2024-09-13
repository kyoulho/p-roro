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
 * Hoon Oh       1월 27, 2022            First Draft.
 */
package io.playce.roro.discover.server.util;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * <pre>
 *
 *
 * </pre>
 *
 * @author Hoon Oh
 * @version 1.0
 */
public class WellKnownPortUtil {
    private static final Map<Integer, String> TCP_PORT_MAP = new HashMap<>();
    private static final Map<Integer, String> UDP_PORT_MAP = new HashMap<>();

    static {
        TCP_PORT_MAP.put(1, "TCPMUX");
        TCP_PORT_MAP.put(7, "Echo");
        TCP_PORT_MAP.put(9, "Discard");
        TCP_PORT_MAP.put(13, "Daytime");
        TCP_PORT_MAP.put(17, "QOTD");
        TCP_PORT_MAP.put(18, "Message Send");
        TCP_PORT_MAP.put(19, "CHARGEN");
        TCP_PORT_MAP.put(20, "FTP");
        TCP_PORT_MAP.put(21, "FTP");
        TCP_PORT_MAP.put(22, "SSH");
        TCP_PORT_MAP.put(23, "Telnet");
        TCP_PORT_MAP.put(25, "SMTP");
        TCP_PORT_MAP.put(37, "Time");
        TCP_PORT_MAP.put(43, "WHOIS");
        TCP_PORT_MAP.put(49, "TACACS");
        TCP_PORT_MAP.put(53, "DNS");
        TCP_PORT_MAP.put(70, "Gopher");
        TCP_PORT_MAP.put(79, "Finger");
        TCP_PORT_MAP.put(80, "HTTP");
        TCP_PORT_MAP.put(88, "Kerberos");
        TCP_PORT_MAP.put(95, "SUPDUP");
        TCP_PORT_MAP.put(109, "POP2");
        TCP_PORT_MAP.put(110, "POP3");
        TCP_PORT_MAP.put(113, "Ident");
        TCP_PORT_MAP.put(119, "NNTP");
        TCP_PORT_MAP.put(123, "NTP");
        TCP_PORT_MAP.put(139, "NetBIOS");
        TCP_PORT_MAP.put(143, "IMAP4");
        TCP_PORT_MAP.put(152, "BFTP");
        TCP_PORT_MAP.put(153, "SGMP");
        TCP_PORT_MAP.put(156, "SQL");
        TCP_PORT_MAP.put(158, "DMSP");
        TCP_PORT_MAP.put(162, "SNMPTRAP");
        TCP_PORT_MAP.put(177, "XDMCP");
        TCP_PORT_MAP.put(179, "BGP");
        TCP_PORT_MAP.put(194, "IRC");
        TCP_PORT_MAP.put(199, "SMUX");
        TCP_PORT_MAP.put(213, "IPX");
        TCP_PORT_MAP.put(218, "MPP");
        TCP_PORT_MAP.put(220, "IMAP3");
        TCP_PORT_MAP.put(259, "ESRO");
        TCP_PORT_MAP.put(262, "Arcisdms");
        TCP_PORT_MAP.put(264, "BGMP");
        TCP_PORT_MAP.put(318, "TSP");
        TCP_PORT_MAP.put(350, "MATIP");
        TCP_PORT_MAP.put(351, "MATIP");
        TCP_PORT_MAP.put(366, "ODMR");
        TCP_PORT_MAP.put(369, "Rpc2portmap");
        TCP_PORT_MAP.put(387, "AURP");
        TCP_PORT_MAP.put(389, "LDAP");
        TCP_PORT_MAP.put(401, "UPS");
        TCP_PORT_MAP.put(427, "SLP");
        TCP_PORT_MAP.put(433, "NNSP");
        TCP_PORT_MAP.put(443, "HTTPS");
        TCP_PORT_MAP.put(444, "SNPP");
        TCP_PORT_MAP.put(445, "Microsoft-DS");
        TCP_PORT_MAP.put(464, "Kerberos");
        TCP_PORT_MAP.put(465, "SMTPS");
        TCP_PORT_MAP.put(497, "Retrospect");
        TCP_PORT_MAP.put(502, "Modbus");
        TCP_PORT_MAP.put(504, "Citadel");
        TCP_PORT_MAP.put(510, "FCP");
        TCP_PORT_MAP.put(512, "Rexec");
        TCP_PORT_MAP.put(513, "rlogin");
        TCP_PORT_MAP.put(515, "LPD");
        TCP_PORT_MAP.put(520, "EFS");
        TCP_PORT_MAP.put(524, "NCP");
        TCP_PORT_MAP.put(530, "RPC");
        TCP_PORT_MAP.put(540, "UUCP");
        TCP_PORT_MAP.put(542, "commerce");
        TCP_PORT_MAP.put(543, "klogin");
        TCP_PORT_MAP.put(544, "kshell");
        TCP_PORT_MAP.put(546, "DHCPv6 client");
        TCP_PORT_MAP.put(547, "DHCPv6 server");
        TCP_PORT_MAP.put(548, "AFP");
        TCP_PORT_MAP.put(550, "new-rwho");
        TCP_PORT_MAP.put(554, "RTSP");
        TCP_PORT_MAP.put(556, "RFS");
        TCP_PORT_MAP.put(563, "NNTPS");
        TCP_PORT_MAP.put(587, "SMTP");
        TCP_PORT_MAP.put(591, "FileMaker 6.0");
        TCP_PORT_MAP.put(631, "IPP");
        TCP_PORT_MAP.put(635, "RLZ DBase");
        TCP_PORT_MAP.put(636, "LDAPS");
        TCP_PORT_MAP.put(639, "MSDP");
        TCP_PORT_MAP.put(643, "SANity");
        TCP_PORT_MAP.put(646, "LDP");
        TCP_PORT_MAP.put(647, "DHCP Failover");
        TCP_PORT_MAP.put(648, "RRP");
        TCP_PORT_MAP.put(651, "IEEE-MMS");
        TCP_PORT_MAP.put(654, "MMS");
        TCP_PORT_MAP.put(674, "ACAP");
        TCP_PORT_MAP.put(688, "REALM-RUSD");
        TCP_PORT_MAP.put(690, "VATP");
        TCP_PORT_MAP.put(691, "MS Exchange Routing");
        TCP_PORT_MAP.put(695, "IEEE-MMS-SSL");
        TCP_PORT_MAP.put(700, "EPP");
        TCP_PORT_MAP.put(701, "LMP");
        TCP_PORT_MAP.put(702, "IRIS over BEEP");
        TCP_PORT_MAP.put(706, "SILC");
        TCP_PORT_MAP.put(712, "TBRPF");
        TCP_PORT_MAP.put(749, "Kerberos");
        TCP_PORT_MAP.put(753, "RRH");
        TCP_PORT_MAP.put(754, "tell send");
        TCP_PORT_MAP.put(800, "mdbs-daemon");
        TCP_PORT_MAP.put(802, "Modbus");
        TCP_PORT_MAP.put(830, "NETCONF over SSH");
        TCP_PORT_MAP.put(831, "NETCONF over BEEP");
        TCP_PORT_MAP.put(832, "NETCONF for SOAP over HTTPS");
        TCP_PORT_MAP.put(833, "NETCONF for SOAP over BEEP");
        TCP_PORT_MAP.put(847, "DHCP Failover protocol");
        TCP_PORT_MAP.put(848, "GDOI");
        TCP_PORT_MAP.put(853, "DNS over TLS");
        TCP_PORT_MAP.put(860, "iSCSI");
        TCP_PORT_MAP.put(861, "OWAMP");
        TCP_PORT_MAP.put(862, "TWAMP");
        TCP_PORT_MAP.put(873, "rsync");
        TCP_PORT_MAP.put(953, "RNDC");
        TCP_PORT_MAP.put(989, "FTP over TLS/SSL");
        TCP_PORT_MAP.put(990, "FTP over TLS/SSL");
        TCP_PORT_MAP.put(991, "NAS");
        TCP_PORT_MAP.put(992, "Telnet over TLS/SSL");
        TCP_PORT_MAP.put(993, "IMAPS");
        TCP_PORT_MAP.put(995, "POP3S");
        TCP_PORT_MAP.put(3389, "RDP");
        TCP_PORT_MAP.put(5985, "WinRM");
        TCP_PORT_MAP.put(5986, "WinRM");

        // Database
        TCP_PORT_MAP.put(1521, "Oracle");
        TCP_PORT_MAP.put(3306, "MySQL");
        TCP_PORT_MAP.put(1433, "MS-SQL");
        TCP_PORT_MAP.put(5432, "PostgreSQL");
        TCP_PORT_MAP.put(50000, "DB2");
        TCP_PORT_MAP.put(1526, "Informix");
        TCP_PORT_MAP.put(5000, "Sybase");
        TCP_PORT_MAP.put(1527, "Derby");
        TCP_PORT_MAP.put(5984, "CouchDB");
        TCP_PORT_MAP.put(27017, "MongoDB");
        TCP_PORT_MAP.put(27018, "MongoDB");
        TCP_PORT_MAP.put(27019, "MongoDB");
        TCP_PORT_MAP.put(28017, "MongoDB");
        TCP_PORT_MAP.put(2181, "ZooKeeper");
        TCP_PORT_MAP.put(60000, "Hadoop");
        TCP_PORT_MAP.put(60010, "Hadoop");
        TCP_PORT_MAP.put(60020, "Hadoop");
        TCP_PORT_MAP.put(60030, "Hadoop");
        TCP_PORT_MAP.put(7000, "Cassandra");

        // WAS
        TCP_PORT_MAP.put(8080, "HTTP"); // Apache Tomcat
        TCP_PORT_MAP.put(7001, "HTTP"); // Oracle WebLogic
        TCP_PORT_MAP.put(9043, "HTTP"); // IBM WebSphere
        TCP_PORT_MAP.put(9736, "HTTP"); // Tmax Jeus

        UDP_PORT_MAP.put(7, "Echo");
        UDP_PORT_MAP.put(9, "Discard");
        UDP_PORT_MAP.put(11, "Active Users");
        UDP_PORT_MAP.put(13, "Daytime");
        UDP_PORT_MAP.put(17, "QOTD");
        UDP_PORT_MAP.put(18, "Message Send");
        UDP_PORT_MAP.put(19, "CHARGEN");
        UDP_PORT_MAP.put(37, "Time");
        UDP_PORT_MAP.put(42, "Host Name Server");
        UDP_PORT_MAP.put(49, "TACACS Login");
        UDP_PORT_MAP.put(53, "DNS");
        UDP_PORT_MAP.put(67, "BOOTP");
        UDP_PORT_MAP.put(68, "BOOTP");
        UDP_PORT_MAP.put(69, "TFTP");
        UDP_PORT_MAP.put(80, "HTTP");
        UDP_PORT_MAP.put(88, "Kerberos");
        UDP_PORT_MAP.put(104, "DICOM");
        UDP_PORT_MAP.put(105, "CCSO Nameserver");
        UDP_PORT_MAP.put(107, "Rtelnet");
        UDP_PORT_MAP.put(108, "SNA");
        UDP_PORT_MAP.put(111, "ONC RPC");
        UDP_PORT_MAP.put(117, "UUCP");
        UDP_PORT_MAP.put(118, "SQL");
        UDP_PORT_MAP.put(123, "NTP");
        UDP_PORT_MAP.put(152, "BFTP");
        UDP_PORT_MAP.put(153, "SGMP");
        UDP_PORT_MAP.put(156, "SQL");
        UDP_PORT_MAP.put(158, "DMSP");
        UDP_PORT_MAP.put(161, "SNMP");
        UDP_PORT_MAP.put(162, "SNMPTRAP");
        UDP_PORT_MAP.put(177, "XDMCP");
        UDP_PORT_MAP.put(194, "IRC");
        UDP_PORT_MAP.put(199, "SMUX");
        UDP_PORT_MAP.put(213, "IPX");
        UDP_PORT_MAP.put(218, "MPP");
        UDP_PORT_MAP.put(220, "IMAP3");
        UDP_PORT_MAP.put(259, "ESRO");
        UDP_PORT_MAP.put(264, "BGMP");
        UDP_PORT_MAP.put(280, "http-mgmt");
        UDP_PORT_MAP.put(318, "TSP");
        UDP_PORT_MAP.put(319, "PTP");
        UDP_PORT_MAP.put(320, "PTP");
        UDP_PORT_MAP.put(350, "MATIP");
        UDP_PORT_MAP.put(351, "MATIP");
        UDP_PORT_MAP.put(366, "ODMR");
        UDP_PORT_MAP.put(387, "AURP");
        UDP_PORT_MAP.put(401, "UPS");
        UDP_PORT_MAP.put(427, "SLP");
        UDP_PORT_MAP.put(433, "NNSP");
        UDP_PORT_MAP.put(443, "HTTPS");
        UDP_PORT_MAP.put(444, "SNPP");
        UDP_PORT_MAP.put(445, "Microsoft-DS");
        UDP_PORT_MAP.put(464, "Kerberos");
        UDP_PORT_MAP.put(497, "Retrospect");
        UDP_PORT_MAP.put(500, "ISAKMP");
        UDP_PORT_MAP.put(502, "Modbus");
        UDP_PORT_MAP.put(513, "Who");
        UDP_PORT_MAP.put(514, "Syslog");
        UDP_PORT_MAP.put(517, "Talk");
        UDP_PORT_MAP.put(518, "NTalk");
        UDP_PORT_MAP.put(510, "RIP");
        UDP_PORT_MAP.put(521, "RIPng");
        UDP_PORT_MAP.put(524, "NCP");
        UDP_PORT_MAP.put(530, "RPC");
        UDP_PORT_MAP.put(546, "DHCPv6 client");
        UDP_PORT_MAP.put(547, "DHCPv6 server");
        UDP_PORT_MAP.put(554, "RTSP");
        UDP_PORT_MAP.put(560, "rmonitor");
        UDP_PORT_MAP.put(561, "monitor");
        UDP_PORT_MAP.put(563, "NNTPS");
        UDP_PORT_MAP.put(623, "ASF-RMCP");
        UDP_PORT_MAP.put(631, "IPP");
        UDP_PORT_MAP.put(635, "RLZ");
        UDP_PORT_MAP.put(639, "MSDP");
        UDP_PORT_MAP.put(643, "SANity");
        UDP_PORT_MAP.put(646, "LDP");
        UDP_PORT_MAP.put(800, "mdbs-daemon");
        UDP_PORT_MAP.put(830, "NETCONF over SSH");
        UDP_PORT_MAP.put(831, "NETCONF over BEEP");
        UDP_PORT_MAP.put(832, "NETCONF over HTTPS");
        UDP_PORT_MAP.put(833, "NETCONF");
        UDP_PORT_MAP.put(848, "GDOI");
        UDP_PORT_MAP.put(853, "DNS over TLS");
        UDP_PORT_MAP.put(861, "OWAMP");
        UDP_PORT_MAP.put(862, "TWAMP");
        UDP_PORT_MAP.put(989, "FTPS over TLS/SSL ");
        UDP_PORT_MAP.put(990, "FTPS over TLS/SSL");
        UDP_PORT_MAP.put(992, "Telnet over TLS/SSL");
        UDP_PORT_MAP.put(995, "POP3S");
    }

    /**
     * Gets type.
     *
     * @param protocol the protocol (TCP or UDP)
     * @param port     the port
     *
     * @return the type
     */
    public static String getType(String protocol, Integer port) {
        String type = null;

        if (protocol.toUpperCase().contains("TCP")) {
            type = TCP_PORT_MAP.get(port);
        } else if (protocol.toUpperCase().contains("UDP")) {
            type = UDP_PORT_MAP.get(port);
        }

        if (StringUtils.isEmpty(type)) {
            type = "Custom";
        }

        return type;
    }
}
//end of WellKnownPortUtil.java