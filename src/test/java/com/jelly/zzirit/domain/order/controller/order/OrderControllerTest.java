package com.jelly.zzirit.domain.order.controller.order;

import static com.epages.restdocs.apispec.RestAssuredRestDocumentationWrapper.*;
import static com.jelly.zzirit.domain.item.domain.fixture.BrandFixture.*;
import static com.jelly.zzirit.domain.item.domain.fixture.ItemFixture.*;
import static com.jelly.zzirit.domain.item.domain.fixture.TypeBrandFixture.*;
import static com.jelly.zzirit.domain.item.domain.fixture.TypeFixture.*;
import static com.jelly.zzirit.domain.member.domain.MemberFixture.*;
import static com.jelly.zzirit.domain.order.domain.fixture.OrderFixture.*;
import static com.jelly.zzirit.domain.order.domain.fixture.OrderItemFixture.*;
import static com.jelly.zzirit.global.dto.BaseResponseStatus.*;
import static io.restassured.RestAssured.*;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;

import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.restdocs.restassured.RestDocumentationFilter;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import com.jelly.zzirit.domain.item.entity.Brand;
import com.jelly.zzirit.domain.item.entity.Item;
import com.jelly.zzirit.domain.item.entity.Type;
import com.jelly.zzirit.domain.item.entity.TypeBrand;
import com.jelly.zzirit.domain.item.repository.BrandRepository;
import com.jelly.zzirit.domain.item.repository.ItemRepository;
import com.jelly.zzirit.domain.item.repository.TypeBrandRepository;
import com.jelly.zzirit.domain.item.repository.TypeRepository;
import com.jelly.zzirit.domain.member.entity.Member;
import com.jelly.zzirit.domain.member.repository.MemberRepository;
import com.jelly.zzirit.domain.order.entity.Order;
import com.jelly.zzirit.domain.order.entity.OrderStatus;
import com.jelly.zzirit.domain.order.entity.Payment;
import com.jelly.zzirit.domain.order.repository.order.OrderRepository;
import com.jelly.zzirit.domain.order.service.order.CommandStockService;
import com.jelly.zzirit.domain.order.service.order.cancel.OrderCancelValidator;
import com.jelly.zzirit.domain.order.service.pay.CommandRefundService;
import com.jelly.zzirit.domain.order.service.payment.TossPaymentValidator;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidOrderException;
import com.jelly.zzirit.global.support.AcceptanceTest;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class OrderControllerTest extends AcceptanceTest {

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private TypeRepository typeRepository;

	@Autowired
	private BrandRepository brandRepository;

	@Autowired
	private TypeBrandRepository typeBrandRepository;

	@Autowired
	private ItemRepository itemRepository;

	@MockitoBean // 외부 API 호출을 방지하고 동작을 제어하기 위해 mock 객체로 대체
	private CommandRefundService refundService;

	@MockitoSpyBean // 실제 동작을 기본으로 하되, 특정 메서드만 모킹하기 위해 spy 객체 사용
	private OrderCancelValidator orderCancelValidator;

	@MockitoSpyBean
	private CommandRefundService commandRefundService;

	@MockitoBean
	private TossPaymentValidator tossPaymentClient;

	@MockitoBean
	private CommandStockService commandStockService;

	private Member 유저;
	private List<Order> 주문_목록;

	@BeforeEach
	void setUp() {
		유저 = memberRepository.save(일반_회원());
		Type 상품_종류 = typeRepository.save(노트북());
		Brand 브랜드 = brandRepository.save(삼성());
		TypeBrand 상품_종류_브랜드 = typeBrandRepository.save(타입_브랜드_생성(상품_종류, 브랜드));

		주문_목록 = IntStream.range(0, 20)
			.mapToObj(i -> {
				Order 주문 = 결제된_주문_생성(유저);

				List<Item> 상품_목록 = List.of(
					itemRepository.save(상품_생성_이름("상품" + i + "-1", 상품_종류_브랜드)),
					itemRepository.save(상품_생성_이름("상품" + i + "-2", 상품_종류_브랜드))
				);

				상품_목록.forEach(상품 -> 주문.addOrderItem(주문_상품_생성(주문, 상품)));

				return 주문;
			})
			.toList();

		orderRepository.saveAll(주문_목록);
	}

	@Nested
	@DisplayName("주문 전체 조회 API")
	class FetchOrders {

		@Test
		void 내림차순으로_주문_전체를_조회하면_상태_코드_200을_응답한다() {
			// given
			Long 유저_아이디 = 유저.getId();

			RequestSpecification 요청 = given(spec)
				.cookie(getCookie(유저_아이디))
				.filter(성공_문서_생성("주문 전체 조회"));

			// when
			Response 응답 = 요청.when()
				.get("/api/orders");

			// then
			응답.then()
				.log().body()
				.statusCode(200);
		}

		@Test
		void 오름차순으로_주문_전체를_조회하면_상태_코드_200을_응답한다() {
			// given
			Long 유저_아이디 = 유저.getId();

			RequestSpecification 요청 = given(spec)
				.cookie(getCookie(유저_아이디))
				.filter(성공_문서_생성("주문 전체 조회"));

			// when
			Response 응답 = 요청.when()
				.get("/api/orders?sort=asc");

			// then
			응답.then()
				.log().body()
				.statusCode(200);
		}

		private RestDocumentationFilter 성공_문서_생성(String name) {
			return document(
				name,
				resourceDetails()
					.summary("주문 전체 조회")
					.description("유저의 전체 주문 목록을 조회합니다."),
				queryParameters(
					parameterWithName("page").description("페이지 번호(디폴트: 0)").optional(),
					parameterWithName("size").description("페이지 크기(디폴트: 10)").optional(),
					parameterWithName("sort").description("생성 일자 기준 정렬 방식(디폴트: desc)").optional()
				),
				responseFields(
					fieldWithPath("success").description("요청 성공 여부").type(BOOLEAN),
					fieldWithPath("code").description("커스텀 응답 코드").type(NUMBER),
					fieldWithPath("httpStatus").description("HTTP 상태 코드").type(NUMBER),
					fieldWithPath("message").description("응답 메시지").type(STRING),

					fieldWithPath("result.content[]").description("주문 목록").type(ARRAY),
					fieldWithPath("result.content[].orderDate").description("주문 일시(yyyy.MM.dd HH:mm:ss)").type(STRING),
					fieldWithPath("result.content[].orderId").description("주문 아이디").type(NUMBER),
					fieldWithPath("result.content[].orderNumber").description("주문 번호").type(STRING),
					fieldWithPath("result.content[].totalPrice").description("총 주문 금액").type(NUMBER),
					fieldWithPath("result.content[].orderStatus").description("주문 상태").type(STRING),

					fieldWithPath("result.content[].items[]").description("주문에 포함된 상품 목록").type(ARRAY),
					fieldWithPath("result.content[].items[].itemName").description("상품 이름").type(STRING),
					fieldWithPath("result.content[].items[].quantity").description("주문 수량").type(NUMBER),
					fieldWithPath("result.content[].items[].imageUrl").description("상품 이미지 URL").type(STRING),
					fieldWithPath("result.content[].items[].totalPrice").description("해당 상품의 총 주문 금액").type(NUMBER),

					fieldWithPath("result.pageNumber").description("현재 페이지 번호").type(NUMBER),
					fieldWithPath("result.pageSize").description("페이지 크기").type(NUMBER),
					fieldWithPath("result.totalElements").description("총 요소 개수").type(NUMBER),
					fieldWithPath("result.totalPages").description("총 페이지 개수").type(NUMBER),
					fieldWithPath("result.last").description("마지막 페이지 여부").type(BOOLEAN)
				)
			);
		}

	}

	@Nested
	@DisplayName("주문 취소 및 환불 API")
	class CancelOrderAndRefund {

		@Test
		void 주문_취소와_환불에_성공하면_200을_응답한다() {
			// given
			Order 취소할_주문 = 주문_목록.getFirst();
			Long orderId = 취소할_주문.getId();
			Long memberId = 유저.getId();

			doAnswer(invocation -> {
				Order order = invocation.getArgument(0);
				Payment payment = order.getPayment();
				order.changeStatus(OrderStatus.CANCELLED);
				payment.changeStatus(Payment.PaymentStatus.CANCELLED);
				return null;
			}).when(commandRefundService)
				.refund(eq(취소할_주문), eq(취소할_주문.getPayment().getPaymentKey()), any());

			RequestSpecification 요청 = given(spec)
				.cookie(getCookie(memberId))
				.filter(성공_문서_생성("주문 취소 및 환불"));

			// when
			Response 응답 = 요청
				.delete("/api/orders/{orderId}", orderId);

			// then
			응답.then()
				.log().body()
				.statusCode(200);
		}
	}

	@Test
	void 주문을_취소할_권한이_없으면_상태_코드_403을_응답한다() {
		// given
		Order 취소할_주문 = 주문_목록.getFirst();
		Long 취소할_주문_아이디 = 취소할_주문.getId();
		Long 유저_아이디 = 유저.getId();

		doThrow(new InvalidOrderException(ACCESS_DENIED)).when(orderCancelValidator)
			.validate(취소할_주문, 유저);

		RequestSpecification 요청 = given(spec)
			.cookie(getCookie(유저_아이디))
			.filter(실패_문서_생성("주문 취소 및 환불", "주문 취소 실패", "주문을 취소할 권한이 없는 경우입니다."));

		// when
		Response 응답 = 요청
			.delete("/api/orders/{orderId}", 취소할_주문_아이디);

		// then
		응답.then()
			.log().body()
			.statusCode(403);
	}

	@Test
	void 주문_취소_가능_시간이_지났다면_상태_코드_400을_응답한다() {
		// given
		Order 취소할_주문 = 주문_목록.getFirst();
		Long 취소할_주문_아이디 = 취소할_주문.getId();
		Long 유저_아이디 = 유저.getId();

		doThrow(new InvalidOrderException(EXPIRED_CANCEL_TIME)).when(orderCancelValidator)
			.validate(취소할_주문, 유저);

		RequestSpecification 요청 = given(spec)
			.cookie(getCookie(유저_아이디))
			.filter(실패_문서_생성("주문 취소 및 환불", "주문 취소 실패", "24시간 이내의 주문이 아닌 경우입니다."));

		// when
		Response 응답 = 요청
			.delete("/api/orders/{orderId}", 취소할_주문_아이디);

		// then
		응답.then()
			.log().body()
			.statusCode(400);
	}

	@Test
	void 결제_완료_상태의_주문이_아니라면_상태_코드_400을_응답한다() {
		// given
		Order 취소할_주문 = 주문_목록.getFirst();
		Long 취소할_주문_아이디 = 취소할_주문.getId();
		Long 유저_아이디 = 유저.getId();

		doThrow(new InvalidOrderException(NOT_PAID_ORDER)).when(orderCancelValidator)
			.validate(취소할_주문, 유저);

		RequestSpecification 요청 = given(spec)
			.cookie(getCookie(유저_아이디))
			.filter(실패_문서_생성("주문 취소 및 환불", "주문 취소 실패", "결제 완료 상태(PAID)의 주문이 아닌 경우입니다."));

		// when
		Response 응답 = 요청
			.delete("/api/orders/{orderId}", 취소할_주문_아이디);

		// then
		응답.then()
			.log().body()
			.statusCode(400);
	}

	@Test
	void 환불에_실패하면_상태_코드_500를_응답한다() {
		// given
		Order 취소할_주문 = 주문_목록.getFirst();
		Long orderId = 취소할_주문.getId();
		Long memberId = 유저.getId();
		String paymentKey = 취소할_주문.getPayment().getPaymentKey();

		// refund()에서 예외 발생 → 이후 로직 안 감
		doThrow(new InvalidOrderException(BaseResponseStatus.ORDER_REFUND_FAILED))
			.when(commandRefundService)
			.refund(eq(취소할_주문), eq(paymentKey), any());

		RequestSpecification 요청 = given(spec)
			.cookie(getCookie(memberId))
			.filter(실패_문서_생성("주문 취소 및 환불", "환불 실패", "토스 결제 취소 API에서 오류가 발생한 경우입니다."));

		// when
		Response 응답 = 요청
			.delete("/api/orders/{orderId}", orderId);

		// then
		응답.then()
			.log().body()
			.statusCode(500);
	}

	private RestDocumentationFilter 성공_문서_생성(String name) {
		return document(
			name,
			resourceDetails()
				.summary("주문 취소 및 환불")
				.description("주문을 취소하고 총 주문 금액을 환불합니다."),
			pathParameters(
				parameterWithName("orderId").description("취소할 주문 아이디")
			),
			responseFields(
				fieldWithPath("success").description("요청 성공 여부").type(BOOLEAN),
				fieldWithPath("code").description("커스텀 응답 코드").type(NUMBER),
				fieldWithPath("httpStatus").description("HTTP 상태 코드").type(NUMBER),
				fieldWithPath("message").description("응답 메시지").type(STRING),
				fieldWithPath("result").description("빈 응답 데이터").type(OBJECT)
			)
		);
	}

	private RestDocumentationFilter 실패_문서_생성(String name, String summary, String description) {
		return document(
			name,
			resourceDetails()
				.summary(summary)
				.description(description),
			pathParameters(
				parameterWithName("orderId").description("취소할 주문 아이디")
			),
			responseFields(
				fieldWithPath("success").description("요청 성공 여부").type(BOOLEAN),
				fieldWithPath("code").description("커스텀 응답 코드").type(NUMBER),
				fieldWithPath("httpStatus").description("HTTP 상태 코드").type(NUMBER),
				fieldWithPath("message").description("응답 메시지").type(STRING),
				fieldWithPath("result").description("빈 응답 데이터").type(OBJECT)
			)
		);
	}
}