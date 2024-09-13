/*
 * Copyright 2020 The Playce-RoRo Project.
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
 * Sang-cheon Park	2020. 3. 31.		First Draft.
 */
package io.playce.roro.mig.aws.model;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Sang-cheon Park
 * @version 1.0
 */
public class SubnetDetail extends Configuration {

    private String subnetId;
    private String subnetName;
    private String subnetCidr;
    private String state;
    private Integer availableIPs;
    private String availabilityZone;
    private Boolean autoAssignPublicIP;

    /**
     * @return the subnetId
     */
    public String getSubnetId() {
        return subnetId;
    }

    /**
     * @param subnetId the subnetId to set
     */
    public void setSubnetId(String subnetId) {
        this.subnetId = subnetId;
    }

    /**
     * @return the subnetName
     */
    public String getSubnetName() {
        return subnetName;
    }

    /**
     * @param subnetName the subnetName to set
     */
    public void setSubnetName(String subnetName) {
        this.subnetName = subnetName;
    }

    /**
     * @return the subnetCidr
     */
    public String getSubnetCidr() {
        return subnetCidr;
    }

    /**
     * @param subnetCidr the subnetCidr to set
     */
    public void setSubnetCidr(String subnetCidr) {
        this.subnetCidr = subnetCidr;
    }

    /**
     * @return the state
     */
    public String getState() {
        return state;
    }

    /**
     * @param state the state to set
     */
    public void setState(String state) {
        this.state = state;
    }

    /**
     * @return the availableIPs
     */
    public Integer getAvailableIPs() {
        return availableIPs;
    }

    /**
     * @param availableIPs the availableIPs to set
     */
    public void setAvailableIPs(Integer availableIPs) {
        this.availableIPs = availableIPs;
    }

    /**
     * @return the availabilityZone
     */
    public String getAvailabilityZone() {
        return availabilityZone;
    }

    /**
     * @param availabilityZone the availabilityZone to set
     */
    public void setAvailabilityZone(String availabilityZone) {
        this.availabilityZone = availabilityZone;
    }

    /**
     * @return the autoAssignPublicIP
     */
    public Boolean getAutoAssignPublicIP() {
        return autoAssignPublicIP;
    }

    /**
     * @param autoAssignPublicIP the autoAssignPublicIP to set
     */
    public void setAutoAssignPublicIP(Boolean autoAssignPublicIP) {
        this.autoAssignPublicIP = autoAssignPublicIP;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "SubnetDetail [subnetId=" + subnetId + ", subnetName=" + subnetName + ", subnetCidr=" + subnetCidr
                + ", state=" + state + ", availableIPs=" + availableIPs + ", availabilityZone=" + availabilityZone
                + ", autoAssignPublicIP=" + autoAssignPublicIP + "]";
    }
}
//end of SubnetDetail.java