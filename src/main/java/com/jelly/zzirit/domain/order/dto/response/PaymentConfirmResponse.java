package com.jelly.zzirit.domain.order.dto.response;

import com.jelly.zzirit.domain.order.entity.Order;

public record PaymentConfirmResponse(
	String orderId,
	String paymentKey,
	int amount

) {

	public static PaymentConfirmResponse from(Order order, String paymentKey) {
		return new PaymentConfirmResponse(
			order.getOrderNumber(),
			paymentKey,
			order.getTotalPrice().intValue()
		);
	}
}