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
 * Hoon Oh       1월 28, 2022            First Draft.
 */
package io.playce.roro.jpa.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * <pre>
 *
 *
 * </pre>
 *
 * @author Hoon Oh
 * @version 1.0
 */
@Entity
@Getter
@Setter
@Table(name = "middleware_instance")
@ToString
public class MiddlewareInstance {
    @Id
    private Long middlewareInstanceId;
    private String middlewareInstanceName;
    private String middlewareInstancePath;
    private String middlewareConfigPath;
    private String middlewareProfileName;
    private String middlewareCellName;
    private String middlewareNodeName;
    private String middlewareServerName;
    private String runningUser;
    private String javaVersion;
    private Long registUserId;
    private Date registDatetime;
}
//end of MiddlewareInstance.java