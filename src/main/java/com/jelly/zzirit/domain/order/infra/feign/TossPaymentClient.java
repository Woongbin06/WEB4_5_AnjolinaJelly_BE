package com.jelly.zzirit.domain.order.infra.feign;

import static com.jelly.zzirit.global.config.Resilience4jRetryConfig.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.jelly.zzirit.domain.order.dto.response.PaymentResponse;
import com.jelly.zzirit.domain.order.entity.Order;
import com.jelly.zzirit.domain.order.infra.feign.dto.TossPaymentConfirmRequest;
import com.jelly.zzirit.domain.order.infra.feign.dto.TossPaymentRefundRequest;
import com.jelly.zzirit.domain.order.service.payment.TossPaymentValidation;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidOrderException;

import feign.RetryableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

@FeignClient(
	name = "TossPaymentClient",
	url = "https://api.tosspayments.com/v1/payments"
)
public interface TossPaymentClient {

	Logger log = LoggerFactory.getLogger(TossPaymentClient.class);

	@Retry(name = TOSS_PAYMENT_RETRY)
	@CircuitBreaker(name = "tossPaymentClient", fallbackMethod = "fallbackConfirm")
	@PostMapping(value = "/confirm")
	PaymentResponse confirmPayment(
		@RequestHeader("Idempotency-Key") String idempotencyKey,
		@RequestBody TossPaymentConfirmRequest request
	);

	@Retry(name = TOSS_PAYMENT_RETRY)
	@CircuitBreaker(name = "tossPaymentClient", fallbackMethod = "fallbackFetch")
	@GetMapping(value = "/{paymentKey}")
	PaymentResponse fetchPaymentInfo(
		@PathVariable(name = "paymentKey") String paymentKey
	);

	@Retry(name = TOSS_PAYMENT_RETRY)
	@CircuitBreaker(name = "tossPaymentClient", fallbackMethod = "fallbackRefund")
	@PostMapping(value = "/{paymentKey}/cancel")
	void refundPayment(
		@PathVariable(name = "paymentKey") String paymentKey,
		@RequestBody TossPaymentRefundRequest request
	);

	default void validate(Order order, PaymentResponse response, String amount) {
		TossPaymentValidation.validateAll(order, response, amount);
	}

	default PaymentResponse fallbackConfirm(String idempotencyKey, TossPaymentConfirmRequest request, Throwable t) {
		log.error("Toss 결제 승인 요청 오류 : idempotencyKey = {}, paymentKey = {}, message = {}",
			idempotencyKey, request.paymentKey(), t.getMessage());
		throw new InvalidOrderException(BaseResponseStatus.TOSS_SERVER_FAILED);
	}

	default PaymentResponse fallbackConfirm(String idempotencyKey, TossPaymentConfirmRequest request, RetryableException e) {
		log.error("Toss 결제 재시도 실패 : idempotencyKey = {}, paymentKey = {}, message = {}",
			idempotencyKey, request.paymentKey(), e.getMessage());
		throw new InvalidOrderException(BaseResponseStatus.TOSS_RETRY_FAILED);
	}

	default PaymentResponse fallbackFetch(String paymentKey, Throwable t) {
		log.error("Toss 결제 조회 요청 오류 : paymentKey = {}, error = {}", paymentKey, t.getMessage());
		throw new InvalidOrderException(BaseResponseStatus.TOSS_SERVER_FAILED);
	}

	default PaymentResponse fallbackFetch(String paymentKey, RetryableException e) {
		log.error("Toss 결제 조회 재시도 실패 : paymentKey = {}, error = {}", paymentKey, e.getMessage());
		throw new InvalidOrderException(BaseResponseStatus.TOSS_RETRY_FAILED);
	}

	default void fallbackRefund(String paymentKey, TossPaymentRefundRequest request, Throwable t) {
		log.error("Toss 결제 환불 요청 오류 : paymentKey = {}, reason = {}, amount = {}, error = {}",
			paymentKey, request.reason(), request.amount(), t.getMessage());
		throw new InvalidOrderException(BaseResponseStatus.TOSS_SERVER_FAILED);
	}

	default void fallbackRefund(String paymentKey, TossPaymentRefundRequest request, RetryableException e) {
		log.error("Toss 결제 환불 재시도 실패 : paymentKey = {}, reason = {}, amount = {}, error = {}",
			paymentKey, request.reason(), request.amount(), e.getMessage());
		throw new InvalidOrderException(BaseResponseStatus.TOSS_RETRY_FAILED);
	}
}