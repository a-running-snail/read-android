package com.jingdong.app.reader.util;

public class NativeWrapper {

    /****
     * 在调用FunA B函数的时候确保 until.a 中的Activity a不为null，如果so没加载的话
     * 
     * <br>
     * 加密函数 aes加密
     * 
     * @param data
     *            加密后的uuid 普通字符，不可以是中文
     * @param bkId
     *            bookid 或者其余，不可以使用中文，不可以为null
     * @return String 返回二16位格式化的字符串，3889814EA38EE39616A40D504EDDB6C8149AFBB07A
     * 
     *         加密长度限制在1k以内，不支持超过1k的串加密,建议在512字符内
     *         0061896CA60F1A6FB0A59353BB6A5269F1E1B0746A5080EF2 前四位是加密后的数据长度
     * 
     * ******************/

    public synchronized static String aseEncrypt(String data, String bkId)
    {
        return "";
    }

    /****
     * 加密函数 aes解密
     * 
     * @param data
     *            二次加密后的uuid
     * @param bkId
     *            bookid 如果bkId与加密的不样，程序可能会崩溃
     * @return String 返回解密后的加密的字符串
     * 
     *         加密长度限制在1k以内，不支持超过1k的串加
     * */

    public synchronized static String aseDencrypt(String data, String bkId)
    {
        return "";
    }

}
