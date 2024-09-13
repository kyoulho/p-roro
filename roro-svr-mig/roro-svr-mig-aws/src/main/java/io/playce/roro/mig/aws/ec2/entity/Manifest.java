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
 * http://docs.aws.amazon.com/AWSEC2/latest/APIReference/manifest.html
 * </pre>
 *
 * @author Sang-cheon Park
 * @version 1.0
 */
@XmlAccessorType(value = XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"version", "fileFormat", "importer", "selfDestructUrl", "_import"})
@XmlRootElement(name = "manifest")
public class Manifest {

    @XmlElement(required = true)
    protected String version;
    @XmlElement(name = "file-format", required = true)
    protected String fileFormat;
    @XmlElement(required = true)
    protected Importer importer;
    @XmlElement(name = "self-destruct-url", required = true)
    @XmlSchemaType(name = "anyURI")
    protected String selfDestructUrl;
    @XmlElement(name = "import", required = true)
    protected Import _import;

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String value) {
        this.version = value;
    }

    public String getFileFormat() {
        return this.fileFormat;
    }

    public void setFileFormat(String value) {
        this.fileFormat = value;
    }

    public Importer getImporter() {
        return this.importer;
    }

    public void setImporter(Importer value) {
        this.importer = value;
    }

    public String getSelfDestructUrl() {
        return this.selfDestructUrl;
    }

    public void setSelfDestructUrl(String value) {
        this.selfDestructUrl = value;
    }

    public Import getImport() {
        return this._import;
    }

    public void setImport(Import value) {
        this._import = value;
    }
}
// end of Manifest.java