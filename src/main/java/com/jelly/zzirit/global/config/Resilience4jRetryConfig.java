package com.jelly.zzirit.global.config;

import java.net.SocketTimeoutException;
import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import feign.FeignException;
import feign.RetryableException;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;

@Configuration
public class Resilience4jRetryConfig {

	public static final String TOSS_PAYMENT_RETRY = "tossPaymentRetry";

	@Bean(name = TOSS_PAYMENT_RETRY)
	public RetryRegistry retryConfig() {
		return RetryRegistry.of(RetryConfig.custom()
			.maxAttempts(4)
			.intervalFunction(IntervalFunction.ofExponentialRandomBackoff(Duration.ofMillis(3000), 2))
			.retryExceptions(FeignException.FeignServerException.class)
			.retryOnException(
				throwable -> !(throwable instanceof FeignException.FeignClientException)
					&& !(throwable instanceof RetryableException))
			.build());
	}
}
