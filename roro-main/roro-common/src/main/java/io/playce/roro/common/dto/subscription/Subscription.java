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

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 2.0.0
 */
@Getter
@Setter
@ToString
public class Subscription {

    /**
     * The Type.
     */
    private SubscriptionType type;

    /**
     * The Count.
     */
    private Integer count = 5;

    private Integer usedCount = 0;
    /**
     * The Expire date.
     */
    private Date expireDate;

    /**
     * The Signature.
     */
    private String signature;

    /**
     * The Subscription staus type.
     */
    private SubscriptionStausType subscriptionStausType;

    /**
     * Instantiates a new Subscription.
     */
    public Subscription() {
        this(false);
    }

    /**
     * Instantiates a new Subscription.
     *
     * @param isTrial the is trial
     */
    public Subscription(boolean isTrial) {
        if (isTrial) {
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            calendar.add(Calendar.DATE, 15);

            this.type = SubscriptionType.TRIAL;
            this.expireDate = calendar.getTime();
        }
    }

    /**
     * Instantiates a new Subscription.
     *
     * @param type       the type
     * @param count      the count
     * @param expireDate the expire date
     * @param signature  the signature
     */
    public Subscription(SubscriptionType type, Integer count, Date expireDate, String signature) {
        this.type = type;
        this.count = count;
        this.expireDate = expireDate;
        this.signature = signature;
    }
}
//end of Subscription.java