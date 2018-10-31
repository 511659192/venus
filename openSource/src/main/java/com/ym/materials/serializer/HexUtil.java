package com.ym.materials.serializer;

/**
 * Created by ym on 2018/10/31.
 */
public class HexUtil {

    /**
     * 字节数组转16进制
     * @param bytes 需要转换的byte数组
     * @return  转换后的Hex字符串
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(bytes[i] & 0xFF);
            if(hex.length() < 2){
                sb.append(0);
            }
            sb.append(hex);
            if (i % 10 == 0 && i > 0) {
                sb.append("\r\n");
            } else {
                sb.append(" ");
            }
        }
        return sb.toString();
    }
}
