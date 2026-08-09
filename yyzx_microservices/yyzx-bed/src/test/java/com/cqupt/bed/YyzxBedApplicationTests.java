package com.cqupt.bed;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {
        "spring.cloud.nacos.discovery.enabled=false",
        "spring.cloud.nacos.config.enabled=false"
})
@ActiveProfiles("test")
class YyzxBedApplicationTests {
    @Test
    void contextLoads() {}
}
