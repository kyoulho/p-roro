/*
 * Copyright 2021 The Playce-RoRo Project.
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
 * Hoon Oh       11월 24, 2021            First Draft.
 */
package io.playce.roro.svr.asmt.aix;

/**
 * <pre>
 *
 *
 * </pre>
 *
 * @author Hoon Oh
 * @version 1.0
 */
public class AixCommand {
    public static String ARCHITECTURE = "getconf KERNEL_BITMODE";

    public static String CRONTAB1 = "/usr/bin/find /var/spool/cron/crontabs -type file";

    public static String DISTRIBUTION_AIX = "/usr/bin/oslevel";

    public static String GROUPS = "/usr/bin/cat /etc/group | egrep -v '^#'";

    public static String HOSTNAME = "/usr/bin/hostname";

    public static String INTERFACES = "/etc/ifconfig -a";
    public static String INTERFACES_DEFAULT_GATEWAY = "netstat -rn | grep default";
    public static String INTERFACES_MAC_ADDRESS = "entstat -d %s | egrep Hardware | uniq";

    public static String KERNEL = "lslpp -l | grep bos.mp";
    public static String KERNEL_PARAM = "/usr/sbin/lsattr -E -l sys0";

    public static String NET_LISTEN_PORT = "/usr/bin/netstat -Aan | grep LISTEN | egrep 'tcp|udp'";
    public static String NET_RMSOCK = "/usr/sbin/rmsock %s tcpcb";
    public static String NET_TRAFFICS = "/usr/bin/netstat -Aan | grep -v LISTEN | egrep 'tcp|udp'";

    public static String PARTITIONS = "/usr/bin/df -m";
    public static String PARTITIONS_TYPE = "/usr/sbin/lsvg -l rootvg";

    public static String EXSTRA_PARTITIONS = "/usr/sbin/lsvg -l rootvg";

    public static String PROCESSES = "/usr/bin/ps -ef";

    public static String PROCESSOR_COUNT = "/usr/sbin/lsdev -Cc processor";
    public static String PROCESSOR = "/usr/sbin/lsattr -El %s -a type";
    public static String PROCESSOR_CORES = "/usr/sbin/lsattr -El %s -a smt_threads";

    public static String FIRMWARE_VERSION = "/usr/sbin/lsattr -El sys0 -a fwversion";

    public static String DMI_FACTS = "/usr/sbin/lsconf";

    public static String ROUTE_TABLE = "/usr/bin/netstat -rn";

    public static String SHADOW = "/usr/bin/cat /etc/security/passwd |egrep ':|password' | sed 's/password = //g' | tr -d '\t'";

    public static String MEMORY_FACTS = "/usr/bin/vmstat -v";
    public static String SWAP_FACTS = "/usr/sbin/lsps -s";

    public static String ULIMITS = "/usr/bin/cat /etc/security/limits | egrep -v '^\\*|^$'";

    public static String USERS = "/usr/bin/cat /etc/passwd | egrep -v '^#'";
    public static String USER_LIST = "/usr/bin/cut -f1 -d: /etc/passwd";

    public static String FILESYSTEM = "/usr/bin/cat /etc/filesystems";

    public static String LVM_VGS = "/usr/sbin/lsvg -o | /usr/bin/xargs /usr/sbin/lsvg -p";
    public static String LVM_PVS = "/usr/sbin/lsvg %s";
    public static String LVM_LVS = "/usr/sbin/lsvg -l %s";

    public static String DAEMON_LIST = "/usr/bin/lssrc -a | grep -v PID";

    public static String SECURITY_PASSWORD = "/usr/bin/cat /etc/security/user";
    public static String SECURITY_LOGIN = "/usr/bin/cat /etc/security/login.cfg";

    public static String LOCALE = "locale";
    public static String ENV = "env";

    public static String DNS = "/usr/bin/cat /etc/resolv.conf | egrep -v '^#'";

    public static String TIMEZONE1 = "/usr/bin/env | grep TZ | awk -F '=' '{print $2}'";

    public static String HOSTS = "/usr/bin/cat /etc/hosts";

    // public static String UPTIME = "uptime | awk -F , '{n=split($1,day,\" \"); split($2,hour,\":\")} END {if(n>3){print day[3]\" \"hour[1]\" \"hour[2]}else{split(day[3],hour,\":\"); print 0\" \"hour[1]\" \"hour[2]}}' | tr -d '\n'";
    // AbstractServerAssessment.getUptime() 에서 처리
    public static String UPTIME = "uptime";

    public static final String OS_FAIMLY = "uname";
    public static final String PACKAGES = "lslpp -lc | egrep -v '^#' | awk -F':' '{print $2, $3}'";
}
//end of AixCommand.java