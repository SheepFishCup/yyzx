package com.cqupt.task.job;

import com.cqupt.feign.BackdownFeignClient;
import com.cqupt.feign.BedFeignClient;
import com.cqupt.feign.CustomerFeignClient;
import com.cqupt.pojo.Backdown;
import com.cqupt.pojo.Customer;
import com.cqupt.utils.ResultVo;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * 退住自动审批任务（Feign 版）
 * <p>通过 OpenFeign 调用 checkinout / bed / customer 服务，不再持有对方的 Mapper</p>
 */
@Slf4j
@Component
public class BackdownAutoApproveJob extends QuartzJobBean {

    private static BackdownFeignClient backdownFeignClient;
    private static CustomerFeignClient customerFeignClient;
    private static BedFeignClient bedFeignClient;

    @Autowired
    public void setBackdownFeignClient(BackdownFeignClient c) {
        BackdownAutoApproveJob.backdownFeignClient = c;
    }

    @Autowired
    public void setCustomerFeignClient(CustomerFeignClient c) {
        BackdownAutoApproveJob.customerFeignClient = c;
    }

    @Autowired
    public void setBedFeignClient(BedFeignClient c) {
        BackdownAutoApproveJob.bedFeignClient = c;
    }

    @Override
    protected void executeInternal(JobExecutionContext context) {
        log.info("开始处理待审批的退住申请...");
        try {
            // Feign 调用 checkinout 服务获取待审批列表
            ResultVo<List<Backdown>> result = backdownFeignClient.listPending();
            List<Backdown> pendingList = result != null ? result.getData() : null;

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
                        long hoursDiff = (System.currentTimeMillis() - createTime.getTime())
                                / (1000 * 60 * 60);
                        if (hoursDiff < 24) {
                            log.debug("退住申请 ID={} 提交不到24小时，暂不处理", backdown.getId());
                            continue;
                        }
                    }

                    // Feign 调用: 自动审批通过
                    backdownFeignClient.approve(backdown.getId());
                    successCount++;
                    log.info("退住申请 ID={} 自动审批通过", backdown.getId());

                    // Feign 调用: 查询客户 → 释放床位
                    ResultVo<Customer> custResult = customerFeignClient.getById(backdown.getCustomerId());
                    if (custResult != null && custResult.getData() != null) {
                        Integer bedId = custResult.getData().getBedId();
                        if (bedId != null) {
                            bedFeignClient.updateStatus(bedId, 1); // 1=空闲
                        }
                    }
                } catch (Exception e) {
                    failCount++;
                    log.error("处理退住申请 ID={} 时发生异常", backdown.getId(), e);
                }
            }
            log.info("退住自动审批完成：成功{}条，失败{}条", successCount, failCount);
        } catch (Exception e) {
            log.error("处理待审批退住申请失败", e);
        }
    }
}
