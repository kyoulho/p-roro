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
 * Hoon Oh       1월 07, 2022            First Draft.
 */
package io.playce.roro.svr.asmt;

/**
 * <pre>
 *
 *
 * </pre>
 *
 * @author Hoon Oh
 * @version 1.0
 */
public enum AssessmentItems {

    /*
     * System & Hardware Information
     * */
    ARCHITECTURE,
    FIREWALL_RULE,
    FIREWALL_EXTRA_RULE,
    DISTRIBUTION,
    FIRMWARE_VERSION,
    KERNEL,
    KERNEL_PARAM,
    PROCESSOR,
    PROCESSOR_COUNT,
    PROCESSOR_CORES,
    DMI_FACTS,
    PRODUCT_SERIAL,
    PRODUCT_NAME,
    SWAP_FACTS,
    CPU_FACTS,

    /*
     * Memory
     * */
    MEMORY_FACTS,
    MEM_FREE,
    MEM_TOTAL,
    SWAP_FREE,
    SWAP_TOTAL,

    /*
     * User & Group
     * */
    USERS,
    USER_LIST,
    CRONTAB1,
    CRONTAB2,
    GROUPS,
    ULIMITS,
    SHADOWS,
    ENV,

    /*
     * Network
     * */
    INTERFACES,
    INTERFACES_DEFAULT_GATEWAY,
    ROUTE_TABLE,
    NET_LISTEN_PORT,
    NET_TRAFFICS,

    /*
     * Volume
     * */
    PARTITIONS,
    PARTITIONS_TYPE,
    EXSTRA_PARTITIONS,
    FSTAB,
    FILESYSTEM,
    LVM_VGS,


    /*
     * Security
     */
    SECURITY_LOGIN,
    SECURITY_PASSWORD,


    /*
     * Processes
     */
    PROCESSES,
    DAEMON_LIST,
    DAEMON_LIST_LOWDER_7,

    /*
     * Settings
     */
    HOSTNAME,
    DNS,
    LOCALE,
    HOSTS,
    LOGIN_DEF,
    TIMEZONE1,
    TIMEZONE2,

    UPTIME,

    PACKAGES,
    OS_FAMILY;

}
//end of AssessmentItems.java