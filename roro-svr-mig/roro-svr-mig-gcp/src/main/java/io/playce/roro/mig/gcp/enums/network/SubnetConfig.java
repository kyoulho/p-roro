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
 * SangCheon Park   Feb 14, 2022		    First Draft.
 */
package io.playce.roro.mig.gcp.enums.network;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
public class SubnetConfig {

    /**
     * The enum Purpose.
     */
    public enum Purpose {
        /**
         * Internal https load balancer subnet purpose.
         */
        INTERNAL_HTTPS_LOAD_BALANCER("Load balancer"),
        /**
         * Private rfc 1918 subnet purpose.
         */
        PRIVATE("Private");

        /**
         * The Description.
         */
        private String description;

        /**
         * @param description the description
         */
        private Purpose(String description) {
            this.description = description;
        }

        /**
         * Gets description.
         *
         * @return the description
         */
        public String getDescription() {
            return description;
        }

        /**
         * To string string.
         *
         * @return the string
         */
        @Override
        public String toString() {
            return this.getDescription();
        }

    }

    /**
     * The enum Role.
     */
    public enum Role {
        /**
         * Internal https load balancer subnet purpose.
         */
        ACTIVE("Active"),
        /**
         * Private rfc 1918 subnet purpose.
         */
        BACKUP("Backup");

        /**
         * The Description.
         */
        private String description;

        /**
         * @param description the description
         */
        private Role(String description) {
            this.description = description;
        }

        /**
         * Gets description.
         *
         * @return the description
         */
        public String getDescription() {
            return description;
        }

        /**
         * To string string.
         *
         * @return the string
         */
        @Override
        public String toString() {
            return this.getDescription();
        }
    }
}
//end of SubnetConfig.java