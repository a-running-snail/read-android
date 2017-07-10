package com.jingdong.app.reader.util;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

import com.jingdong.app.reader.config.Configuration;
import com.jingdong.app.reader.entity.BodyEncodeEntity;

import android.text.TextUtils;

/**
 * @author qt-liuguanqing Rsa加密辅助类
 */
public class RsaEncoder {

    private PublicKey               rsaPublicKey;

    private Cipher                  cipher;

    private final static int        PT_LEN     = 117; // 1024位RSA加密算法中,当加密明文长度超过117个字节后,会出现异常,所以采用分段加密
    private final static String     SPRIT_CHAR = "|"; // 分段加密/解密,段落分割符
    private static BodyEncodeEntity encodeEntity;

    // private static BodyEncodeEntity encodeEntity;

    public static BodyEncodeEntity getEncodeEntity() {
        return encodeEntity;
    }

    public static void setEncodeEntity(BodyEncodeEntity encodeEntity) {
        RsaEncoder.encodeEntity = encodeEntity;
    }

    /*
     * 检查sessionkey是否存在: true 存在 false 不存在
     */
    public static boolean checkSessionKey() {
        BodyEncodeEntity encodeEntity = RsaEncoder.getEncodeEntity();
        if (encodeEntity != null) {
            if (encodeEntity.isSuccess == false || TextUtils.isEmpty(encodeEntity.desSessionKey)) {
                return false;
            }
        } else {
            return false;
        }
        return true;
    }

    private RsaEncoder(String publicKey) throws Exception {
        cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        this.rsaPublicKey = this.generatePublic(publicKey);
    }

    private PublicKey generatePublic(String key) throws Exception {
        byte[] keyBytes;
        keyBytes = Base64.decode(key);

        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(keySpec);
        return publicKey;
    }

    /**
     * 加密输入的明文字符串 当value的字节长度大于117,将会采用分段加密,即依次对117个字节,加密,并通过"|"对段落进行分割,请解密者注意
     * 
     * @param value
     *            加密后的字符串 1024个字节长度
     * @return
     */
    public String encrypt(String value) throws IOException {
        if (TextUtils.isEmpty(value)) {
            return null;
        }
        return encryptBySeg(value.getBytes(), Base64.NO_OPTIONS);
    }

    /**
     * 分段加密
     * 
     * @param plainText
     *            ,各个段落以'|'分割
     * @param option
     * @return
     * @throws IOException
     */
    private String encryptBySeg(byte[] plainText, int option)
            throws IOException {
        // 获取加密段落个数
        int length = plainText.length;//
        int mod = length % PT_LEN;// 余数
        int ptime = length / PT_LEN;// 段数
        int ptimes = (mod == 0 ? ptime : ptime + 1);
        StringBuffer sb = new StringBuffer();
        int i = 0;
        while (i < ptimes) {
            int from = i * PT_LEN;
            int to = Math.min(length, (i + 1) * PT_LEN);
            byte[] temp = copyofRange(plainText, from, to);
            sb.append(Base64.encodeBytes(encrypt(temp), option));
            if (i != (ptimes - 1)) {
                sb.append(SPRIT_CHAR);
            }
            i++;
        }
        return sb.toString();

    }

    /*
     * test
     */
    private byte[] copyofRange(byte[] plainText, int from, int to) {
        int length = to - from;
        byte[] result = new byte[length];
        for (int i = 0; i < length; i++) {
            result[i] = plainText[from];
            from = from + 1;
        }
        return result;
    }

