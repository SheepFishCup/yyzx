package com.cqupt.feign;

import com.cqupt.constant.InternalTokenConstant;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Feign 请求拦截器
 * <p>对所有 Feign 调用自动注入 X-Internal-Token 共享密钥，
 * 使被调用方的 InternalAuthInterceptor 能够校验通过</p>
 */
@Slf4j
@Component
public class FeignRequestInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        template.header(InternalTokenConstant.HEADER_NAME,
                InternalTokenConstant.DEFAULT_TOKEN);
    }
}
