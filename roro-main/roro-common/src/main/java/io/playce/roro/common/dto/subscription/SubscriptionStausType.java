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
public enum SubscriptionStausType {

    /**
     * Subscription not found subscription staus type.
     */
    SUBSCRIPTION_NOT_FOUND("SUBSCRIPTION_NOT_FOUND"),
    /**
     * Subscription invalid subscription staus type.
     */
    SUBSCRIPTION_INVALID("SUBSCRIPTION_INVALID"),
    /**
     * Signature not match subscription staus type.
     */
    SIGNATURE_NOT_MATCH("SIGNATURE_NOT_MATCH"),
    /**
     * Subscription expired subscription staus type.
     */
    SUBSCRIPTION_EXPIRED("SUBSCRIPTION_EXPIRED"),
    /**
     * Subscription valid subscription staus type.
     */
    SUBSCRIPTION_VALID("SUBSCRIPTION_VALID");
    /**
     * The Description.
     */
    private final String description;

    /**
     * Instantiates a new Subscription status type.
     *
     * @param description the description
     */
    SubscriptionStausType(String description) {
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
//end of SubscriptionStausType.java