package com.hm.basedecode.utils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author： pt
 * @date： 2021/3/23 13:28
 * @discription
 */
public class BaseUtils {

    /**
     * Base64 解密 zip 解压缩
     *
     * @param base64
     * @throws RuntimeException
     */
    public static Map<String, byte[]> base64ToFile(byte[] base64) throws RuntimeException {

        String str = new String(base64);
        str = str.replaceAll("<file>\n","").replaceAll("\n</file>","");
        base64 = str.getBytes();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Map<String, byte[]> byteMap = new HashMap<>();
        try {
            byte[] byteBase64 = Base64.getDecoder().decode(base64);
            ByteArrayInputStream byteArray = new ByteArrayInputStream(byteBase64);
            ZipInputStream zipInput = new ZipInputStream(byteArray);
            ZipEntry entry = zipInput.getNextEntry();
            while (entry != null && !entry.isDirectory()) {
                BufferedOutputStream bos = new BufferedOutputStream(baos);
                int offo = -1;
                byte[] buffer = new byte[2 * 1024];
                while ((offo = zipInput.read(buffer)) != -1) {
                    bos.write(buffer, 0, offo);
                }
                bos.close();
                // 获取 下一个文件
                byteMap.put(entry.getName(), baos.toByteArray());
                entry = zipInput.getNextEntry();
            }
            zipInput.close();
            byteArray.close();
        } catch (Exception e) {
            throw new RuntimeException("解压流出现异常", e);
        }
        return byteMap;
    }

}
