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
public class Configuration {

    private String search;
    private String region;
    private String account;
    private String vpcId;
    private String vpcName;

    /**
     * @return the search
     */
    public String getSearch() {
        return search;
    }

    /**
     * @param search the search to set
     */
    public void setSearch(String search) {
        this.search = search;
    }

    /**
     * @return the region
     */
    public String getRegion() {
        return region;
    }

    /**
     * @param region the region to set
     */
    public void setRegion(String region) {
        this.region = region;
    }

    /**
     * @return the account
     */
    public String getAccount() {
        return account;
    }

    /**
     * @param account the account to set
     */
    public void setAccount(String account) {
        this.account = account;
    }

    /**
     * @return the vpcId
     */
    public String getVpcId() {
        return vpcId;
    }

    /**
     * @param vpcId the vpcId to set
     */
    public void setVpcId(String vpcId) {
        this.vpcId = vpcId;
    }

    /**
     * @return the vpcName
     */
    public String getVpcName() {
        return vpcName;
    }

    /**
     * @param vpcName the vpcName to set
     */
    public void setVpcName(String vpcName) {
        this.vpcName = vpcName;
    }
}
//end of Configuration.java