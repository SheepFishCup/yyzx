package com.cqupt.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cqupt.dto.OutwardDTO;
import com.cqupt.pojo.Backdown;
import com.cqupt.utils.ResultVo;
import com.cqupt.vo.OutwardVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 出入管理 Feign 客户端
 * <p>供 yyzx-task / yyzx-nursing 调用</p>
 */
@FeignClient(name = "yyzx-checkinout", contextId = "backdownFeignClient", path = "/yyzx/internal")
public interface BackdownFeignClient {

    /** 查询所有待审批退住申请 */
    @GetMapping("/backdown/pending")
    ResultVo<List<Backdown>> listPending();

    /** 自动审批通过 */
    @PutMapping("/backdown/{id}/approve")
    ResultVo<Void> approve(@PathVariable("id") Long id);

    /** 分页查询外出记录（供护理记录页面使用） */
    @GetMapping("/outward/queryVo")
    ResultVo<Page<OutwardVo>> queryOutwardVo(@RequestParam("current") Long current,
                                              @RequestParam("pageSize") Long pageSize,
                                              @RequestParam(value = "userId", required = false) Long userId,
                                              @RequestParam(value = "customerId", required = false) Long customerId);
}

/**
 * 护理服务 Feign 客户端
 * <p>供其他模块调用 yyzx-nursing</p>
 */
@FeignClient(name = "yyzx-nursing", path = "/yyzx/internal/nursing")
interface NursingFeignClient {
    // 预留：后续如需 Feign 调用护理服务可在此扩展
}
