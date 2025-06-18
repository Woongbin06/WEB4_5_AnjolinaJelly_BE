package com.jelly.zzirit.domain.order.service.pay;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

import com.jelly.zzirit.domain.order.dto.response.PaymentResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jelly.zzirit.domain.order.dto.response.PaymentConfirmResponse;
import com.jelly.zzirit.domain.order.entity.Order;
import com.jelly.zzirit.domain.order.entity.Payment;
import com.jelly.zzirit.domain.order.infra.feign.TossPaymentClient;
import com.jelly.zzirit.domain.order.infra.feign.dto.TossPaymentConfirmRequest;
import com.jelly.zzirit.domain.order.repository.order.OrderRepository;
import com.jelly.zzirit.domain.order.repository.PaymentRepository;
import com.jelly.zzirit.domain.order.service.message.OrderConfirmMessage;
import com.jelly.zzirit.domain.order.service.message.OrderConfirmProducer;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidOrderException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommandPaymentConfirmService {

	private final OrderRepository orderRepository;
	private final PaymentRepository paymentRepository;
	private final OrderConfirmProducer orderConfirmProducer;
	private final TossPaymentClient tossPaymentClient;

	@Value("${toss.payments.secret-key}")
	private String secretKey;

	@Transactional
	public PaymentConfirmResponse confirmPayment(String paymentKey, String orderNumber, String amount) {
		log.info("[결제확정요청] 시작 - orderNumber={}, paymentKey={}, amount={}", orderNumber, paymentKey, amount);

		Order order = orderRepository.findByOrderNumber(orderNumber)
				.orElseThrow(() -> {
					System.out.println("[결제확정요청] 주문 조회 실패 - orderNumber=" + orderNumber);
					return new InvalidOrderException(BaseResponseStatus.ORDER_NOT_FOUND);
				});

		log.info("[결제확정요청] 주문 조회 성공 - memberId={}", order.getMember().getId());

		String auth = "Basic" + Base64.getEncoder()
			.encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
		String idempotencyKey = UUID.randomUUID().toString();
		TossPaymentConfirmRequest request = TossPaymentConfirmRequest.of(paymentKey, orderNumber, amount);
		PaymentResponse paymentResponse = tossPaymentClient.confirmPayment(auth, idempotencyKey, request);
		log.info("[결제확정요청] confirm 응답: method={}, status={}, totalAmount={}", paymentResponse.getMethod(),
			paymentResponse.getStatus(), paymentResponse.getTotalAmount());


		Payment payment = Payment.of(paymentKey, order);
		paymentRepository.save(payment);
		paymentRepository.flush();
		log.info("[결제확정요청] 결제 정보 저장 완료 - paymentKey={}", paymentKey);

		OrderConfirmMessage message = OrderConfirmMessage.from(order, paymentKey, amount);
		orderConfirmProducer.send(message);
		log.info("[결제확정요청] 주문 확정 메시지 발행 완료 - queue=order.confirm.queue");

		return PaymentConfirmResponse.from(order, paymentKey);
	}
}