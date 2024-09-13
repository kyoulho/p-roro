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

/**
 * <pre>
 *
 * </pre>
 *
 * @author Sang-cheon Park
 * @version 1.0
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Part", propOrder = {"byteRange", "key", "headUrl", "getUrl", "deleteUrl"})
public class Part {

    @XmlElement(name = "byte-range", required = true)
    protected ByteRange byteRange;
    @XmlElement(required = true)
    protected String key;
    @XmlElement(name = "head-url", required = true)
    @XmlSchemaType(name = "anyURI")
    protected String headUrl;
    @XmlElement(name = "get-url", required = true)
    @XmlSchemaType(name = "anyURI")
    protected String getUrl;
    @XmlElement(name = "delete-url")
    @XmlSchemaType(name = "anyURI")
    protected String deleteUrl;
    @XmlAttribute
    protected Integer index;

    public ByteRange getByteRange() {
        return this.byteRange;
    }

    public void setByteRange(ByteRange value) {
        this.byteRange = value;
    }

    public String getKey() {
        return this.key;
    }

    public void setKey(String value) {
        this.key = value;
    }

    public String getHeadUrl() {
        return this.headUrl;
    }

    public void setHeadUrl(String value) {
        this.headUrl = value;
    }

    public String getGetUrl() {
        return this.getUrl;
    }

    public void setGetUrl(String value) {
        this.getUrl = value;
    }

    public String getDeleteUrl() {
        return this.deleteUrl;
    }

    public void setDeleteUrl(String value) {
        this.deleteUrl = value;
    }

    public Integer getIndex() {
        return this.index;
    }

    public void setIndex(Integer value) {
        this.index = value;
    }
}
// end of Part.java