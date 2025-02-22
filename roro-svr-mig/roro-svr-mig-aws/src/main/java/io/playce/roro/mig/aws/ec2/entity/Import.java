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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Sang-cheon Park
 * @version 1.0
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Import", propOrder = {"size", "volumeSize", "parts"})
public class Import {

    protected long size;
    @XmlElement(name = "volume-size")
    protected long volumeSize;
    @XmlElement(required = true)
    protected Parts parts;

    public long getSize() {
        return this.size;
    }

    public void setSize(long value) {
        this.size = value;
    }

    public long getVolumeSize() {
        return this.volumeSize;
    }

    public void setVolumeSize(long value) {
        this.volumeSize = value;
    }

    public Parts getParts() {
        return this.parts;
    }

    public void setParts(Parts value) {
        this.parts = value;
    }
}
// end of Import.java