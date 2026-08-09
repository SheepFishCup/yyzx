package com.cqupt.feign;

import com.cqupt.pojo.Customer;
import com.cqupt.utils.ResultVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * CustomerFeignClient 降级工厂
 * <p>当 yyzx-customer 服务不可用时，返回安全的默认值</p>
 */
@Slf4j
@Component
public class CustomerFeignClientFallbackFactory implements FallbackFactory<CustomerFeignClient> {

    @Override
    public CustomerFeignClient create(Throwable cause) {
        log.error("CustomerFeignClient 熔断降级，原因: {}", cause.getMessage());

        return new CustomerFeignClient() {
            @Override
            public ResultVo<Customer> getById(Long customerId) {
                log.warn("Fallback: getById({}) → 服务不可用", customerId);
                return ResultVo.fail("客户服务暂不可用，请稍后重试");
            }

            @Override
            public ResultVo<Void> updateLevel(Long customerId, Integer levelId) {
                log.warn("Fallback: updateLevel({},{}) → 服务不可用", customerId, levelId);
                return ResultVo.fail("客户服务暂不可用，请稍后重试");
            }

            @Override
            public ResultVo<Void> updateCustomer(Long customerId, Customer customer) {
                log.warn("Fallback: updateCustomer({}) → 服务不可用", customerId);
                return ResultVo.fail("客户服务暂不可用，请稍后重试");
            }
        };
    }
}
