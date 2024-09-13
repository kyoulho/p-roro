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
 * Hoon Oh       11월 23, 2021            First Draft.
 */
package io.playce.roro.svr.asmt.linux;

/**
 * <pre>
 *
 *
 * </pre>
 *
 * @author Hoon Oh
 * @version 1.0
 */
public class CommonCommand {

    public static String ARCHITECTURE = "uname -m";

    //public static String FIREWALL_RULE = "/sbin/iptables -L --line-number -n";
   // public static String FIREWALL_EXTRA_RULE = "/sbin/iptables -t nat -L --line-number -n";
    public static String FIREWALL_RULE = "/sbin/iptables -L -n";
    public static String FIREWALL_EXTRA_RULE = "/sbin/iptables -t nat -L -n";

    public static String CRONTAB1 = "find /var/spool/cron -type f";
    public static String CRONTAB2 = "find /var/spool/cron/crontabs -type f";

    public static String DISTRIBUTION_REDHAT = "cat /etc/redhat-release";
    public static String DISTRIBUTION_AMAZON = "cat /etc/system-release";

    public static String GROUPS = "cat /etc/group | egrep -v '^#'";

    public static String HOSTNAME = "uname -n";

    public static String INTERFACES = "/sbin/ip addr";
    public static String INTERFACES_DEFAULT_GATEWAY = "/sbin/ip route | grep default";
    public static String INTERFACES_NETWORK_SCRIPTS = "ls /etc/sysconfig/network-scripts/ifcfg-%s";

    public static String INTERFACES_RX_TX_SCRIPTS1 = "sar -n DEV | egrep '%s($|\\s)' | grep -i average | awk '{print $5, $6}'";
    public static String INTERFACES_RX_TX_SCRIPTS2 = "cat /sys/class/net/%s/statistics/rx_bytes;cat /sys/class/net/%s/statistics/tx_bytes;cat /proc/uptime | awk -F'.' '{print $1}'";

    public static String KERNEL = "uname -r";
    public static String KERNEL_PARAM = "/sbin/sysctl -a";

    //    public static String NET_LISTEN_PORT = "netstat -nap | grep LISTEN";
    public static String NET_LISTEN_PORT = "netstat -nap | grep LISTEN | grep -v LISTENING";
    public static String NET_TRAFFICS = "netstat -nap | tail -n+3 | grep -v LISTEN | egrep 'tcp|udp'";
//    public static String NET_TRAFFICS = "netstat -nap | tail -n+3 | grep -v LISTEN | egrep 'tcp|udp' | grep -v '127.0.0.1'";

    public static String PARTITIONS = "df -PTm | tail -n+2";

    public static String PROCESSES = "ps -ef";

    public static String CPU_FACTS = "lscpu";

    public static String SHADOW = "cat /etc/shadow";

    public static String MEMORY_FACTS = "vmstat -s";

    public static String TIMEZONE1 = "timedatectl | grep \"Time zone\"";
    public static String TIMEZONE2 = "cat /etc/sysconfig/clock | grep ZONE";

    public static String USER_LIST = "cut -f1 -d: /etc/passwd";
    public static String ULIMIT = "su - %s --shell /bin/bash -c 'ulimit -a'";
    public static String USERS = "cat /etc/passwd | egrep -v '^#'";

    public static String ROUTE_TABLE = "netstat -rn | tail -n+3";

    public static String ENV = "env";

    public static String LOCALE = "locale";

    public static String LOGIN_DEF = "cat /etc/login.defs";

    public static String LVM_VGS = "vgs | tail -n+2";
    public static String LVM_LVS = "lvdisplay";
    public static String LVM_PVS = "pvdisplay";

    public static String SECURITY_PASSWORD = "cat /etc/login.defs";

    public static String FSTAB = "cat /etc/fstab";

    public static String DAEMON_LIST = "systemctl list-units --type service | tail -n+2 | head -n-6";
    public static String DAEMON_LIST_LOWDER_7 = "chkconfig --list";

    public static String DNS = "cat /etc/resolv.conf | egrep -v '^#'";

    // public static String UPTIME = "uptime | awk -F , '{n=split($1,day,\" \"); split($2,hour,\":\")} END {if(n>3){print day[3]\" \"hour[1]\" \"hour[2]}else{split(day[3],hour,\":\"); print 0\" \"hour[1]\" \"hour[2]}}' | tr -d '\\n'";
    // AbstractServerAssessment.getUptime() 에서 처리
    public static String UPTIME = "uptime";

    public static String HOSTS = "cat /etc/hosts";

}
//end of CommonCommand.java