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

import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 * Security Group 상세 정보가 저장될 클래스
 * </pre>
 *
 * @author Sang-cheon Park
 * @version 1.0
 */
public class SecurityGroupDetail extends Configuration {

    private String groupId;
    private String groupName;
    private String nameTag;
    private String description;
    private List<Permission> permissions;

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
     * @return the groupName
     */
    public String getGroupName() {
        return groupName;
    }

    /**
     * @param groupName the groupName to set
     */
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    /**
     * @return the nameTag
     */
    public String getNameTag() {
        return nameTag;
    }

    /**
     * @param nameTag the nameTag to set
     */
    public void setNameTag(String nameTag) {
        this.nameTag = nameTag;
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

    /**
     * @return the permissions
     */
    public List<Permission> getPermissions() {
        if (permissions == null) {
            permissions = new ArrayList<Permission>();
        }

        return permissions;
    }

    /**
     * @param permissions the permissions to set
     */
    public void setPermissions(List<Permission> permissions) {
        this.permissions = permissions;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "SecurityGroupDetail [groupId=" + groupId + ", groupName=" + groupName + ", nameTag=" + nameTag
                + ", description=" + description + ", permissions=" + permissions + "]";
    }
}
//end of SecurityGroupDetail.java