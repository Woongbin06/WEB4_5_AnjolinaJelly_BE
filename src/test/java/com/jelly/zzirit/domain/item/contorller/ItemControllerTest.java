package com.jelly.zzirit.domain.item.contorller;

import static com.epages.restdocs.apispec.RestAssuredRestDocumentationWrapper.*;
import static com.jelly.zzirit.domain.item.domain.fixture.BrandFixture.*;
import static com.jelly.zzirit.domain.item.domain.fixture.ItemFixture.*;
import static com.jelly.zzirit.domain.item.domain.fixture.ItemStockFixture.*;
import static com.jelly.zzirit.domain.item.domain.fixture.TypeBrandFixture.*;
import static com.jelly.zzirit.domain.item.domain.fixture.TypeFixture.*;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.restdocs.restassured.RestDocumentationFilter;

import com.jelly.zzirit.domain.item.dto.request.TimeDealCreateRequest;
import com.jelly.zzirit.domain.item.entity.Brand;
import com.jelly.zzirit.domain.item.entity.Item;
import com.jelly.zzirit.domain.item.entity.Type;
import com.jelly.zzirit.domain.item.repository.BrandRepository;
import com.jelly.zzirit.domain.item.repository.ItemRepository;
import com.jelly.zzirit.domain.item.repository.TypeBrandRepository;
import com.jelly.zzirit.domain.item.repository.TypeRepository;
import com.jelly.zzirit.domain.item.repository.stock.ItemStockRepository;
import com.jelly.zzirit.global.support.AcceptanceTest;
import com.jelly.zzirit.testutil.TimeDealTestHelper;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class ItemControllerTest extends AcceptanceTest {

	@Autowired
	private ItemRepository itemRepository;

	@Autowired
	private ItemStockRepository itemStockRepository;

	@Autowired
	private BrandRepository brandRepository;

	@Autowired
	private TypeRepository typeRepository;

	@Autowired
	private TypeBrandRepository typeBrandRepository;

	@Autowired
	private TimeDealTestHelper timeDealTestHelper;

	@Nested
	@DisplayName("상품 전체 조회 API")
	class GetItems {

		@Test
		void 필터를_지정하지_않고_요청하면_200() {
			// given
			Type 노트북 = typeRepository.save(노트북());
			Brand 삼성 = brandRepository.save(삼성());
			Item 상품 = itemRepository.save(삼성_노트북(
				typeBrandRepository.save(타입_브랜드_생성(노트북, 삼성))
			));
			itemStockRepository.save(풀재고_상품(상품));

			RequestSpecification 요청_준비 = given(spec)
				.cookie(getCookie())
				.filter(성공_API_문서_생성("필터링, 검색 없이 조회"));

			// when
			Response 응답 = 요청_준비.when()
				.get("/api/items/search");

			// then
			응답.then()
				.statusCode(200);
		}

		@Test
		void 필터와_검색어를_지정하고_요청하면_200() {
			// given
			Type 노트북 = typeRepository.save(노트북());
			Brand 삼성 = brandRepository.save(삼성());
			Item 상품 = itemRepository.save(삼성_노트북(
				typeBrandRepository.save(타입_브랜드_생성(노트북, 삼성))
			));
			itemStockRepository.save(풀재고_상품(상품));

			RequestSpecification 요청_준비 = given(spec)
				.cookie(getCookie())
				.queryParam("types", List.of("노트북"))
				.queryParam("brands", List.of("삼성"))
				.queryParam("keyword", "노트북")
				.queryParam("sort", "priceDesc")
				.queryParam("page", 0)
				.queryParam("size", 20)
				.filter(성공_API_문서_생성("필터링, 검색을 지정하고 조회"));

			// when
			Response 응답 = 요청_준비.when()
				.get("/api/items/search");

			// then
			응답.then()
				.statusCode(200);
		}

		private RestDocumentationFilter 성공_API_문서_생성(String name) {
			return document(
				name,
				resourceDetails()
					.summary("상품 전체 조회하기")
					.description("상품 전체를 조회합니다."),
				queryParameters(
					parameterWithName("types").description("상품 종류(optional)").optional(),
					parameterWithName("brands").description("브랜드(optional)").optional(),
					parameterWithName("keyword").description("검색어(optional)").optional(),
					parameterWithName("sort").description("정렬 기준(optional) priceAsc(default) | priceDesc").optional(),
					parameterWithName("page").description("페이지 번호").optional(),
					parameterWithName("size").description("페이징 사이즈").optional()
				),
				responseFields(
					fieldWithPath("success").description("요청 성공 여부").type(BOOLEAN),
					fieldWithPath("code").description("응답 코드").type(NUMBER),
					fieldWithPath("httpStatus").description("HTTP 상태 코드").type(NUMBER),
					fieldWithPath("message").description("응답 메시지").type(STRING),
					fieldWithPath("result.items[].itemId").description("상품 ID").type(NUMBER),
					fieldWithPath("result.items[].name").description("상품 이름").type(STRING),
					fieldWithPath("result.items[].type").description("상품 종류").type(STRING),
					fieldWithPath("result.items[].brand").description("브랜드 이름").type(STRING),
					fieldWithPath("result.items[].imageUrl").description("상품 이미지 URL").type(STRING),
					fieldWithPath("result.items[].originalPrice").description("원래 가격").type(NUMBER),
					fieldWithPath("result.items[].discountedPrice").description("할인된 가격 (타임딜 상품일 경우)").type(NUMBER).optional(),
					fieldWithPath("result.items[].itemStatus").description("상품 상태 (NORMAL | TIME_DEAL)").type(STRING),
					fieldWithPath("result.items[].discountRatio").description("할인율 (타임딜 상품일 경우)").type(NUMBER).optional(),
					fieldWithPath("result.items[].endTimeDeal").description("타임딜 종료 시각 (타임딜 상품일 경우)").type(STRING).optional()
				)
			);
		}
	}

	@Nested
	@DisplayName("상품 상세 조회 API")
	class getItem {

		@Test
		void 상품을_상세_조회하면_200() {
			// when
			Type 노트북 = typeRepository.save(노트북());
			Brand 삼성 = brandRepository.save(삼성());
			Item 상품 = itemRepository.save(삼성_노트북(
				typeBrandRepository.save(타입_브랜드_생성(노트북, 삼성))
			));
			itemStockRepository.save(풀재고_상품(상품));

			RequestSpecification 요청_준비 = given(spec)
				.cookie(getCookie())
				.filter(상세_조회_성공_API_문서_생성());

			// when
			Response 응답 = 요청_준비.when()
				.get("/api/items/{item-id}", 상품.getId());

			// then
			응답.then()
				.statusCode(200);
		}

		private RestDocumentationFilter 상세_조회_성공_API_문서_생성() {
			return document(
				"상품 상세 조회 API",
				resourceDetails()
					.summary("상품 상세 조회하기")
					.description("상품 상세를 조회합니다."),
				responseFields(
					fieldWithPath("success").description("요청 성공 여부").type(BOOLEAN),
					fieldWithPath("code").description("응답 코드").type(NUMBER),
					fieldWithPath("httpStatus").description("HTTP 상태 코드").type(NUMBER),
					fieldWithPath("message").description("응답 메시지").type(STRING),
					fieldWithPath("result.itemId").description("상품 ID").type(NUMBER),
					fieldWithPath("result.name").description("상품 이름").type(STRING),
					fieldWithPath("result.type").description("상품 종류").type(STRING),
					fieldWithPath("result.brand").description("브랜드 이름").type(STRING),
					fieldWithPath("result.quantity").description("상품 재고").type(NUMBER),
					fieldWithPath("result.imageUrl").description("상품 이미지 URL").type(STRING),
					fieldWithPath("result.originalPrice").description("원래 가격").type(NUMBER),
					fieldWithPath("result.discountedPrice").description("할인된 가격 (타임딜 상품일 경우)").type(NUMBER).optional(),
					fieldWithPath("result.itemStatus").description("상품 상태 (NORMAL | TIME_DEAL)").type(STRING),
					fieldWithPath("result.discountRatio").description("할인율 (타임딜 상품일 경우)").type(NUMBER).optional(),
					fieldWithPath("result.endTimeDeal").description("타임딜 종료 시각 (타임딜 상품일 경우)").type(STRING).optional()
				)
			);
		}
	}

	// @Nested
	// @DisplayName("진행 중인 타임딜 조회 API")
	// class GetCurrentTimeDeal {
	// 	@Test
	// 	void 현재_진행중인_타임딜_조회_성공() {
	// 		TimeDealCreateRequest request = new TimeDealCreateRequest(
	// 			"테스트 타임딜",
	// 			LocalDateTime.now().plusHours(1),
	// 			LocalDateTime.now().plusHours(2),
	// 			20,
	// 			List.of(
	// 				new TimeDealCreateRequest.TimeDealCreateItemDetail(1L, 3),
	// 				new TimeDealCreateRequest.TimeDealCreateItemDetail(2L, 3)
	// 			)
	// 		);
	//
	// 		timeDealTestHelper.createOngoingTimeDeal(request);
	//
	// 		given(spec)
	// 			.cookie(getCookie())
	// 			.when()
	// 			.get("/api/time-deals/now")
	// 			.then()
	// 			.statusCode(200)
	// 			.body("result.timeDealId", notNullValue())
	// 			.body("result.timeDealName", notNullValue())
	// 			.body("result.items", notNullValue());
	// 	}
	// }

}
