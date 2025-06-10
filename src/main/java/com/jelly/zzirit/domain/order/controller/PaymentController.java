package com.jelly.zzirit.domain.order.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jelly.zzirit.domain.order.dto.request.PaymentRequest;
import com.jelly.zzirit.domain.order.dto.response.PaymentConfirmResponse;
import com.jelly.zzirit.domain.order.dto.response.PaymentInitResponse;
import com.jelly.zzirit.domain.order.service.order.manage.CommandTempOrderService;
import com.jelly.zzirit.domain.order.service.pay.CommandPaymentConfirmService;
import com.jelly.zzirit.domain.order.service.pay.CommandPaymentInitService;
import com.jelly.zzirit.global.dto.BaseResponse;
import com.jelly.zzirit.global.dto.BaseResponseStatus;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {

	private final CommandPaymentInitService commandPaymentInitService;
	private final CommandTempOrderService commandTempOrderService;
	private final CommandPaymentConfirmService commandPaymentConfirmService;

	@PostMapping("/init")
	public PaymentInitResponse initOrder(@RequestBody @Valid PaymentRequest requestDto) {
		return commandPaymentInitService.createOrderAndReturnInit(requestDto);
	}

	@GetMapping("/success")
	public PaymentConfirmResponse confirmPayment(
		@RequestParam("paymentKey") String paymentKey,
		@RequestParam("orderId") String orderId,
		@RequestParam("amount") String amount
	) {
		return commandPaymentConfirmService.confirmPayment(paymentKey, orderId, amount);
	}

	@GetMapping("/fail")
	public BaseResponse<String> failPayment(
		@RequestParam(required = false) String code,
		@RequestParam(required = false) String message,
		@RequestParam(required = false) String orderId
	) {
		commandTempOrderService.deleteTempOrder(orderId);
		String failReason = String.format("결제 실패 (%s): %s | 주문번호: %s", code, message, orderId);
		return BaseResponse.error(BaseResponseStatus.TOSS_PAYMENT_REQUEST_FAILED, failReason);
	}
}