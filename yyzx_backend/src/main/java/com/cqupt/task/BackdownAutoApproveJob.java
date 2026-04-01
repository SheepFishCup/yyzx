package com.cqupt.task;
/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2026/03/21 22:55
 * @description
 */

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cqupt.pojo.Backdown;
import com.cqupt.pojo.Bed;
import com.cqupt.pojo.Customer;
import com.cqupt.service.BackdownService;
import com.cqupt.service.BedService;
import com.cqupt.service.CustomerService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

// ... existing code ...

@Slf4j
@Component
public class BackdownAutoApproveJob extends QuartzJobBean {

    private static BackdownService backdownService;
    private static CustomerService customerService;
    private static BedService bedService;

    @Autowired
    public void setBackdownService(BackdownService backdownService) {
        BackdownAutoApproveJob.backdownService = backdownService;
    }

    @Autowired
    public void setCustomerService(CustomerService customerService) {
        BackdownAutoApproveJob.customerService = customerService;
    }

    @Autowired
    public void setBedService(BedService bedService) {
        BackdownAutoApproveJob.bedService = bedService;
    }

    @Override
    protected void executeInternal(JobExecutionContext context) {
        log.info("开始处理待审批的退住申请...");
        try {
            LambdaQueryWrapper<Backdown> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Backdown::getAuditStatus, 0)
                    .eq(Backdown::getIsDeleted, 0);

            List<Backdown> pendingList = backdownService.list(queryWrapper);

            if (pendingList == null || pendingList.isEmpty()) {
                log.info("没有待处理的退住申请");
                return;
            }

            int successCount = 0;
            int failCount = 0;

            for (Backdown backdown : pendingList) {
                try {
                    Date createTime = backdown.getAuditTime();
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

                    backdown.setAuditStatus(1);
                    backdown.setAuditPerson("0");
                    backdown.setAuditTime(new Date());
                    backdown.setRemarks("系统自动审批：超过 24 小时未人工处理");
                    boolean updated = backdownService.updateById(backdown);

                    if (updated) {
                        successCount++;
                        log.info("退住申请 ID={} 自动审批通过", backdown.getId());

                        Customer customer = customerService.getById(backdown.getCustomerId());
                        if (customer != null) {
                            Bed bed = new Bed();
                            bed.setId(customer.getBedId());
                            bed.setBedStatus(1);
                            bedService.updateById(bed);
                        }
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
}

