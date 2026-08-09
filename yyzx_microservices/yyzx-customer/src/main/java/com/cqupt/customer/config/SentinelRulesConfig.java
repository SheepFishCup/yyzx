package com.cqupt.customer.config;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * Sentinel 规则初始化
 * <p>硬编码规则仅用于演示，生产环境建议通过 Sentinel Dashboard 或 Nacos 动态配置</p>
 */
@Slf4j
@Configuration
public class SentinelRulesConfig {

    @PostConstruct
    public void initFlowRules() {
        List<FlowRule> rules = new ArrayList<>();

        // listKhxxPage — QPS 限流：每秒最多 100 个请求
        FlowRule rule = new FlowRule("listKhxxPage");
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);  // QPS 模式
        rule.setCount(100);                           // 阈值 100
        rule.setLimitApp("default");                  // 对所有调用生效
        rules.add(rule);

        FlowRuleManager.loadRules(rules);
        log.info("Sentinel 限流规则已加载: listKhxxPage QPS=100");

        // 熔断降级规则：慢调用比例 > 50% 且最小 5 个请求时熔断
        List<DegradeRule> degradeRules = new ArrayList<>();
        DegradeRule degradeRule = new DegradeRule("listKhxxPage");
        degradeRule.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO); // 异常比例
        degradeRule.setCount(0.5);      // 异常比例 50%
        degradeRule.setMinRequestAmount(5);  // 最小请求数
        degradeRule.setTimeWindow(10);  // 熔断时长 10s
        degradeRules.add(degradeRule);

        DegradeRuleManager.loadRules(degradeRules);
        log.info("Sentinel 熔断规则已加载: listKhxxPage 异常比例>50% 熔断10s");
    }
}
