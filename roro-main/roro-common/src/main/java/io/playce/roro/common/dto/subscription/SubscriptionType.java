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
 * SangCheon Park   Feb 19, 2021		First Draft.
 */
package io.playce.roro.common.dto.subscription;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 2.0.0
 */
public enum SubscriptionType {

    /**
     * Trial subscription type.
     */
    TRIAL("TRIAL"),
    /**
     * Inventory and assessment subscription type.
     */
    INVENTORY_AND_ASSESSMENT("INVENTORY_AND_ASSESSMENT"),
    /**
     * Migration and verify subscription type.
     */
    MIGRATION_AND_VERIFY("MIGRATION_AND_VERIFY");

    /**
     * The Description.
     */
    private final String description;

    /**
     * Instantiates a new Subscription type.
     *
     * @param description the description
     */
    SubscriptionType(String description) {
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
//end of SubscriptionType.java