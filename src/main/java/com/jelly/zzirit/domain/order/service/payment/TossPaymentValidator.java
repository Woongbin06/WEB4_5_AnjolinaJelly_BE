package com.jelly.zzirit.domain.order.service.payment;

import org.springframework.stereotype.Service;

import com.jelly.zzirit.domain.order.dto.response.PaymentResponse;
import com.jelly.zzirit.domain.order.entity.Order;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TossPaymentValidator {

	public void validate(Order order, PaymentResponse response, String amount) {
		TossPaymentValidation.validateAll(order, response, amount);
	}
}