package com.jelly.zzirit.global.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(value = {"com.jelly.zzirit.domain.order.infra.feign"})
public class FeignClientConfig {
}
