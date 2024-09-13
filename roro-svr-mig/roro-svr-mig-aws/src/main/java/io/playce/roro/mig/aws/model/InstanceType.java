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
 * Sang-cheon Park	2020. 3. 29.		First Draft.
 */
package io.playce.roro.mig.aws.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Sang -cheon Park
 * @version 1.0
 */
@Getter
@Setter
@ToString
public class InstanceType {

    /**
     * The Type.
     */
    private String type;
    /**
     * The Family.
     */
    private String family;
    /**
     * The vCpus.
     */
    private String vCPUs;
    /**
     * The Memory.
     */
    private String memory;
    /**
     * The Memory.
     */
    private int group;
}
//end of InstanceType.java