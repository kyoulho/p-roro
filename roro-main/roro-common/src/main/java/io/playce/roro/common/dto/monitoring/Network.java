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
 * SangCheon Park   Jul 22, 2021		First Draft.
 */
package io.playce.roro.common.dto.monitoring;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 2.0.0
 */
@Getter
@Setter
@ToString
@JsonPropertyOrder({"date", "protocol", "state", "localAddress", "localPort", "foreignAddress", "foreignPort", "pid"})
public class Network {

    private String date;
    private String protocol;
    private String state;
    private String localAddress;
    private String localPort;
    private String foreignAddress;
    private String foreignPort;
    private String pid;
}
//end of Network.java