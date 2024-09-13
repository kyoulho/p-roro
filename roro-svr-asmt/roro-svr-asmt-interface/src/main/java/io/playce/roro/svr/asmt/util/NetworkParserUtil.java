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
package io.playce.roro.svr.asmt.util;

import io.playce.roro.svr.asmt.dto.common.interfaces.InterfaceInfo;
import io.playce.roro.svr.asmt.dto.common.interfaces.Ipv4Address;
import org.apache.commons.net.util.SubnetUtils;

/**
 * <pre>
 *
 *
 * </pre>
 *
 * @author Hoon Oh
 * @version 1.0
 */
public class NetworkParserUtil {

    public static String getBroadCase(String ipAddress) {
        SubnetUtils subnetUtils = new SubnetUtils(ipAddress);
        return subnetUtils.getInfo().getBroadcastAddress();
    }

    public static String getNetMask(String ipAddress) {
        SubnetUtils subnetUtils = new SubnetUtils(ipAddress);
        return subnetUtils.getInfo().getNetmask();
    }

    public static Ipv4Address getIpv4Address(String ipNetmask, String ip) {
        Ipv4Address address = new Ipv4Address();
        address.setAddress(ip);
        address.setNetmask(NetworkParserUtil.getNetMask(ipNetmask));
        address.setBroadcast(NetworkParserUtil.getBroadCase(ipNetmask));
        return address;
    }

    public static InterfaceInfo getInitInterface(String device, String gateway, String script, String macAddress) {
        InterfaceInfo info = new InterfaceInfo();
        info.setDevice(device);
        info.setGateway(gateway);
        info.setScript(script);
        info.setMacaddress(macAddress);
        return info;
    }

}
//end of NetworkParserUtil.java