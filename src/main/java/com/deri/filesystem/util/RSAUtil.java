package com.deri.filesystem.util;

//import org.apache.commons.codec.binary.Base64;

import org.apache.tomcat.util.codec.binary.Base64;

import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName: RSAUtil
 * @Description: 前后台加密算法，前台配合jsencrypt.min.js加密
 * @Author: wuzhiyong
 * @Time: 2019/9/5 10:54
 * @Version: v1.0
 **/
public class RSAUtil {
    private static final String KEY_ALGORITHM = "RSA";
    private static final String SIGNATURE_ALGORITHM = "MD5withRSA";
    private static final String PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCyVP9lbEL+693KGnwM5ph8WG0CAIVJm2LqhfELZKjbCNa1wGRR/5qNDlKx3UCZkqHhCR/c73Z57p3jXw7tu82PgvKzu2qyjwaQeYf/r9K2pbq3N86+41jKj5oi+V7rcTN6jEuv6mmWySV8WsD6JV81n/utgx8yetK9JKWNZMLsXwIDAQAB";
    private static final String PRIVATE_KEY = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBALJU/2VsQv7r3coafAzmmHxYbQIAhUmbYuqF8QtkqNsI1rXAZFH/mo0OUrHdQJmSoeEJH9zvdnnuneNfDu27zY+C8rO7arKPBpB5h/+v0ralurc3zr7jWMqPmiL5XutxM3qMS6/qaZbJJXxawPolXzWf+62DHzJ60r0kpY1kwuxfAgMBAAECgYAk4QEEB3R/ZWWzcvCudk2YgWzhmhv2UeUN3O8xTPrCeTx1z7ivYG2kuA6P69J9L968O/fT582Xf6o1KCZdMZZbu+bB1mDYHZ2KSTFq3w3dAipDY4DYstItR0rwvdhptjWNvdRxTKtCnx/VPF+1v1pN+wi/jFvHqu0HNtxk2stKaQJBAN5AyPQ6Rudif47RmEwpmIHGF67CE1F8nbQcBfaLD0ZqXzGXTt7j8FG0DCutSRgMogmo6p5u8Z7rvX67tXGnJnUCQQDNaPRe/FDcoWeVb2ZNwS5PCSnrTfvfU04Xwz7m0APcEiaa1CD0A4nmQVJV7qX7ZWcA0tEAUQ2jWuD5iqY7EnUDAkAOXKtftOBeFWxew0aPLWTwheeD4IC1FFNwjxsHiobrKvaJ0thC6QHflb3vmJwPlMlnzGWU5WBxv5QYO3MHLCD9AkBf+jBkyYSG8qvRuTGWFzOqHmAF4HRVzzjoBYS4mG2VQK3lHkUffx/KBK+SE2Ze5uyQB/E8MqMqtuXz84LkiyU9AkEAtpeRxoWWcjMMQKKnChzxgtmm13rryHVuvv2/hYyyiSbUX0vMxY73VOwPIwsdYpIJ46HDSmtZX6KSRzR6TCUqkQ==";

    public static void main(String[] args) throws Exception {
//        私钥加密   公钥解密
        String inputStr = "abc";
        String encodedData = RSAUtil.encryptByPrivateKey(inputStr);
        System.out.println(encodedData);
        String decodedData = RSAUtil.decryptByPublicKey(encodedData);
        System.err.println("加密前: " + inputStr + "\n\r" + "解密后: " + decodedData);

//        公钥加密   私钥解密
        String inputStr1 = "abc";
        String encodedData1 = RSAUtil.encryptByPublicKey(inputStr1);
        String decodedData1 = RSAUtil.decryptByPrivateKey(encodedData1);
        System.err.println("加密前: " + inputStr1 + "\n\r" + "解密后: " + decodedData1);
//          签名
        String ttss = "ssdada";
//        获取加密的内容
        String encond = RSAUtil.encryptByPublicKey(ttss);
//        私钥签名
        String si = sign(encond);
        System.out.println(si);
//        私钥验证
        System.out.println(verify(encond, si));
        // 生成新的公钥和私钥
//        Map<String, Key> keyMap = initKey(1024);
//        System.out.println(getPrivateKey(keyMap));
//        System.out.println("----");
//        System.out.println(getPublicKey(keyMap));
    }

    /**
     * 用私钥对信息生成数字签名
     *
     * @param data 加密数据-经过BASE64转码的字符串
     */
    public static String sign(String data) throws Exception {
        byte[] keyBytes = decryptBASE64(PRIVATE_KEY);
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        PrivateKey priKey = keyFactory.generatePrivate(pkcs8KeySpec);
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initSign(priKey);
        signature.update(decryptBASE64(data));
        return encryptBASE64(signature.sign());
    }

