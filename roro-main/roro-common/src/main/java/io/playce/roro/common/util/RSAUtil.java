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
 * SangCheon Park   Feb 08, 2021		First Draft.
 */
package io.playce.roro.common.util;

import io.playce.roro.common.exception.RoRoException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import java.security.Key;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 1.0
 */
public class RSAUtil {

    /**
     * The constant logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(RSAUtil.class);

    /**
     * Encrypt string.
     *
     * @param plainText the plain text
     * @param key       the key
     *
     * @return the string
     *
     * @throws Exception the exception
     */
    public static String encrypt(String plainText, Key key) throws RoRoException {
        String cipherText = null;

        try {
            // SSHUtil에서 BouncyCastleProvider가 상위에 포함될 수 있기 때문에 명시적으로 com.sun.crypto.provider.SunJCE Provider를 지정한다.
            Cipher cipher = Cipher.getInstance("RSA", "SunJCE");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            cipherText = Base64.encodeBase64String(cipher.doFinal(plainText.getBytes()));
        } catch (Exception e) {
            logger.error("Unhandled exception while encrypt plain text.", e);
            throw new RoRoException(e);
        }

        return cipherText.replaceAll("\\r|\\n", "");
    }

    /**
     * Decrypt string.
     *
     * @param cipherText the cipher text
     * @param key        the key
     *
     * @return the string
     *
     * @throws Exception the exception
     */
    public static String decrypt(String cipherText, Key key) throws RoRoException {
        String plainText = null;

        try {
            if (!StringUtils.isEmpty(cipherText)) {
                // SSHUtil에서 BouncyCastleProvider가 상위에 포함될 수 있기 때문에 명시적으로 SunJCE Provider를 지정한다.
                Cipher cipher = Cipher.getInstance("RSA", "SunJCE");
                cipher.init(Cipher.DECRYPT_MODE, key);
                plainText = new String(cipher.doFinal(Base64.decodeBase64(cipherText)));
            }
        } catch (Exception e) {
            logger.error("Unhandled exception while decrypt cipher text.", e);
            throw new RoRoException(e);
        }

        return plainText;
    }
}
//end of RSAUtil.java