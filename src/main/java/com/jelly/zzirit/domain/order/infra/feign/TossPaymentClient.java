package com.jelly.zzirit.domain.order.infra.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.jelly.zzirit.domain.order.dto.response.PaymentResponse;
import com.jelly.zzirit.domain.order.infra.feign.dto.TossPaymentConfirmRequest;
import com.jelly.zzirit.domain.order.infra.feign.dto.TossPaymentRefundRequest;

@FeignClient(
	name = "TossPaymentClient",
	url = "https://api.tosspayments.com/v1/payments"
)
public interface TossPaymentClient {

	@PostMapping(value = "/confirm")
	PaymentResponse confirmPayment(
		@RequestHeader("Idempotency-Key") String idempotencyKey,
		@RequestBody TossPaymentConfirmRequest request
	);

	@GetMapping(value = "/{paymentKey}")
	PaymentResponse fetchPaymentInfo(
		@PathVariable(name = "paymentKey") String paymentKey
	);

	@PostMapping(value = "/{paymentKey}/cancel")
	void refundPayment(
		@PathVariable(name = "paymentKey") String paymentKey,
		@RequestBody TossPaymentRefundRequest request
	);
}