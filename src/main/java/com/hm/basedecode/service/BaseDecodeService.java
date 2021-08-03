package com.hm.basedecode.service;

import com.jcraft.jsch.SftpException;

/**
 * @author： pt
 * @date： 2021/3/23 11:20
 * @discription
 */
public interface BaseDecodeService {

    /**
     * base64解密zip解压缩成文件
     */
    void base64ZipToFile() throws NoSuchFieldException, SftpException, IllegalAccessException;

}
