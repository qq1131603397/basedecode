package com.hm.basedecode.controller;

import com.hm.basedecode.service.BaseDecodeService;
import com.jcraft.jsch.SftpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.concurrent.ScheduledFuture;

/**
 * @author： pt
 * @date： 2021/3/23 11:16
 * @discription
 */
@RestController
@EnableScheduling
public class DynamicController {

    private static final Logger log = LoggerFactory.getLogger(DynamicController.class);

    private String dynamicCron = "0/30 * * * * ?";

    @Resource
    private ThreadPoolTaskScheduler threadPoolTaskScheduler;

    private ScheduledFuture sftpTask;

    @Autowired
    private BaseDecodeService baseDecodeService;

    @Bean
    public ThreadPoolTaskScheduler trPoolTaskScheduler() {
        return new ThreadPoolTaskScheduler();
    }

    /**
     * 初始化方法，开启定时任务
     */
    @PostConstruct
    private void initialize() {
        this.startTask("sftpTask");
        log.info("sftpTask定时任务----初始化" + this.dynamicCron);
    }

    /**
     * 启动定时任务
     *
     * @return
     */
    public void startTask(String taskName) {
        /**
         * task:定时任务要执行的方法
         * trigger:定时任务执行的时间
         */
        if ("sftpTask".equals(taskName)) {
            sftpTask = threadPoolTaskScheduler.schedule(new MyRunable(), new CronTrigger(dynamicCron));
        }
    }

    /**
     * 停止定时任务
     *
     * @return
     */
    public void endTask(String taskName) {
        if ("sftpTask".equals(taskName)) {
            sftpTask.cancel(true);
        }
    }

    /**
     * 改变调度的时间
     * 步骤：
     * 1,先停止定时器
     * 2,在启动定时器
     */
    @RequestMapping("setDynamicCron")
    public void changeTask(String newCron) {
        this.dynamicCron = newCron;
        //停止定时器
        endTask("sftpTask");
        //启动定时器
        startTask("sftpTask");
        log.info("sftpTask定时任务----变更调度时间：" + newCron);
    }

    /**
     * 定义定时任务执行的方法
     *
     * @author Admin
     */
    public class MyRunable implements Runnable {
        @Override
        public void run() {
            try {
                baseDecodeService.base64ZipToFile();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (SftpException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}
