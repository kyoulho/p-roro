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
public class InstanceTypes {

    private static List<InstanceType> instanceTypes;

    static {
        instanceTypes = new ArrayList<InstanceType>();

        // General purpose (t2.micro | t2.small | t2.medium | m3.medium | m3.large | m3.xlarge | m3.2xlarge)
        InstanceType instanceType = new InstanceType();
        instanceType.setType("t2.micro");
        instanceType.setFamily("General purpose");
        instanceType.setVCPUs("1");
        instanceType.setMemory("1");
        instanceTypes.add(instanceType);

        instanceType = new InstanceType();
        instanceType.setType("t2.small");
        instanceType.setFamily("General purpose");
        instanceType.setVCPUs("1");
        instanceType.setMemory("2");
        instanceTypes.add(instanceType);

        instanceType = new InstanceType();
        instanceType.setType("t2.medium");
        instanceType.setFamily("General purpose");
        instanceType.setVCPUs("2");
        instanceType.setMemory("4");
        instanceTypes.add(instanceType);

        instanceType = new InstanceType();
        instanceType.setType("m3.medium");
        instanceType.setFamily("General purpose");
        instanceType.setVCPUs("1");
        instanceType.setMemory("3.75");
        instanceTypes.add(instanceType);

        instanceType = new InstanceType();
        instanceType.setType("m3.large");
        instanceType.setFamily("General purpose");
        instanceType.setVCPUs("2");
        instanceType.setMemory("7.5");
        instanceTypes.add(instanceType);

        instanceType = new InstanceType();
        instanceType.setType("m3.xlarge");
        instanceType.setFamily("General purpose");
        instanceType.setVCPUs("4");
        instanceType.setMemory("15");
        instanceTypes.add(instanceType);

        instanceType = new InstanceType();
        instanceType.setType("m3.2xlarge");
        instanceType.setFamily("General purpose");
        instanceType.setVCPUs("8");
        instanceType.setMemory("30");
        instanceTypes.add(instanceType);

        // Compute optimized (c3.large | c3.xlarge | c3.2xlarge | c3.4xlarge | c3.8xlarge | cc1.4xlarge)
        instanceType = new InstanceType();
        instanceType.setType("c3.large");
        instanceType.setFamily("Compute optimized");
        instanceType.setVCPUs("2");
        instanceType.setMemory("3.75");
        instanceTypes.add(instanceType);

        instanceType = new InstanceType();
        instanceType.setType("c3.xlarge");
        instanceType.setFamily("Compute optimized");
        instanceType.setVCPUs("4");
        instanceType.setMemory("7.5");
        instanceTypes.add(instanceType);

        instanceType = new InstanceType();
        instanceType.setType("c3.2xlarge");
        instanceType.setFamily("Compute optimized");
        instanceType.setVCPUs("8");
        instanceType.setMemory("15");
        instanceTypes.add(instanceType);

        instanceType = new InstanceType();
        instanceType.setType("c3.4xlarge");
        instanceType.setFamily("Compute optimized");
        instanceType.setVCPUs("16");
        instanceType.setMemory("30");
        instanceTypes.add(instanceType);

        instanceType = new InstanceType();
        instanceType.setType("c3.8xlarge");
        instanceType.setFamily("Compute optimized");
        instanceType.setVCPUs("32");
        instanceType.setMemory("60");
        instanceTypes.add(instanceType);

//		instanceType = new InstanceType();
//		instanceType.setType("cc1.4xlarge");
//		instanceType.setFamily("Compute optimized");
//		instanceType.setVCPUs("");
//		instanceType.setMemory("");
//		instanceTypes.add(instanceType);

        // Memory optimized (r3.large | r3.xlarge | r3.2xlarge | r3.4xlarge | r3.8xlarge)
        instanceType = new InstanceType();
        instanceType.setType("r3.large");
        instanceType.setFamily("Memory optimized");
        instanceType.setVCPUs("2");
        instanceType.setMemory("15");
        instanceTypes.add(instanceType);

        instanceType = new InstanceType();
        instanceType.setType("r3.xlarge");
        instanceType.setFamily("Memory optimized");
        instanceType.setVCPUs("4");
        instanceType.setMemory("30.5");
        instanceTypes.add(instanceType);

        instanceType = new InstanceType();
        instanceType.setType("r3.2xlarge");
        instanceType.setFamily("Memory optimized");
        instanceType.setVCPUs("8");
        instanceType.setMemory("61");
        instanceTypes.add(instanceType);

        instanceType = new InstanceType();
        instanceType.setType("r3.4xlarge");
        instanceType.setFamily("Memory optimized");
        instanceType.setVCPUs("16");
        instanceType.setMemory("122");
        instanceTypes.add(instanceType);

        instanceType = new InstanceType();
        instanceType.setType("r3.8xlarge");
        instanceType.setFamily("Memory optimized");
        instanceType.setVCPUs("32");
        instanceType.setMemory("244");
        instanceTypes.add(instanceType);

        // Storage optimized (i2.xlarge | i2.2xlarge | i2.4xlarge | i2.8xlarge)
        instanceType = new InstanceType();
        instanceType.setType("i2.xlarge");
        instanceType.setFamily("Storage optimized");
        instanceType.setVCPUs("4");
        instanceType.setMemory("30.5");
        instanceTypes.add(instanceType);

        instanceType = new InstanceType();
        instanceType.setType("i2.2xlarge");
        instanceType.setFamily("Storage optimized");
        instanceType.setVCPUs("8");
        instanceType.setMemory("61");
        instanceTypes.add(instanceType);

        instanceType = new InstanceType();
        instanceType.setType("i2.4xlarge");
        instanceType.setFamily("Storage optimized");
        instanceType.setVCPUs("16");
        instanceType.setMemory("122");
        instanceTypes.add(instanceType);

        instanceType = new InstanceType();
        instanceType.setType("i2.8xlarge");
        instanceType.setFamily("Storage optimized");
        instanceType.setVCPUs("32");
        instanceType.setMemory("244");
        instanceTypes.add(instanceType);
    }

    public static List<InstanceType> getInstanceTypeList() {
        return instanceTypes;
    }

    public static void main(String[] args) {
        for (InstanceType type : instanceTypes) {
            System.out.println(type);
        }
    }
}
//end of InstanceTypes.java