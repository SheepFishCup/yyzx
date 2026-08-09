package com.cqupt.feign;

import com.cqupt.pojo.Bed;
import com.cqupt.utils.ResultVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * BedFeignClient 降级工厂
 */
@Slf4j
@Component
public class BedFeignClientFallbackFactory implements FallbackFactory<BedFeignClient> {

    @Override
    public BedFeignClient create(Throwable cause) {
        log.error("BedFeignClient 熔断降级，原因: {}", cause.getMessage());

        return new BedFeignClient() {
            @Override
            public ResultVo<Bed> getBedById(Integer bedId) {
                log.warn("Fallback: getBedById({}) → 服务不可用", bedId);
                return ResultVo.fail("床位服务暂不可用，请稍后重试");
            }
            @Override
            public ResultVo<Boolean> checkAvailable(Integer bedId) {
                log.warn("Fallback: checkAvailable({}) → 服务不可用", bedId);
                return ResultVo.fail("床位服务暂不可用，请稍后重试");
            }
            @Override
            public ResultVo<Void> updateStatus(Integer bedId, Integer status) {
                log.warn("Fallback: updateStatus({},{}) → 服务不可用", bedId, status);
                return ResultVo.fail("床位服务暂不可用，请稍后重试");
            }
        };
    }
}
