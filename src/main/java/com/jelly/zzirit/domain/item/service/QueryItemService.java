package com.jelly.zzirit.domain.item.service;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jelly.zzirit.domain.item.dto.request.ItemFilterRequest;
import com.jelly.zzirit.domain.item.dto.response.ItemFetchQueryResponse;
import com.jelly.zzirit.domain.item.dto.response.ItemFetchResponse;
import com.jelly.zzirit.domain.item.dto.response.SimpleItemFetchResponse;
import com.jelly.zzirit.domain.item.dto.response.SimpleItemsFetchResponse;
import com.jelly.zzirit.domain.item.entity.Item;
import com.jelly.zzirit.domain.item.entity.stock.ItemStock;
import com.jelly.zzirit.domain.item.entity.timedeal.TimeDealItem;
import com.jelly.zzirit.domain.item.repository.ItemQueryRepository;
import com.jelly.zzirit.domain.item.repository.ItemRepository;
import com.jelly.zzirit.domain.item.repository.stock.ItemStockRepository;
import com.jelly.zzirit.domain.item.repository.TimeDealItemRepository;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.dto.PageResponse;
import com.jelly.zzirit.global.exception.custom.InvalidItemException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QueryItemService {

	private final ItemRepository itemRepository;
	private final ItemQueryRepository itemQueryRepository;
	private final ItemStockRepository itemStockRepository;
	private final TimeDealItemRepository timeDealItemRepository;

	public ItemFetchResponse getById(Long itemId) {
		Item item = itemRepository.getById(itemId);

		if(item.validateTimeDeal()) {
			TimeDealItem timeDealItem = timeDealItemRepository.findActiveByItemId(item.getId())
				.orElseThrow(() -> new InvalidItemException(BaseResponseStatus.ITEM_NOT_FOUND));
			ItemStock itemStock = itemStockRepository.findByTimeDealItem(timeDealItem)
				.orElseThrow(() -> new InvalidItemException(BaseResponseStatus.ITEM_NOT_FOUND));

			return ItemFetchResponse.from(timeDealItem, itemStock.getQuantity());
		}

		ItemStock itemStock = itemStockRepository.findByItemId(item.getId())
			.orElseThrow(() -> new InvalidItemException(BaseResponseStatus.ITEM_NOT_FOUND));
		return ItemFetchResponse.from(item, itemStock.getQuantity());
	}

	public SimpleItemsFetchResponse search(
		ItemFilterRequest filter,
		String sort,
		int size,
		Long lastPrice,
		Long lastItemId
	) {
		List<ItemFetchQueryResponse> items = itemQueryRepository.findItems(filter, sort, lastItemId, lastPrice, size);
		Long totalCount = itemQueryRepository.findItemsCount(filter);
		return SimpleItemsFetchResponse.from(totalCount, items);
	}
}
