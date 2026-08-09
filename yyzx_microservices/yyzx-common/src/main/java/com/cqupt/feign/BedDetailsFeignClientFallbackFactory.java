package com.cqupt.feign;

import com.cqupt.pojo.BedDetails;
import com.cqupt.utils.ResultVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BedDetailsFeignClientFallbackFactory implements FallbackFactory<BedDetailsFeignClient> {

    @Override
    public BedDetailsFeignClient create(Throwable cause) {
        log.error("BedDetailsFeignClient 熔断降级，原因: {}", cause.getMessage());

        return new BedDetailsFeignClient() {
            @Override
            public ResultVo<BedDetails> createBedDetails(BedDetails bedDetails) {
                log.warn("Fallback: createBedDetails → 服务不可用");
                return ResultVo.fail("床位详情服务暂不可用，请稍后重试");
            }
            @Override
            public ResultVo<Void> closeBedDetails(Long bedDetailsId) {
                log.warn("Fallback: closeBedDetails({}) → 服务不可用", bedDetailsId);
                return ResultVo.fail("床位详情服务暂不可用，请稍后重试");
            }
            @Override
            public ResultVo<Void> updateBedDetails(Long id, BedDetails bedDetails) {
                log.warn("Fallback: updateBedDetails({}) → 服务不可用", id);
                return ResultVo.fail("床位详情服务暂不可用，请稍后重试");
            }
            @Override
            public ResultVo<Void> updateByCustomer(Long customerId, BedDetails bedDetails) {
                log.warn("Fallback: updateByCustomer({}) → 服务不可用", customerId);
                return ResultVo.fail("床位详情服务暂不可用，请稍后重试");
            }
        };
    }
}
