package com.jelly.zzirit.global.interceptor;

import static java.nio.charset.StandardCharsets.*;

import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import feign.RequestInterceptor;
import feign.RequestTemplate;

@Component
public class PaymentAuthInterceptor implements RequestInterceptor {

	@Value("${toss.payments.secret-key}")
	private String secretKey;

	@Override
	public void apply(RequestTemplate template) {
		String authHeader = "Basic " + Base64.getEncoder().encodeToString((secretKey + ":").getBytes(UTF_8));
		template.header("Authorization", authHeader);
		template.header("Content-Type", "application/json");
	}
}
