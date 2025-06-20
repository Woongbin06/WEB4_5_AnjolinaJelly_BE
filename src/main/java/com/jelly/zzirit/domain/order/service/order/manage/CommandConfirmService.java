package com.jelly.zzirit.domain.order.service.order.manage;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jelly.zzirit.domain.order.dto.response.PaymentResponse;
import com.jelly.zzirit.domain.order.entity.Order;
import com.jelly.zzirit.domain.order.entity.Payment;
import com.jelly.zzirit.domain.order.infra.feign.TossPaymentClient;
import com.jelly.zzirit.domain.order.repository.order.OrderRepository;
import com.jelly.zzirit.domain.order.service.message.OrderConfirmMessage;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidOrderException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommandConfirmService {

	private final TossPaymentClient tossPaymentClient;
	private final CommandOrderService commandOrderService;
	private final OrderRepository orderRepository;

	@Transactional
	public void confirmWithTx(String orderNumber, OrderConfirmMessage message) {
		Order order = orderRepository.findWithPaymentByOrderNumber(orderNumber)
				.orElseThrow(() -> new InvalidOrderException(BaseResponseStatus.ORDER_NOT_FOUND));
		confirm(order, message);
	}

	private void confirm(Order order, OrderConfirmMessage message) {
		try {
			PaymentResponse paymentInfo = tossPaymentClient.fetchPaymentInfo(message.getPaymentKey());
			tossPaymentClient.validate(order, paymentInfo, message.getAmount());

			Payment payment = order.getPayment();
			payment.changeStatus(Payment.PaymentStatus.DONE);
			payment.changeMethod(paymentInfo.getMethod());

			commandOrderService.completeOrder(order);
		} catch (Exception e) {
			throw e;
		}
	}

	public Order findOrderOrThrow(String orderNumber) {
		return orderRepository.findWithPaymentByOrderNumber(orderNumber)
				.orElseThrow(() -> new InvalidOrderException(BaseResponseStatus.ORDER_NOT_FOUND));
	}
}