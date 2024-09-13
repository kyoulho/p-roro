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
package io.playce.roro.common.util;

import io.playce.roro.common.dto.subscription.Subscription;
import io.playce.roro.common.dto.subscription.SubscriptionType;
import io.playce.roro.common.exception.RoRoException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.util.Base64;

import java.io.DataInputStream;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 2.0.0
 */
@Slf4j
public class SubscriptionUtil {

    /**
     * The constant publicKey.
     */
    private static Key publicKey;

    /**
     * Gets subscription.
     *
     * @param subscriptionStr the subscription str
     *
     * @return the license
     *
     * @throws RoRoException the ro ro exception
     */
    public static Subscription getSubscription(String subscriptionStr) throws RoRoException, InterruptedException {
        if (publicKey == null) {
            try {
                InputStream is = SubscriptionUtil.class.getClassLoader().getResourceAsStream("roro_subscription.pub");

                if (is == null) {
                    throw new RuntimeException("Public key file does not exist.");
                }

                DataInputStream dis = new DataInputStream(is);
                byte[] keyBytes = new byte[dis.available()];
                dis.readFully(keyBytes);

                KeyFactory factory = KeyFactory.getInstance("RSA");

                publicKey = factory.generatePublic(new X509EncodedKeySpec(Base64.decodeBase64(keyBytes)));
            } catch (Exception e) {
                log.error("Unhandled exception occurred while get subscription info.", e);
                throw new RoRoException(e);
            }
        }

        return JsonUtil.jsonToObj(RSAUtil.decrypt(subscriptionStr, publicKey), Subscription.class);
    }

    /**
     * Create subscription string.
     *
     * @param subscription the subscription
     *
     * @return the string
     *
     * @throws RoRoException the ro ro exception
     */
    private static String createSubscription(Subscription subscription) throws RoRoException, InterruptedException {
        Key privateKey = null;
        String encryptedSubscription = null;

        try {
            InputStream is = SubscriptionUtil.class.getClassLoader().getResourceAsStream("roro_subscription.priv");

            if (is == null) {
                throw new RuntimeException("Private key file does not exist.");
            }

            DataInputStream dis = new DataInputStream(is);
            byte[] keyBytes = new byte[dis.available()];
            dis.readFully(keyBytes);

            KeyFactory factory = KeyFactory.getInstance("RSA");

            privateKey = factory.generatePrivate(new PKCS8EncodedKeySpec(Base64.decodeBase64(keyBytes)));

            encryptedSubscription = RSAUtil.encrypt(JsonUtil.objToJson(subscription), privateKey);
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            log.error("Unhandled exception occurred while create subscription key.", e);
            throw new RoRoException(e);
        }

        return encryptedSubscription;
    }

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     *
     * @throws Exception the exception
     */
    public static void main(String[] args) throws Exception {
        SubscriptionType subscriptionType;
        subscriptionType = SubscriptionType.TRIAL;
        // subscriptionType = SubscriptionType.INVENTORY_AND_ASSESSMENT;
        // subscriptionType = SubscriptionType.MIGRATION_AND_VERIFY;

        int count = 30;

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        // calendar.set(2021, 11, 31, 23, 59, 59);
        // calendar.add(Calendar.YEAR, 1);
        calendar.add(Calendar.DATE, 365);

        // [DEV] 002692fac2875b9785a45a7642fd11e76c5620a047ce3c27994772826c57b6ea
        // [QA] 5360dd0ac3c5f13c6a928451a3be1b32cde1d2a11eb4375b09d9f5e40758db93
        String signature = null;
        // signature = "002692fac2875b9785a45a7642fd11e76c5620a047ce3c27994772826c57b6ea";
        // signature = "5360dd0ac3c5f13c6a928451a3be1b32cde1d2a11eb4375b09d9f5e40758db93";

        Subscription subscription = new Subscription(subscriptionType, count, calendar.getTime(), signature);

        String encryptedSubscription = createSubscription(subscription);
        System.err.println("encryptedSubscription : " + encryptedSubscription);

        subscription = getSubscription(encryptedSubscription);
        System.err.println("Subscription : " + subscription);
    }
}
//end of SubscriptionUtil.java