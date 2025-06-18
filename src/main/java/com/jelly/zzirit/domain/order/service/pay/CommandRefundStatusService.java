package com.jelly.zzirit.domain.order.service.pay;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.jelly.zzirit.domain.order.dto.StockChangeEvent;
import com.jelly.zzirit.domain.order.entity.Order;
import com.jelly.zzirit.domain.order.entity.OrderStatus;
import com.jelly.zzirit.domain.order.entity.Payment;
import com.jelly.zzirit.domain.order.repository.order.OrderRepository;
import com.jelly.zzirit.domain.order.repository.PaymentRepository;
import com.jelly.zzirit.domain.order.util.AsyncStockHistoryUploader;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidOrderException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommandRefundStatusService {

	private final AsyncStockHistoryUploader asyncStockHistoryUploader;
	private final PaymentRepository paymentRepository;
	private final OrderRepository orderRepository;

	@Transactional
	public void markAsRefunded(Order order, String paymentKey, boolean isRefundSuccessful) {

		Payment payment = paymentRepository.findByPaymentKey(paymentKey)
			.orElseThrow(() -> new InvalidOrderException(BaseResponseStatus.PAYMENT_NOT_FOUND));

		if (isRefundSuccessful) {
			order.changeStatus(OrderStatus.CANCELLED);
			payment.changeStatus(Payment.PaymentStatus.CANCELLED);
		} else {
			order.changeStatus(OrderStatus.FAILED);
			payment.changeStatus(Payment.PaymentStatus.FAILED);
		}

		orderRepository.save(order);
		paymentRepository.save(payment);

		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				asyncStockHistoryUploader.upload(
					StockChangeEvent.refund(order.getOrderNumber(), order.getOrderItems().size())
				);
			}
		});
	}
}