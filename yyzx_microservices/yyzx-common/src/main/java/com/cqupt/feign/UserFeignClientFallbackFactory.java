package com.cqupt.feign;

import com.cqupt.pojo.User;
import com.cqupt.utils.ResultVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * UserFeignClient 降级工厂
 * <p>当 yyzx-auth 服务不可用时，返回空列表/空对象</p>
 */
@Slf4j
@Component
public class UserFeignClientFallbackFactory implements FallbackFactory<UserFeignClient> {

    @Override
    public UserFeignClient create(Throwable cause) {
        log.error("UserFeignClient 熔断降级，原因: {}", cause.getMessage());

        return new UserFeignClient() {
            @Override
            public ResultVo<User> getById(Long userId) {
                log.warn("Fallback: getById({}) → 服务不可用", userId);
                return ResultVo.fail("认证服务暂不可用，请稍后重试");
            }

            @Override
            public ResultVo<List<User>> listByRole(Integer roleId) {
                log.warn("Fallback: listByRole({}) → 服务不可用，返回空列表", roleId);
                return ResultVo.ok(Collections.emptyList());
            }

            @Override
            public ResultVo<User> getByEmail(String email) {
                log.warn("Fallback: getByEmail({}) → 服务不可用", email);
                return ResultVo.fail("认证服务暂不可用，请稍后重试");
            }
        };
    }
}
