package com.jelly.zzirit.domain.order.service.order.manage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.jelly.zzirit.domain.order.dto.response.PaymentResponse;
import com.jelly.zzirit.domain.order.entity.Order;
import com.jelly.zzirit.domain.order.entity.Payment;
import com.jelly.zzirit.domain.order.infra.feign.TossPaymentClient;
import com.jelly.zzirit.domain.order.repository.order.OrderRepository;
import com.jelly.zzirit.domain.order.service.message.OrderConfirmMessage;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidOrderException;

@ExtendWith(MockitoExtension.class)
class CommandConfirmServiceTest {

	@InjectMocks
	private CommandConfirmService commandConfirmService;

	@Mock
	private TossPaymentClient tossPaymentClient;

	@Mock
	private CommandOrderService commandOrderService;

	@Mock
	private OrderRepository orderRepository;

	@Test
	void 결제정보_확인_및_주문확정_성공() {
		// given
		String orderNumber = "ORD123";
		String paymentKey = "pay_abc123";
		String amount = "15000";
		String paymentMethod = "카드";

		OrderConfirmMessage message = new OrderConfirmMessage(orderNumber, paymentKey, amount, List.of());

		Payment payment = mock(Payment.class);
		Order order = mock(Order.class);
		when(order.getPayment()).thenReturn(payment);

		PaymentResponse paymentResponse = new PaymentResponse();
		paymentResponse.setMethod(paymentMethod);

		when(orderRepository.findWithPaymentByOrderNumber(orderNumber)).thenReturn(Optional.of(order));
		when(tossPaymentClient.fetchPaymentInfo(paymentKey)).thenReturn(paymentResponse);

		// when
		commandConfirmService.confirmWithTx(orderNumber, message);

		// then
		verify(tossPaymentClient).fetchPaymentInfo(paymentKey);
		verify(tossPaymentClient).validate(order, paymentResponse, amount);
		verify(payment).changeStatus(Payment.PaymentStatus.DONE);
		verify(payment).changeMethod(paymentMethod);
		verify(commandOrderService).completeOrder(order);
	}

	@Test
	void 결제정보_유효성검사_실패_시_예외발생() {
		// given
		String orderNumber = "ORD123";
		String paymentKey = "pay_abc123";
		String amount = "15000";

		OrderConfirmMessage message = new OrderConfirmMessage(orderNumber, paymentKey, amount, List.of());

		Order order = mock(Order.class);
		PaymentResponse paymentResponse = new PaymentResponse();

		when(orderRepository.findWithPaymentByOrderNumber(orderNumber)).thenReturn(Optional.of(order));
		when(tossPaymentClient.fetchPaymentInfo(paymentKey)).thenReturn(paymentResponse);
		doThrow(new InvalidOrderException(BaseResponseStatus.ORDER_NOT_FOUND))
				.when(tossPaymentClient).validate(order, paymentResponse, amount);

		// when & then
		assertThrows(InvalidOrderException.class, () ->
				commandConfirmService.confirmWithTx(orderNumber, message)
		);

		verify(tossPaymentClient).fetchPaymentInfo(paymentKey);
		verify(tossPaymentClient).validate(order, paymentResponse, amount);
		verifyNoInteractions(commandOrderService);
	}
}