    /**
     * 加密
     * 
     * @param plainTextArray
     * @return
     */
    private byte[] encrypt(byte[] plainTextArray) {
        try {
            cipher.init(Cipher.ENCRYPT_MODE, rsaPublicKey);
            byte[] encryptByteArray = cipher.doFinal(plainTextArray);
            return encryptByteArray;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据公钥,获取加密工具类实例
     * 
     * @param publicKey
     * @return
     */
    public static RsaEncoder getInstance(String publicKey) throws Exception {
        if (TextUtils.isEmpty(publicKey)) {
            return null;
        }
        return new RsaEncoder(publicKey);
    }

    /**
     * 重置加密key
     * 
     * @param publicKey
     */
    public void reset(String publicKey) throws Exception {
        this.rsaPublicKey = this.generatePublic(publicKey);
    }

    /**
     * 字符错位算法
     * 
     * @param ori
     * @return
     */
    public static byte[] confuse(byte[] ori) {
        for (int i = 0, byteLength = ori.length; i < byteLength; i++) {
            ori[i] = (byte) ~ori[i];
        }
        return ori;

    }

    /*
     * 获取是否成功获取到了加密key
     */
    public static boolean ifdesKeySuccess() {
        if (encodeEntity == null) {
            return false;
        }
        return encodeEntity.isSuccess;
    }

    /*
     * 生成一个信封
     */
    public static String stringEnvelope(BodyEncodeEntity encodeEntity) {
        // BodyEncodeEntity encodeEntity =
        // (BodyEncodeEntity)DataIntent.get(KEY_BODYENCODEENTITY, true);
        if (encodeEntity == null) {
            return "";
        }
        String templeStr = System.currentTimeMillis() + CommonUtil.getDeviceId();
        
        MZLog.d("wangguodong", "templeStr###"+templeStr);
        
        String strEnvelope = MD5Calculator.calculateMD5(templeStr);
        encodeEntity.strEnvelope = strEnvelope;
        // DataIntent.put(KEY_BODYENCODEENTITY, encodeEntity);
        return stringRsaPublicKey(strEnvelope, encodeEntity.sourcePublicKey);
    }

    /*
     * 加密信封
     */
    public static String stringRsaPublicKey(String info, String cSourcePublicKey) {
        String jResult = "";
        try {
            if (TextUtils.isEmpty(cSourcePublicKey)) {
                return "";
            }
            // ="z35gz/L59tV5t3kI8v7+/vr//H5y/89+dv1+fv9ty1Bwsoodk1/EkSruncOdFCQMJYGVKR4aqFqC  IrBS3lK3uHhodLL/efU4rANQCqc7/id1UYeCrRFIi2Tw0sEKisXwrwmAnXvgWZSFfmSBP8La5zQl  J9hOFCTFBl5NHl2W9cejAlDtRVshMJukiBVcNvvxigCaKMMWt28dg8cMRP38/v/+";
            // RsaUtil rsaUtil = new RsaUtil();
            String jPublicKey = Base64.encodeBytes(confuse(Base64.decode(cSourcePublicKey)));
            
            MZLog.d("wangguodong", "jPublicKey###"+jPublicKey);
            
            
            RsaEncoder rsaEncoder = RsaEncoder.getInstance(jPublicKey);
            jResult = rsaEncoder.encrypt(info);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jResult;
    }

    /*
     * 加密body
     */
    public static String stringBodyEncoder(String info,
            BodyEncodeEntity encodeEntity) {
        String jResult = "";
        try {
            if (encodeEntity == null) {
                return "";
            }
            String desSessionKey = encodeEntity.desSessionKey;
            jResult = DesUtil.encrypt(info, desSessionKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jResult;
    }

    /*
     * rsa
     */

    public static String stringRSAEncoder(String info) {
        String jResult = "";
        try {
            // String
            // sourcePublicKey="z35gz/L59tV5t3kI8v7+/vr//H5y/89+dv1+fv9ty1Bwsoodk1/EkSruncOdFCQMJYGVKR4aqFqC  IrBS3lK3uHhodLL/efU4rANQCqc7/id1UYeCrRFIi2Tw0sEKisXwrwmAnXvgWZSFfmSBP8La5zQl  J9hOFCTFBl5NHl2W9cejAlDtRVshMJukiBVcNvvxigCaKMMWt28dg8cMRP38/v/+";
            String sourcePublicKey = null;
            sourcePublicKey = String.valueOf(Configuration.getProperty(Configuration.SOURCEPUBLICKEY, ""));
            if (TextUtils.isEmpty(sourcePublicKey)) {
                return "";
            }
            String publicKey = Base64.encodeBytes(confuse(Base64.decode(sourcePublicKey)));
            RsaEncoder rsaEncoder = RsaEncoder.getInstance(publicKey);
            jResult = rsaEncoder.encrypt(info);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jResult;
    }
}
