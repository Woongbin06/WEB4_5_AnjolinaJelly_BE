package com.jelly.zzirit.domain.order.infra.feign.dto;

public record TossPaymentConfirmRequest(
	String paymentKey,
	String orderId,
	String amount
) {

	public static TossPaymentConfirmRequest of(String paymentKey, String orderId, String amount) {
		return new TossPaymentConfirmRequest(paymentKey, orderId, amount);
	}
}
