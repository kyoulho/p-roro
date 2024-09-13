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
package io.playce.roro.svr.asmt.solaris;

/**
 * <pre>
 *
 *
 * </pre>
 *
 * @author Hoon Oh
 * @version 1.0
 */
public class SolarisCommand {

    public static String ARCHITECTURE = "uname -m";

    // public static String DISTRIBUTION_SOLARIS = "cat /etc/release";
    public static String DISTRIBUTION_SOLARIS = "uname -r";

    public static String CRONTAB = "/usr/bin/find /var/spool/cron/crontabs -type f";
    public static String GROUPS = "cat /etc/group | egrep -v '^#'";
    public static String HOSTNAME = "uname -n";


    /*
     * CPU Command
     * */
    public static String PROCESSOR_COUNT = "psrinfo -p";
    public static String PROCESSOR = "kstat -m cpu_info | grep brand | uniq";
    public static String PROCESSOR_CORES = "kstat cpu_info | grep core_id | uniq | wc -l";

    /*
     * Memroy Command
     * */
    public static String PAGESIZE = "pagesize";
    public static String MEM_FREE = "echo '::memstat' | mdb -k | grep Free";
    public static String MEM_TOTAL = "echo '::memstat' | mdb -k | grep Total | awk '{print $2}'";
    public static String SWAP_INFO = "LANG=C && export LANG && swap -s";


    public static String INTERFACES = "ifconfig -a";
    public static String INTERFACES_DEFAULT_GATEWAY = "netstat -r | grep default";


    /*
     * Kernel Command
     * */
    public static String KERNEL = "uname -v";
    public static String KERNEL_PARAM = "prctl $$ | tail +3";

    public static String NET_LISTEN_PORT = "netstat -na -P %s | grep LISTEN";
    public static String NET_PROC_INFO = "pfiles %s 2> /dev/null | grep 'port: %s'";
    public static String NET_PORT_LIST = "ptree -a | grep -v ptree | awk '{print $1\" \"$2};'";
    public static String NET_TRAFFICS = "netstat -na -P %s | grep -v LISTEN";

    public static String PARTITIONS = "df -k | tail +2";
    public static String PARTITIONS_TYPE = "df -n";

    public static String PROCESSES = "LANG=C && export LANG && ps -ef";

    public static String SHADOW = "cat /etc/shadow";

    public static String TIMEZONE_HIGHER_11 = "nlsadm get-timezone";
    public static String TIMEZONE_BELOW_11 = "cat /etc/TIMEZONE | grep 'TZ='";

    public static String USER_LIST = "cut -f1 -d: /etc/passwd";
    public static String ULIMIT = "LANG=C && export LANG && su %s -c 'ulimit -a'";

    public static String USERS = "/usr/bin/cat /etc/passwd | egrep -v '^#'";
    public static String ROUTE_TABLE = "netstat -rn";


    public static String LOCALE = "locale";
    public static String ENV = "env";
    public static String FSTAB = "cat /etc/vfstab";

    public static String DNS = "cat /etc/resolv.conf | egrep -v '^#'";
    public static String HOSTS = "cat /etc/hosts";
    public static String DAEMON_LIST = "LANG=C && export LANG && svcs -a | tail +2";

    public static String PRODUCT_NAME = "smbios -t SMB_TYPE_SYSTEM | grep Manufacturer | cut -d ':' -f 2";
    public static String PRODUCT_SERIAL = "smbios -t SMB_TYPE_SYSTEM | grep 'UUID'  | cut -d ':' -f 2";

    // SunOS 5.11 기준 awk에는 -F 옵션이 비정상적으로 동작하여 gawk로 변경
    // https://stackoverflow.com/questions/31327749/awk-bailing-out-at-source-line-1
    // public static String UPTIME = "LANG=C && export LANG && uptime | gawk -F , '{n=split($1,day,\" \"); split($2,hour,\":\")} END {if(n>3){print day[3]\" \"hour[1]\" \"hour[2]}else{split(day[3],hour,\":\"); print 0\" \"hour[1]\" \"hour[2]}}' | tr -d '\n'";
    // AbstractServerAssessment.getUptime() 에서 처리
    public static String UPTIME = "LANG=C && export LANG && uptime";

    public static String OS_FAIMLY = "uname";

    public static String PACKAGES = "pkginfo -l | awk '/PKGINST/ {print $2} /VERSION/ {print $2}'";
}
//end of SolarisCommand.java