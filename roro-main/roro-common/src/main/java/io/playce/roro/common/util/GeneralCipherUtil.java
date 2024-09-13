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

import java.io.DataInputStream;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 1.0
 */
public class GeneralCipherUtil {

    /**
     * The constant logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(GeneralCipherUtil.class);

    /**
     * The constant privateKey.
     */
    private static Key privateKey;

    /**
     * The constant privateKey.
     */
    private static Key publicKey;

    /**
     * Encrypt string.
     *
     * @param plainText the plain text
     *
     * @return the string
     *
     * @throws RoRoException the roro exception
     */
    public static String encrypt(String plainText) throws RoRoException {
        if (StringUtils.isEmpty(plainText)) {
            return StringUtils.EMPTY;
        } else {
            getPublicKey();
            return RSAUtil.encrypt(plainText, publicKey);
        }
    }

    /**
     * Decrypt string.
     *
     * @param cipherText the cipher text
     *
     * @return the string
     *
     * @throws RoRoException the roro exception
     */
    public static String decrypt(String cipherText) throws RoRoException {
        if (StringUtils.isEmpty(cipherText)) {
            return StringUtils.EMPTY;
        } else {
            getPrivateKey();
            return RSAUtil.decrypt(cipherText, privateKey);
        }
    }

    /**
     * Encrypt string.
     *
     * @param plainText the plain text
     *
     * @return the string
     *
     * @throws RoRoException the roro exception
     */
    public static String encryptWithPriv(String plainText) throws RoRoException {
        if (StringUtils.isEmpty(plainText)) {
            return StringUtils.EMPTY;
        } else {
            getPrivateKey();
            return RSAUtil.encrypt(plainText, privateKey);
        }
    }

    /**
     * Decrypt string.
     *
     * @param cipherText the cipher text
     *
     * @return the string
     *
     * @throws RoRoException the roro exception
     */
    public static String decryptWithPub(String cipherText) throws RoRoException {
        if (StringUtils.isEmpty(cipherText)) {
            return StringUtils.EMPTY;
        } else {
            getPublicKey();
            return RSAUtil.decrypt(cipherText, publicKey);
        }
    }

    private static void getPublicKey() {
        if (publicKey == null) {
            try {
                InputStream is = GeneralCipherUtil.class.getClassLoader().getResourceAsStream("roro_general.pub");

                if (is == null) {
                    throw new RuntimeException("Public key file does not exist.");
                }

                DataInputStream dis = new DataInputStream(is);
                byte[] keyBytes = new byte[dis.available()];
                dis.readFully(keyBytes);

                KeyFactory factory = KeyFactory.getInstance("RSA");

                publicKey = factory.generatePublic(new X509EncodedKeySpec(Base64.decodeBase64(keyBytes)));
            } catch (Exception e) {
                logger.error("Unhandled exception occurred while load public key(roro_general.pub).", e);
                throw new RoRoException(e);
            }
        }
    }

    private static void getPrivateKey() {
        if (privateKey == null) {
            try {
                InputStream is = GeneralCipherUtil.class.getClassLoader().getResourceAsStream("roro_general.priv");

                if (is == null) {
                    throw new RuntimeException("Private key file does not exist.");
                }

                DataInputStream dis = new DataInputStream(is);
                byte[] keyBytes = new byte[dis.available()];
                dis.readFully(keyBytes);

                KeyFactory factory = KeyFactory.getInstance("RSA");

                privateKey = factory.generatePrivate(new PKCS8EncodedKeySpec(Base64.decodeBase64(keyBytes)));
            } catch (Exception e) {
                logger.error("Unhandled exception occurred while load private key(roro_general.priv).", e);
                throw new RoRoException(e);
            }
        }
    }

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     *
     * @throws Exception the exception
     */
    public static void main(String[] args) throws Exception {
        String password = "1234qwer";

        String encryptedPassword = encrypt(password);
        System.err.println("Encrypted Password : " + encryptedPassword);

        password = decrypt(encryptedPassword);
        System.err.println("Decrypted Password : " + password);

        encryptedPassword = encryptWithPriv(password);
        System.err.println("Encrypted with private key : " + encryptedPassword);

        password = decryptWithPub(encryptedPassword);
        System.err.println("Decrypted with public key : " + password);
    }
}
//end of GeneralCipherUtil.java