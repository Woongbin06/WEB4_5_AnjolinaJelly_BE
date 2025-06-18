package com.jelly.zzirit.domain.order.service.pay;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.jelly.zzirit.domain.order.entity.Order;
import com.jelly.zzirit.domain.order.infra.feign.TossPaymentClient;
import com.jelly.zzirit.domain.order.infra.feign.dto.TossPaymentRefundRequest;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidOrderException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommandRefundService {

	private final TossPaymentClient tossPaymentClient;
	private final CommandRefundStatusService commandRefundStatusService;

	@Value("${toss.payments.secret-key}")
	private String secretKey;

	public void refund(Order order, String paymentKey, String reason) {
		boolean isRefundSuccessful = false;

		try {
			String auth = "Basic" + Base64.getEncoder()
				.encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
			TossPaymentRefundRequest request = TossPaymentRefundRequest.of(reason, order.getTotalPrice());
			tossPaymentClient.refundPayment(auth, paymentKey, request);
			isRefundSuccessful = true;
		} catch (Exception e) {
			log.error("환불 처리 중 예외 발생: order={}, reason={}", order.getOrderNumber(), reason, e);
			throw new InvalidOrderException(BaseResponseStatus.ORDER_REFUND_FAILED);
		} finally {
			commandRefundStatusService.markAsRefunded(order, paymentKey, isRefundSuccessful);
		}
	}
}