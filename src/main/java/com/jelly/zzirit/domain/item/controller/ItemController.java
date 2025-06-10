package com.jelly.zzirit.domain.item.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jelly.zzirit.domain.item.dto.request.ItemFilterRequest;
import com.jelly.zzirit.domain.item.dto.response.CurrentTimeDealFetchResponse;
import com.jelly.zzirit.domain.item.dto.response.ItemFetchResponse;
import com.jelly.zzirit.domain.item.dto.response.SimpleItemsFetchResponse;
import com.jelly.zzirit.domain.item.service.CommandTimeDealService;
import com.jelly.zzirit.domain.item.service.QueryItemService;
import com.jelly.zzirit.global.dto.PageResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/items")
public class ItemController {

	private final QueryItemService queryItemService;
	private final CommandTimeDealService timeDealService;

	@GetMapping("/search")
	public SimpleItemsFetchResponse search(
		@RequestParam(name = "types", required = false) String types,
		@RequestParam(name = "brands", required = false) String brands,
		@RequestParam(name = "keyword", required = false) String keyword,
		@RequestParam(name = "sort", defaultValue = "priceAsc") String sort,
		@RequestParam(name = "size", defaultValue = "20") int size,
		@RequestParam(name = "last-price", required = false) Long lastPrice,
		@RequestParam(name = "last-item-id", required = false) Long lastItemId
	) {
		ItemFilterRequest filter = ItemFilterRequest.of(types, brands, keyword);
		return queryItemService.search(filter, sort, size, lastPrice, lastItemId);
	}

	@GetMapping("/{item-id}")
	public ItemFetchResponse getById(@PathVariable(name = "item-id") Long itemId) {
		return queryItemService.getById(itemId);
	}

	@GetMapping("/time-deals/now")
	public PageResponse<CurrentTimeDealFetchResponse> getCurrentTimeDeals(
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "3") int size
	) {
		return timeDealService.getCurrentTimeDeals(page, size);
	}
}
