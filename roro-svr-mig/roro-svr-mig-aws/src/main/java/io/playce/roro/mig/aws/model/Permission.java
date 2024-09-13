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
 * Sang-cheon Park	2020. 3. 30.		First Draft.
 */
package io.playce.roro.mig.aws.model;

/**
 * <pre>
 * Security Group permission 정보가 저장될 클래스
 * </pre>
 *
 * @author Sang-cheon Park
 * @version 1.0
 */
public class Permission extends Configuration {

    private String groupId;
    private String protocol;
    private Integer fromPort;
    private Integer toPort;
    private String source;
    private String description;

    /**
     * @return the groupId
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * @param groupId the groupId to set
     */
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    /**
     * @return the protocol
     */
    public String getProtocol() {
        if (protocol != null && protocol.toUpperCase().equals("ALL")) {
            protocol = "-1";
        }

        return protocol;
    }

    /**
     * @param protocol the protocol to set
     */
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    /**
     * @return the fromPort
     */
    public Integer getFromPort() {
        return fromPort;
    }

    /**
     * @param fromPort the fromPort to set
     */
    public void setFromPort(Integer fromPort) {
        this.fromPort = fromPort;
    }

    /**
     * @return the toPort
     */
    public Integer getToPort() {
        return toPort;
    }

    /**
     * @param toPort the toPort to set
     */
    public void setToPort(Integer toPort) {
        this.toPort = toPort;
    }

    /**
     * @return the source
     */
    public String getSource() {
        return source;
    }

    /**
     * @param source the source to set
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Permission [groupId=" + groupId + ", protocol=" + protocol + ", fromPort=" + fromPort + ", toPort="
                + toPort + ", source=" + source + ", description=" + description + "]";
    }
}
//end of Permission.java