/*
 * Copyright 2022 The playce-roro-v3 Project.
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
 * SangCheon Park   Jan 26, 2022		    First Draft.
 */
package io.playce.roro.common.dto.preconfig;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
@Getter
@Setter
public class PreConfigDto {

    @Getter
    @Builder
    public static class UserGroups {
        private List<User> users;
        private List<Group> groups;
    }

    @Getter
    @Setter
    public static class Profile {
        private String profile;
    }

    @Getter
    @Setter
    public static class Crontab {
        private String name;
        private String crontab;
    }

    @Getter
    @Setter
    public static class File {
        private String source;
        private String target;
        private String type;
        private long size;
        private String ownerGroup;
        private String ownerUser;
    }

    @Getter
    @Setter
    public static class User {
        private Integer uid;
        private String userName;
        private String homeDir;
        private String userPassword;
    }

    @Getter
    @Setter
    public static class Group {
        private Integer gid;
        private String groupName;
    }

    public static List<Group> convertMapToGroup(Map<String, Object> groupMap, Map<String, Object> defInfo) {
        List<Group> groupList = new ArrayList<>();

        int gidMin = 1000;
        int gidMax = 60000;
        if (defInfo != null) {
            gidMin = Integer.parseInt((String) defInfo.get("gidMin"));
            gidMax = Integer.parseInt((String) defInfo.get("gidMax"));
        }

        for (String key : groupMap.keySet()) {
            Map<String, Object> subGroupMap = (Map<String, Object>) groupMap.get(key);
            int gid = Integer.parseInt((String) subGroupMap.get("gid"));

            if (gid >= gidMin && gid <= gidMax) {
                Group group = new Group();
                group.setGroupName(key);
                group.setGid(gid);
                groupList.add(group);
            }
        }

        addLinuxDefaultGroup(groupList, gidMin);

        return groupList;
    }

    private static List<Group> addLinuxDefaultGroup(List<Group> groupList, int gidMin) {
        Group rootGroup = new Group();
        rootGroup.setGroupName("root");
        rootGroup.setGid(0);
        groupList.add(rootGroup);

        if (gidMin > 1) {
            Group wheelGroup = new Group();
            wheelGroup.setGroupName("wheel");
            wheelGroup.setGid(10);
            groupList.add(wheelGroup);
        }

        return groupList;
    }

    public static List<User> convertMapToUser(Map<String, Object> userMap, Map<String, String> shadowMap, Map<String, Object> defInfo) {
        List<User> userList = new ArrayList<>();

        int uidMin = 1000;
        int uidMax = 60000;
        if (defInfo != null) {
            uidMin = Integer.parseInt((String) defInfo.get("uidMin"));
            uidMax = Integer.parseInt((String) defInfo.get("uidMax"));
        }

        for (String key : userMap.keySet()) {
            Map<String, String> subUserMap = (Map<String, String>) userMap.get(key);
            int uid = Integer.parseInt(subUserMap.get("uid"));
            String username = key;

            if ((uid >= uidMin && uid <= uidMax) || username.equals("root")) {
                User user = new User();
                user.setUid(uid);
                user.setUserName(key);
                user.setUserPassword(getPassword(shadowMap, key));
                user.setHomeDir(subUserMap.get("homeDir"));
                userList.add(user);
            }
        }

        return userList;
    }

    private static String getPassword(Map<String, String> shadowMap, String username) {
        String password = null;

        for (String key : shadowMap.keySet()) {
            if (key.equals(username)) {
                password = shadowMap.get(key);
                break;
            }
        }

        return password;
    }

    public static List<Crontab> convertMapToCrontab(Map<String, String> crontabMap) {
        List<Crontab> crontabList = new ArrayList<>();

        if (crontabMap != null) {
            for (String key : crontabMap.keySet()) {
                Crontab crontab = new Crontab();
                crontab.setName(key);
                crontab.setCrontab(crontabMap.get(key));
                crontabList.add(crontab);
            }
        }

        return crontabList;
    }
}
//end of PreConfigDto.java