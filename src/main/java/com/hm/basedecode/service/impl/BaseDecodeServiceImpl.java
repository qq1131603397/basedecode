package com.hm.basedecode.service.impl;

import com.hm.basedecode.service.BaseDecodeService;
import com.hm.basedecode.utils.BaseUtils;
import com.hm.basedecode.utils.SFTPUtils;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Vector;

/**
 * @author： pt
 * @date： 2021/3/23 11:20
 * @discription
 */
@Service
public class BaseDecodeServiceImpl implements BaseDecodeService {

    private static final Logger log = LoggerFactory.getLogger(BaseDecodeServiceImpl.class);

    @Value("${sftp.srcHost}")
    private String srcHost;

    @Value("${sftp.srcPort}")
    private int srcPort;

    @Value("${sftp.srcUsername}")
    private String srcUsername;

    @Value("${sftp.srcPassword}")
    private String srcPassword;

    @Value("${sftp.destHost}")
    private String destHost;

    @Value("${sftp.destPort}")
    private int destPort;

    @Value("${sftp.destUsername}")
    private String destUsername;

    @Value("${sftp.destPassword}")
    private String destPassword;

    @Value("${sftp.srcDataPath}")
    private String srcDataPath;

    @Value("${sftp.targetDataPath}")
    private String targetDataPath;

    @Override
    public void base64ZipToFile() throws NoSuchFieldException, SftpException, IllegalAccessException {
        SFTPUtils srcSftp = new SFTPUtils(srcUsername, srcPassword, srcHost, srcPort);
        srcSftp.login();//sftp登录

        SFTPUtils destSftp = new SFTPUtils(destUsername, destPassword, destHost, destPort);
        destSftp.login();//sftp登录

        Session session = destSftp.getSession(destHost, destPort, destUsername, destPassword);
        Channel channel = destSftp.getChannel(session);
        ChannelSftp sftp = (ChannelSftp) channel;
        //反射获取对象
        Class cl = ChannelSftp.class;
        Field f1 = cl.getDeclaredField("server_version");
        f1.setAccessible(true);
        f1.set(sftp, 2); //很重要,不能省略
        //修改默认编码UTF-8为GBK
        sftp.setFilenameEncoding("GB2312");

        try {
            log.info("源文件路径" + srcDataPath);
            Vector<ChannelSftp.LsEntry> files = srcSftp.getFileList(srcDataPath);
            if (files.size() != 0) {
                for (ChannelSftp.LsEntry file : files) {
                    if (!file.getFilename().contains(".")) {
                        log.info("获取文件" + srcDataPath + file.getFilename());
                        Map<String, byte[]> byteMap = BaseUtils.base64ToFile(srcSftp.download(srcDataPath + file.getFilename()));
                        for (Map.Entry<String, byte[]> entry : byteMap.entrySet()) {
                            InputStream is = new ByteArrayInputStream(entry.getValue());

                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            int j;
                            while ((j = is.read()) != -1) {
                                baos.write(j);
                            }
                            String str = baos.toString();
                            byte[] bytes = str.getBytes(Charset.forName("GB2312"));//这一步就是转成gb18030格式的字节码
                            //字节码转成gb18030的字符串
                            String str4 = new String(bytes, "GB2312");
                            str4 = str4.replaceAll("encoding=\"UTF-8\"", "encoding=\"GB2312\"");
                            InputStream is3 = new ByteArrayInputStream(str4.getBytes("GB2312"));

                            destSftp.upload(sftp, targetDataPath, entry.getKey(), is3);
                            log.info("base64解密解压缩文件：" + entry.getKey());
                        }
                        srcSftp.delete(srcDataPath, file.getFilename());
                        log.info("删除文件" + file.getFilename() + "-----成功");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            srcSftp.logout();
            destSftp.logout();
        }
    }
}
