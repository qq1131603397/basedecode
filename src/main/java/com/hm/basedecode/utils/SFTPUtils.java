package com.hm.basedecode.utils;

import com.jcraft.jsch.*;
import org.apache.poi.util.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Vector;

/**
 * @author： pt
 * @date： 2021/3/23 9:46
 * @discription
 */
public class SFTPUtils {

    private ChannelSftp sftp;

    private Session session;
    /**
     * SFTP 登录用户名
     */
    private String username;
    /**
     * SFTP 登录密码
     */
    private String password;
    /**
     * SFTP 服务器地址IP地址
     */
    private String host;
    /**
     * SFTP 端口
     */
    private int port;

    /**
     * 构造基于密码认证的sftp对象
     */
    public SFTPUtils(String username, String password, String host, int port) {
        this.username = username;
        this.password = password;
        this.host = host;
        this.port = port;
    }

    /**
     * 连接sftp服务器
     */
    public void login() {
        try {
            JSch jsch = new JSch();

            session = jsch.getSession(username, host, port);

            if (password != null) {
                session.setPassword(password);
            }
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");

            session.setConfig(config);
            session.connect();

            Channel channel = session.openChannel("sftp");
            channel.connect();

            sftp = (ChannelSftp) channel;
        } catch (JSchException e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭连接 server
     */
    public void logout() {
        if (sftp != null) {
            if (sftp.isConnected()) {
                sftp.disconnect();
            }
        }
        if (session != null) {
            if (session.isConnected()) {
                session.disconnect();
            }
        }
    }

    /**
     * 将输入流的数据上传到sftp作为文件。文件完整路径=basePath+directory
     *
     * @param channelSftp
     * @param directory    上传到该目录
     * @param sftpFileName sftp端文件名
     */
    public boolean upload(ChannelSftp channelSftp, String directory, String sftpFileName, InputStream input) throws SftpException {
        try {
            if (directory != null && !"".equals(directory)) {
                channelSftp.cd(directory);
            }
            channelSftp.put(input, sftpFileName);  //上传文件
            return true;
        } catch (SftpException e) {
            return false;
        }
    }

    /**
     * 获取文件byte数组
     *
     * @param downloadFile 文件路径
     * @return
     * @throws SftpException
     * @throws IOException
     */
    public byte[] download(String downloadFile) throws SftpException, IOException {

        InputStream is = sftp.get(downloadFile);

        byte[] fileData = IOUtils.toByteArray(is);

        return fileData;
    }

    /**
     * 删除文件
     *
     * @param directory  要删除文件所在目录
     * @param deleteFile 要删除的文件
     */
    public void delete(String directory, String deleteFile) throws SftpException {
        if (directory != null && !"".equals(directory)) {
            sftp.cd(directory);
        }
        sftp.rm(deleteFile);
    }

    /**
     * 获取文件夹下所有文件信息
     * @param path
     * @return
     */
    public Vector<ChannelSftp.LsEntry> getFileList(String path) {
        try {
            sftp.cd(path);
            Vector<ChannelSftp.LsEntry> vector = sftp.ls("*");
            return vector;
        } catch (SftpException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Session getSession(String host, int port, String username,
                              final String password) {
        Session session = null;
        try {
            JSch jsch = new JSch();
            jsch.getSession(username, host, port);
            session = jsch.getSession(username, host, port);
            session.setPassword(password);
            Properties sshConfig = new Properties();
            sshConfig.put("StrictHostKeyChecking", "no");
            session.setConfig(sshConfig);
            session.connect();
        } catch (JSchException e) {
            e.printStackTrace();
        }
        return session;
    }

    public Channel getChannel(Session session) {
        Channel channel = null;
        try {
            channel = session.openChannel("sftp");
            channel.connect();
        } catch (JSchException e) {
            e.printStackTrace();
        }
        return channel;
    }

}