    /**
     * 用公钥校验数字签名，公钥大家都有，用来签名没意义，所以只有用私钥签名，公钥验证
     *
     * @param data 加密数据-经过BASE64转码的字符串
     * @param sign 数字签名
     * @return 校验成功返回true 失败返回false
     */
    public static boolean verify(String data, String sign) throws Exception {
        byte[] keyBytes = decryptBASE64(PUBLIC_KEY);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        PublicKey pubKey = keyFactory.generatePublic(keySpec);
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initVerify(pubKey);
        signature.update(decryptBASE64(data));
        return signature.verify(decryptBASE64(sign));
    }

    /**
     * 使用公钥加密，返回BASE64编码的加密内容
     *
     * @param data
     * @return
     * @throws Exception
     */
    public static String encryptByPublicKey(String data) throws Exception {
        return encryptBASE64(encryptByPublicKey(data, PUBLIC_KEY));
    }

    /**
     * 使用私钥加密，返回BASE64编码的加密内容
     *
     * @param data
     * @return
     * @throws Exception
     */
    public static String encryptByPrivateKey(String data) throws Exception {
        return encryptBASE64(encryptByPrivateKey(data, PRIVATE_KEY));
    }

    /**
     * 使用私钥解密，返回明文
     *
     * @param data
     * @return
     * @throws Exception
     */
    public static String decryptByPrivateKey(String data) throws Exception {
        return new String(decryptByPrivateKey(data, PRIVATE_KEY));
    }

    /**
     * 使用公钥解密，返回明文
     *
     * @param data
     * @return
     * @throws Exception
     */
    public static String decryptByPublicKey(String data) throws Exception {
        return new String(decryptByPublicKey(data, PUBLIC_KEY));
    }


    private static byte[] encryptByPublicKey(String data, String key) throws Exception {
        return initCipher(key, true, true).doFinal(data.getBytes());
    }

    private static byte[] encryptByPrivateKey(String data, String key) throws Exception {
        return initCipher(key, false, true).doFinal(data.getBytes());
    }

    private static byte[] encryptByPrivateKey(byte[] data, String key) throws Exception {
        return initCipher(key, false, true).doFinal(data);
    }

    private static byte[] decryptByPrivateKey(String data, String key) throws Exception {
        return decryptByPrivateKey(decryptBASE64(data), key);
    }

    private static byte[] decryptByPublicKey(String data, String key) throws Exception {
        return decryptByPublicKey(decryptBASE64(data), key);
    }

    private static byte[] decryptByPublicKey(byte[] data, String key) throws Exception {
        return initCipher(key, true, false).doFinal(data);
    }

    private static byte[] decryptByPrivateKey(byte[] data, String key) throws Exception {
        return initCipher(key, false, false).doFinal(data);
    }

    private static Cipher initCipher(String key, boolean pub, boolean encrypt) throws Exception {
        byte[] keyBytes = decryptBASE64(key);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        Key tmp;
        if (pub) {
            X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
            tmp = keyFactory.generatePublic(x509KeySpec);
        } else {
            PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
            tmp = keyFactory.generatePrivate(pkcs8KeySpec);
        }
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        if (encrypt) {
            cipher.init(Cipher.ENCRYPT_MODE, tmp);
        } else {
            cipher.init(Cipher.DECRYPT_MODE, tmp);
        }
        return cipher;
    }

    public static byte[] decryptBASE64(String key) {
        return Base64.decodeBase64(key);
    }

    public static String encryptBASE64(byte[] bytes) {
        return Base64.encodeBase64String(bytes);
    }

    private static String getPrivateKey(Map<String, Key> keyMap) throws Exception {
        Key key = (Key) keyMap.get("RSAPrivateKey");
        return encryptBASE64(key.getEncoded());
    }

    private static String getPublicKey(Map<String, Key> keyMap) throws Exception {
        Key key = keyMap.get("RSAPublicKey");
        return encryptBASE64(key.getEncoded());
    }

    private static Map<String, Key> initKey(int keySize) throws Exception {
        KeyPairGenerator keyPairGen = KeyPairGenerator
                .getInstance(KEY_ALGORITHM);
        keyPairGen.initialize(keySize);
        KeyPair keyPair = keyPairGen.generateKeyPair();
        Map<String, Key> keyMap = new HashMap(2);
        keyMap.put("RSAPublicKey", keyPair.getPublic());
        keyMap.put("RSAPrivateKey", keyPair.getPrivate());
        return keyMap;
    }
}
