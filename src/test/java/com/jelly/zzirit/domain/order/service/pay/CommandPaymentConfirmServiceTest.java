package com.jelly.zzirit.domain.order.service.pay;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import com.jelly.zzirit.domain.order.dto.response.PaymentResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.jelly.zzirit.domain.member.domain.MemberFixture;
import com.jelly.zzirit.domain.member.entity.Member;
import com.jelly.zzirit.domain.order.domain.fixture.OrderFixture;
import com.jelly.zzirit.domain.order.dto.response.PaymentConfirmResponse;
import com.jelly.zzirit.domain.order.entity.Order;
import com.jelly.zzirit.domain.order.entity.Payment;
import com.jelly.zzirit.domain.order.infra.feign.TossPaymentClient;
import com.jelly.zzirit.domain.order.infra.feign.dto.TossPaymentConfirmRequest;
import com.jelly.zzirit.domain.order.repository.PaymentRepository;
import com.jelly.zzirit.domain.order.repository.order.OrderRepository;
import com.jelly.zzirit.domain.order.service.message.OrderConfirmMessage;
import com.jelly.zzirit.domain.order.service.message.OrderConfirmProducer;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidOrderException;

@ExtendWith(MockitoExtension.class)
class CommandPaymentConfirmServiceTest {

	@InjectMocks
	private CommandPaymentConfirmService service;

	@Mock
	private TossPaymentClient tossPaymentClient;

	@Mock
	private OrderRepository orderRepository;

	@Mock
	private PaymentRepository paymentRepository;

	@Mock
	private OrderConfirmProducer orderConfirmProducer;

	private final String paymentKey = "pay_abc123";
	private final String amount = "15000";

	@Test
	void 결제확인_성공시_결제정보저장_및_MQ_전송() {
		// given
		Member member = MemberFixture.일반_회원();
		Order order = OrderFixture.결제된_주문_생성(member); // orderNumber 자동 생성

		String paymentKey = "pay_abc123";
		String amount = "15000";
		String method = "카드";

		when(orderRepository.findByOrderNumber(order.getOrderNumber()))
				.thenReturn(Optional.of(order));

		PaymentResponse paymentResponse = new PaymentResponse();
		paymentResponse.setMethod(method);


		TossPaymentConfirmRequest request = TossPaymentConfirmRequest.of(paymentKey, order.getOrderNumber(), amount);
		when(tossPaymentClient.confirmPayment(anyString(), refEq(request)))
				.thenReturn(paymentResponse);

		// when
		PaymentConfirmResponse response = service.confirmPayment(
			paymentKey, order.getOrderNumber(), amount
		);

		// then
		verify(tossPaymentClient).confirmPayment(anyString(), refEq(request));
		verify(paymentRepository).save(any(Payment.class));
		verify(orderConfirmProducer).send(any(OrderConfirmMessage.class));

		assertEquals(order.getOrderNumber(), response.orderId());
		assertEquals(paymentKey, response.paymentKey());
		assertEquals(order.getTotalPrice().intValue(), response.amount());
	}


	@Test
	void 주문번호가_존재하지_않으면_예외발생() {
		// given
		String unknownOrderNumber = "ORD999";
		when(orderRepository.findByOrderNumber(unknownOrderNumber))
			.thenReturn(Optional.empty());

		// when & then
		InvalidOrderException ex = assertThrows(InvalidOrderException.class, () ->
			service.confirmPayment(paymentKey, unknownOrderNumber, amount)
		);

		assertEquals(BaseResponseStatus.ORDER_NOT_FOUND, ex.getStatus());
		verifyNoInteractions(tossPaymentClient, paymentRepository, orderConfirmProducer);
	}

	@Test
	void 토스_API_요청중_예외발생시_이후_로직_실행되지_않는다() {
		// given
		Member member = MemberFixture.일반_회원();
		Order order = OrderFixture.결제된_주문_생성(member);

		when(orderRepository.findByOrderNumber(order.getOrderNumber()))
			.thenReturn(Optional.of(order));

		TossPaymentConfirmRequest request = TossPaymentConfirmRequest.of(paymentKey, order.getOrderNumber(), amount);
		doThrow(new RuntimeException("토스 에러"))
			.when(tossPaymentClient).confirmPayment(anyString(), refEq(request));

		// when & then
		RuntimeException ex = assertThrows(RuntimeException.class, () ->
			service.confirmPayment(paymentKey, order.getOrderNumber(), amount)
		);

		assertEquals("토스 에러", ex.getMessage());
		verify(paymentRepository, never()).save(any());
		verify(orderConfirmProducer, never()).send(any());
	}
}