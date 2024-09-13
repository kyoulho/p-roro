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
 * Sang-cheon Park	2020. 3. 16.		First Draft.
 */
package io.playce.roro.mig.aws.ec2.entity;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Sang-cheon Park
 * @version 1.0
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Parts", propOrder = {"part"})
public class Parts {

    @XmlElement(required = true)
    protected List<Part> part;
    @XmlAttribute
    protected Integer count;

    public List<Part> getPart() {
        if (this.part == null) {
            this.part = new ArrayList<Part>();
        }

        return this.part;
    }

    public Integer getCount() {
        return this.count;
    }

    public void setCount(Integer value) {
        this.count = value;
    }
}
// end of Parts.java