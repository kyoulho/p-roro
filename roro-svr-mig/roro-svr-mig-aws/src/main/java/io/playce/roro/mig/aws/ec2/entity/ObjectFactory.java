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

import javax.xml.bind.annotation.XmlRegistry;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Sang-cheon Park
 * @version 1.0
 */
@XmlRegistry
public class ObjectFactory {

    public ByteRange createByteRange() {
        return new ByteRange();
    }

    public Part createPart() {
        return new Part();
    }

    public Manifest createManifest() {
        return new Manifest();
    }

    public Import createImport() {
        return new Import();
    }

    public Importer createImporter() {
        return new Importer();
    }

    public Parts createParts() {
        return new Parts();
    }
}
// end of ObjectFactory.java