package com.cqupt.feign;

import com.cqupt.pojo.Bed;
import com.cqupt.utils.ResultVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 床位服务 Feign 客户端
 * <p>供 customer、checkinout、task 等模块调用 yyzx-bed</p>
 */
@FeignClient(name = "yyzx-bed", contextId = "bedFeignClient", path = "/yyzx/internal/bed",
        fallbackFactory = BedFeignClientFallbackFactory.class)
public interface BedFeignClient {

    @GetMapping("/{bedId}")
    ResultVo<Bed> getBedById(@PathVariable("bedId") Integer bedId);

    @GetMapping("/checkAvailable")
    ResultVo<Boolean> checkAvailable(@RequestParam("bedId") Integer bedId);

    @PutMapping("/updateStatus")
    ResultVo<Void> updateStatus(@RequestParam("bedId") Integer bedId,
                                 @RequestParam("status") Integer status);
}
