package com.jelly.zzirit.domain.item.service;

import static com.jelly.zzirit.domain.item.domain.fixture.BrandFixture.*;
import static com.jelly.zzirit.domain.item.domain.fixture.ItemFixture.*;
import static com.jelly.zzirit.domain.item.domain.fixture.TypeBrandFixture.*;
import static com.jelly.zzirit.domain.item.domain.fixture.TypeFixture.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.jelly.zzirit.domain.item.dto.request.ItemFilterRequest;
import com.jelly.zzirit.domain.item.dto.response.ItemFetchQueryResponse;
import com.jelly.zzirit.domain.item.dto.response.ItemFetchResponse;
import com.jelly.zzirit.domain.item.dto.response.SimpleItemsFetchResponse;
import com.jelly.zzirit.domain.item.entity.Brand;
import com.jelly.zzirit.domain.item.entity.Item;
import com.jelly.zzirit.domain.item.entity.ItemStatus;
import com.jelly.zzirit.domain.item.entity.Type;
import com.jelly.zzirit.domain.item.entity.stock.ItemStock;
import com.jelly.zzirit.domain.item.repository.ItemQueryRepository;
import com.jelly.zzirit.domain.item.repository.ItemRepository;
import com.jelly.zzirit.domain.item.repository.stock.ItemStockRepository;
import com.jelly.zzirit.global.dto.PageResponse;

@Disabled
@ExtendWith(MockitoExtension.class)
public class QueryItemServiceTest {

	@Mock
	private ItemRepository itemRepository;

	@Mock
	private ItemStockRepository itemStockRepository;

	@Mock
	private ItemQueryRepository itemQueryRepository;

	@InjectMocks
	private QueryItemService queryItemService;

	@Nested
	@DisplayName("상품 전체 조회 테스트")
	class getItems {
		@Test
		void 검색어와_필터에_맞게_상품을_조회한다() {
			// given
			Type 노트북 = 노트북();
			Type 스마트폰 = 스마트폰();
			Brand 삼성 = 삼성();
			Brand 애플 = 브랜드_생성("애플");
			Item 상품 = 삼성_노트북(타입_브랜드_생성(노트북, 삼성));

			List<ItemFetchQueryResponse> 상품들 = List.of(
				new ItemFetchQueryResponse(
					상품.getId(),
					상품.getName(),
					상품.getTypeBrand().getType().getName(),
					상품.getTypeBrand().getBrand().getName(),
					상품.getImageUrl(),
					상품.getPrice(),
					null,
					ItemStatus.NONE,
					null,
					null
				)
			);

			given(itemQueryRepository.findItems(
				ItemFilterRequest.of(
					"노트북",
					"",
					"노트북"
				),
				"priceAsc",
				null,
				null,
				20
			)).willReturn(상품들);

			given(itemQueryRepository.findItemsCount(
				ItemFilterRequest.of(
					"노트북",
					"",
					"노트북"
				)
			)).willReturn(1L);

			// when
			SimpleItemsFetchResponse 응답 = queryItemService.search(
				ItemFilterRequest.of(
					"노트북",
					"",
					"노트북"
				),
				"priceAsc",
				20,
				null,
				null
			);

			// then
			assertThat(응답.totalCount()).isEqualTo(1);
		}

		@Test
		void 필터에_맞게_상품을_조회한다() {
			// given
			Type 노트북 = 노트북();
			Type 스마트폰 = 스마트폰();
			Brand 삼성 = 삼성();
			Brand 애플 = 브랜드_생성("애플");

			Item 상품 = 삼성_노트북(타입_브랜드_생성(노트북, 삼성));

			List<ItemFetchQueryResponse> 상품들 = List.of(
				new ItemFetchQueryResponse(
					상품.getId(),
					상품.getName(),
					상품.getTypeBrand().getType().getName(),
					상품.getTypeBrand().getBrand().getName(),
					상품.getImageUrl(),
					상품.getPrice(),
					null,
					ItemStatus.NONE,
					null,
					null
				)
			);

			given(itemQueryRepository.findItems(
				ItemFilterRequest.of(
					"노트북",
					"삼성",
					null
				),
				"priceAsc",
				null,
				null,
				20
			)).willReturn(상품들);

			given(itemQueryRepository.findItemsCount(
				ItemFilterRequest.of(
					"노트북",
					"삼성",
					null
				)
			)).willReturn(1L);

			// when
			SimpleItemsFetchResponse  응답 = queryItemService.search(
				ItemFilterRequest.of(
					"노트북,스마트폰",
					"삼성,애플",
					null
				),
				"priceAsc",
				20,
				null,
				null
			);

			// then
			assertThat(응답.totalCount()).isEqualTo(2);
		}
	}

	@Test
	void 상품을_상세_조회한다() {
		// given
		Type 노트북 = 노트북();
		Brand 삼성 = 삼성();
		Item 상품 = 삼성_노트북(타입_브랜드_생성(노트북, 삼성));
		ItemStock 재고 = new ItemStock(상품, null, 10, 2);
		given(itemRepository.getById(상품.getId())).willReturn(상품);
		given(itemStockRepository.findByItemId(상품.getId())).willReturn(Optional.of(재고));

		// when
		ItemFetchResponse 응답 = queryItemService.getById(상품.getId());

		// then
		assertThat(응답.itemId()).isEqualTo(상품.getId());
	}
}
