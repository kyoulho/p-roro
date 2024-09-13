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
 * Author			Date				Description
 * ---------------	----------------	------------
 * Hoon Oh          11월 10, 2021		First Draft.
 */
package io.playce.roro.svr.asmt.dto.common.interfaces;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

/**
 * <pre>
 *
 *
 * </pre>
 *
 * @author Hoon Oh
 * @version 1.0
 */
@Getter
@Setter
public class InterfaceInfo {
    private String device;
    private String macaddress;
    private String gateway;
    private String script;
    private List<Ipv4Address> ipv4 = new ArrayList<>();
    private List<Ipv6Address> ipv6 = new ArrayList<>();
    @JsonProperty("rxBytes/s")
    private Long rxBytes;
    @JsonProperty("txBytes/s")
    private Long txBytes;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InterfaceInfo that = (InterfaceInfo) o;

        if (!Objects.equals(device, that.device)) return false;
        if (!Objects.equals(macaddress, that.macaddress)) return false;
        if (!Objects.equals(gateway, that.gateway)) return false;
        if (!Objects.equals(script, that.script)) return false;
        Set<Ipv4Address> ipv4Addresses = new HashSet<>(ipv4);
        Set<Ipv4Address> thatIpv4Addresses = new HashSet<>(that.ipv4);
        if (!ipv4Addresses.equals(thatIpv4Addresses)) return false;
        Set<Ipv6Address> ipv6Addresses = new HashSet<>(ipv6);
        Set<Ipv6Address> thatIpv6Addresses = new HashSet<>(that.ipv6);
        if (!ipv6Addresses.equals(thatIpv6Addresses)) return false;
        return ipv6.equals(that.ipv6);
    }

    @Override
    public int hashCode() {
        int result = device != null ? device.hashCode() : 0;
        result = 31 * result + (macaddress != null ? macaddress.hashCode() : 0);
        result = 31 * result + (gateway != null ? gateway.hashCode() : 0);
        result = 31 * result + (script != null ? script.hashCode() : 0);
        return result;
    }
}
//end of InterfaceInfo.java