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
 * Sang-cheon Park	2020. 7. 28.		First Draft.
 */
package io.playce.roro.common.util;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Sang-cheon Park
 * @version 1.0
 */
public class AES256Util {

    private static final Logger logger = LoggerFactory.getLogger(AES256Util.class);

    private static final String KEY = "Xq4fPdZQ4tHWrRgu";
    private static final String iv;
    private static final Key keySpec;

    static {
        iv = KEY.substring(0, 16);

        byte[] keyBytes = new byte[16];

        byte[] b = KEY.getBytes(StandardCharsets.UTF_8);
        int len = b.length;
        if (len > keyBytes.length) {
            len = keyBytes.length;
        }
        System.arraycopy(b, 0, keyBytes, 0, len);

        keySpec = new SecretKeySpec(keyBytes, "AES");
    }

    /**
     * <pre>
     * encryption
     * </pre>
     *
     * @param str
     *
     * @return
     */
    public static String encrypt(String str) {
        if (StringUtils.isEmpty(str)) {
            return "";
        }

        String cipherText = null;

        try {
            Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
            c.init(Cipher.ENCRYPT_MODE, keySpec, new IvParameterSpec(iv.getBytes()));

            byte[] encrypted = c.doFinal(str.getBytes(StandardCharsets.UTF_8));
            cipherText = new String(Base64.encodeBase64(encrypted));
        } catch (Exception e) {
            logger.warn("Unhandled exception occurred while encrypt. [Reason] : {}", e.getMessage());
        }

        return cipherText;
    }

    /**
     * <pre>
     * decryption
     * </pre>
     *
     * @param str
     *
     * @return
     *
     * @throws Exception
     */
    public static String decrypt(String str) {

        if (StringUtils.isEmpty(str)) {
            return "";
        }

        String plainText = null;

        try {
            Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
            c.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(iv.getBytes(StandardCharsets.UTF_8)));

            byte[] byteStr = Base64.decodeBase64(str.getBytes());
            plainText = new String(c.doFinal(byteStr), StandardCharsets.UTF_8);
        } catch (Exception e) {
            if (e instanceof javax.crypto.IllegalBlockSizeException) {
                // ignore
            } else {
                logger.warn("Unhandled exception occurred while decrypt. [Reason] : {}", e.getMessage());
            }
        }

        return plainText;
    }

    public static void main(String[] args) {
        String plainText = "This is test string";
        String cipherText = "";

        cipherText = encrypt(plainText);

        System.out.println(plainText + " => " + cipherText);

        plainText = decrypt(cipherText);

        System.out.println(cipherText + " => " + plainText);
    }

}
//end of AES256Util.java