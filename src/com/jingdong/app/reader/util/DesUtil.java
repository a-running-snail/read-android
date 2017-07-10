package com.jingdong.app.reader.util;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public class DesUtil {
    private final static String DES     = "DES";
    private final static String PADDING = "DES/ECB/PKCS5Padding";

    public static byte[] encrypt(byte[] src, byte[] key) throws Exception {

        SecureRandom sr = new SecureRandom();

        DESKeySpec dks = new DESKeySpec(key);

        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);

        SecretKey securekey = keyFactory.generateSecret(dks);

        Cipher cipher = Cipher.getInstance(PADDING);

        cipher.init(Cipher.ENCRYPT_MODE, securekey, sr);

        return cipher.doFinal(src);

    }

    public static byte[] decrypt(byte[] src, byte[] key) throws Exception {

        SecureRandom sr = new SecureRandom();

        DESKeySpec dks = new DESKeySpec(key);

        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);

        SecretKey securekey = keyFactory.generateSecret(dks);

        Cipher cipher = Cipher.getInstance(PADDING);

        cipher.init(Cipher.DECRYPT_MODE, securekey, sr);

        return cipher.doFinal(src);

    }

    public final static String decrypt(String data, String key) {

        try {

            return new String(decrypt(Base64.decode(data.getBytes()),

            key.getBytes()), "utf-8");

        } catch (Exception e) {
        }

        return null;

    }

    public final static String encrypt(String code, String key) {

        try {

            return Base64.encodeBytes(encrypt(code.getBytes("utf-8"), key.getBytes()));
        } catch (Exception e) {
        }

        return null;

    }

    public static void main(String[] args) {
        System.out.print(DesUtil.encrypt("mGsAmeU/lQA=", "1234567890abcdEfg"));
    }

}
