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
 * Author			Date				Description
 * ---------------	----------------	------------
 * Hoon Oh          1월 20, 2022		First Draft.
 */
package io.playce.roro.svr.asmt.hpux;

/**
 * <pre>
 *
 *
 * </pre>
 *
 * @author Hoon Oh
 * @version 1.0
 */
public class HpuxCommand {
    public static String ARCHITECTURE = "getconf KERNEL_BITS";

    public static String CRONTAB1 = "/usr/bin/find /var/spool/cron/crontabs -type f";

    public static String DISTRIBUTION_HPUX = "/usr/sbin/swlist |egrep 'HPUX.*OE.*[AB].[0-9]+\\.[0-9]+'";

    public static String GROUPS = "/usr/bin/cat /etc/group | egrep -v '^#'";

    public static String HOSTNAME = "/usr/bin/hostname";

    public static String INTERFACES = "netstat -inw | tail -n+2 | awk '{print $1}' | xargs -I% ifconfig '%'";
    public static String INTERFACES_DEFAULT_GATEWAY = "netstat -rn | tail -n+3";
    public static String INTERFACES_MAC_ADDRESS = "lanscan | grep %s";

    public static String KERNEL = "uname -m";
    public static String KERNEL_PARAM = "kctune | tail -n+2 | awk '{print $1\" \"$2}'";

    public static String NET_LISTEN_PORT = "netstat -na | grep LISTEN";
    public static String NET_TRAFFICS = "netstat -na | grep -v LISTEN | egrep 'tcp|udp'";
    public static String NET_LSOF = "/usr/local/bin/lsof -i:%s 2> /dev/null |grep LISTEN | awk {'print $1\" \"$2'} | uniq | tail -1";

    public static String PARTITIONS = "bdf | tail -n+2";
    public static String PARTITIONS_TYPE = "fstyp %s";

    public static String PROCESSES = "/usr/bin/ps -ef";

    //TODO 추후 shadow 로직 확인 필요 현재는 결과가 없음.
    public static String SHADOW = "cat /etc/passwd";
    public static String TIMEZONE = "/usr/bin/env | grep TZ | awk -F '=' '{print $2}'";
    public static String USERS = "/usr/bin/cat /etc/passwd | egrep -v '^#'";
    public static String ROUTE_TABLE = "/usr/bin/netstat -rn | tail -n+3";


    public static String PRODUCT_SERIAL = "getconf MACHINE_SERIAL";
    public static String FIRMWARE_VERSION = "machinfo | grep 'Firmware revision'";

    public static String LVM_VGS = "vgdisplay -v";

    public static String LOCALE = "locale";
    public static String ENV = "env";
    public static String FSTAB = "cat /etc/fstab";

    public static String DNS = "cat /etc/resolv.conf | egrep -v '^#'";
    public static String HOSTS = "cat /etc/hosts";

    public static String ULIMIT = "su %s -c 'ulimit -a'";
    public static String USER_LIST = "cut -f1 -d: /etc/passwd";

    /*
     * CPU Command
     * */

    public static String PROCESSOR_11_23 = "/usr/contrib/bin/machinfo | grep 'vendor information'";
    public static String PROCESSOR_COUNT_11_23 = "/usr/contrib/bin/machinfo | grep 'Number of CPUs'";
    public static String PROCESSOR_CORES_11_23 = "ioscan -FkCprocessor | wc -l";

    public static String PROCESSOR_MACHINE_TYPE = "/usr/contrib/bin/machinfo | grep core | wc -l";

    public static String PROCESSOR1_11_31 = "/usr/contrib/bin/machinfo | grep Intel |cut -d' ' -f4-";
    public static String PROCESSOR1_COUNT_11_31 = "/usr/contrib/bin/machinfo | grep Intel";
    public static String PROCESSOR1_CORES_HYPER_THREADING_11_31 = "/usr/sbin/psrset | grep LCPU";
    public static String PROCESSOR1_CORES_LOGICAL_11_31 = "/usr/contrib/bin/machinfo | grep logical";

    public static String PROCESSOR2_11_31 = "/usr/contrib/bin/machinfo | grep Intel";
    public static String PROCESSOR2_COUNT_11_31 = "/usr/contrib/bin/machinfo | egrep 'socket[s]?$' | tail -1";
    public static String PROCESSOR2_CORES_11_31 = "/usr/contrib/bin/machinfo | grep -e '[0-9] core' | tail -1";

    /*
     * Memroy Command
     * */
    public static String MEM_FREE = "/usr/bin/vmstat | tail -1";
    public static String MEM_TOTAL = "/usr/contrib/bin/machinfo | grep Memory";
    public static String SWAP_FREE = "/usr/sbin/swapinfo -m -d -f -q";
    public static String SWAP_TOTAL = "/usr/sbin/swapinfo -m -d -f | egrep '^dev|^fs'";

    // public static String UPTIME = "uptime | awk -F , '{n=split($1,day,\" \"); split($2,hour,\":\")} END {if(n>3){print day[3]\" \"hour[1]\" \"hour[2]}else{split(day[3],hour,\":\"); print 0\" \"hour[1]\" \"hour[2]}}' | tr -d '\n'";
    // AbstractServerAssessment.getUptime() 에서 처리
    public static String UPTIME = "uptime";
    public static final String OS_FAIMLY = "uname";
    public static final String PACKAGES = "swlist | egrep -v '^#' | awk '{print $1, $2}'";
}
//end of HpuxCommand.java