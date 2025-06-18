package com.jelly.zzirit.domain.order.service.pay;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.jelly.zzirit.domain.member.domain.MemberFixture;
import com.jelly.zzirit.domain.member.entity.Member;
import com.jelly.zzirit.domain.order.domain.fixture.OrderFixture;
import com.jelly.zzirit.domain.order.entity.Order;
import com.jelly.zzirit.domain.order.infra.feign.TossPaymentClient;
import com.jelly.zzirit.domain.order.infra.feign.dto.TossPaymentRefundRequest;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidOrderException;

@ExtendWith(MockitoExtension.class)
class CommandRefundServiceTest {

	@InjectMocks
	private CommandRefundService commandRefundService;

	@Mock
	private TossPaymentClient tossPaymentClient;

	@Mock
	private CommandRefundStatusService commandRefundStatusService;

	private final String paymentKey = "pay_abc123";
	private final String reason = "테스트 환불 사유";

	@Test
	void 환불_성공시_API호출_및_성공상태_반영() {
		// given
		Member member = MemberFixture.일반_회원();
		Order order = OrderFixture.결제된_주문_생성(member);

		TossPaymentRefundRequest request = TossPaymentRefundRequest.of(reason, order.getTotalPrice());
		doNothing().when(tossPaymentClient).refundPayment(anyString(), anyString(), refEq(request));

		// when
		commandRefundService.refund(order, paymentKey, reason);

		// then
		verify(tossPaymentClient).refundPayment(anyString(), anyString(), refEq(request));
		verify(commandRefundStatusService).markAsRefunded(order, paymentKey, true);
	}

	@Test
	void 환불_실패시_API예외_발생_및_실패상태_반영() {
		// given
		Member member = MemberFixture.일반_회원();
		Order order = OrderFixture.결제된_주문_생성(member);

		TossPaymentRefundRequest request = TossPaymentRefundRequest.of(reason, order.getTotalPrice());
		doThrow(new RuntimeException("API 오류"))
			.when(tossPaymentClient)
			.refundPayment(anyString(), anyString(), refEq(request));

		// when & then
		InvalidOrderException ex = assertThrows(InvalidOrderException.class, () ->
			commandRefundService.refund(order, paymentKey, reason)
		);

		assertEquals(BaseResponseStatus.ORDER_REFUND_FAILED, ex.getStatus());
		verify(commandRefundStatusService).markAsRefunded(order, paymentKey, false);
	}
}