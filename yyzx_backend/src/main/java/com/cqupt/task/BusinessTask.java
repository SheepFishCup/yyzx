package com.cqupt.task;
/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2026/03/08 20:01
 * @description
 */

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cqupt.pojo.Backdown;
import com.cqupt.pojo.Bed;
import com.cqupt.pojo.Customer;
import com.cqupt.service.BackdownService;
import com.cqupt.service.BedService;
import com.cqupt.service.CustomerService;
import com.cqupt.service.NurseRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class BusinessTask {
    @Autowired
    private NurseRecordService nurseRecordService;
    @Autowired
    private BackdownService backdownService;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private BedService bedService;

    /**
     * 每天凌晨 2 点执行
     * 清理过期的护理记录（假设只保留最近 1 年的数据）
     */
//    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanExpiredNurseRecords() {
        log.info("开始清理过期护理记录...");
        try {
            // TODO: 实现具体的清理逻辑
            // nurseRecordService.deleteExpiredRecords(LocalDateTime.now().minusYears(1));
            log.info("清理过期护理记录完成");
        } catch (Exception e) {
            log.error("清理过期护理记录失败", e);
        }
    }

    /**
     * 每小时执行一次
     * 检查并处理待审批的退住申请
     */
//    @Scheduled(cron = "0 0 * * * ?")
    public void processPendingBackdown() {
        log.info("开始处理待审批的退住申请...");
        try {
            // TODO: 实现具体的处理逻辑
            // 1. 查询所有待审批的退住申请（audit_status=0）
            LambdaQueryWrapper<Backdown> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Backdown::getAuditStatus, 0)  // 待审批
                    .eq(Backdown::getIsDeleted, 0);        // 未删除

            List<Backdown> pendingList = backdownService.list(queryWrapper);

            if (pendingList == null || pendingList.isEmpty()) {
                log.info("没有待处理的退住申请");
                return;
            }
            // 2. 遍历处理
            int successCount = 0;
            int failCount = 0;

            for (Backdown backdown : pendingList) {
                try {
                    // 3. 检查申请时间是否超过 24 小时
                    Date createTime = backdown.getAuditTime(); // 如果没有，可以用创建时间或退住时间
                    if (createTime == null) {
                        createTime = backdown.getRetreatTime();
                    }
                    if (createTime != null) {
                        long hoursDiff = (System.currentTimeMillis() - createTime.getTime()) / (1000 * 60 * 60);
                        if (hoursDiff < 24) {
                            log.debug("退住申请 ID={} 提交不到 24 小时，暂不处理", backdown.getId());
                            continue;
                        }
                    }
                    // 4. 自动审批通过
                    backdown.setAuditStatus(1);  // 1-同意
                    backdown.setAuditPerson("0");// 系统自动
                    backdown.setAuditTime(new Date());// 当前时间
                    backdown.setRemarks("系统自动审批：超过 24 小时未人工处理");
                    boolean updated = backdownService.updateById(backdown);
                    if (updated) {
                        successCount++;
                        log.info("退住申请 ID={} 自动审批通过", backdown.getId());
                        Customer customer=customerService.getById(backdown.getCustomerId());
                        Bed bed=new Bed();
                        bed.setId(customer.getBedId());
                        bed.setBedStatus(1);
                        bedService.updateById(bed);
                    } else {
                        failCount++;
                        log.warn("退住申请 ID={} 更新失败", backdown.getId());
                    }
                } catch (Exception e) {
                    failCount++;
                    log.error("处理退住申请 ID={} 时发生异常", backdown.getId(), e);
                }
            }
            log.info("========== 处理完成：成功{}条，失败{}条 ==========", successCount, failCount);
        } catch (Exception e) {
            log.error("处理待审批退住申请失败", e);
        }
    }

    /**
     * 每周一早上 8 点执行
     * 生成周报统计
     */
//    @Scheduled(cron = "0 0 8 ? * MON")
    public void generateWeeklyReport() {
        log.info("开始生成周报统计...");
        try {
            log.info("周报统计生成完成");
        } catch (Exception e) {
            log.error("生成周报统计失败", e);
        }
    }
}
