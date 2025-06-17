package com.jelly.zzirit.domain.order.service.order;

import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.jelly.zzirit.domain.item.entity.Item;
import com.jelly.zzirit.domain.item.entity.timedeal.TimeDeal;
import com.jelly.zzirit.domain.item.repository.ItemRepository;
import com.jelly.zzirit.domain.item.repository.TimeDealItemRepository;
import com.jelly.zzirit.domain.item.repository.stock.ItemStockRepository;
import com.jelly.zzirit.domain.order.dto.StockChangeEvent;
import com.jelly.zzirit.domain.order.util.AsyncStockHistoryUploader;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidOrderException;
import com.jelly.zzirit.global.redis.lock.DistributedLock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommandStockService {

	private final ItemRepository itemRepository;
	private final TimeDealItemRepository timeDealItemRepository;
	private final ItemStockRepository itemStockRepository;
	private final AsyncStockHistoryUploader asyncStockHistoryUploader;

	@Transactional
	@DistributedLock(key = "#itemId", leaseTime = 12L)
	@Retryable(
		retryFor = {ObjectOptimisticLockingFailureException.class},
		maxAttempts = 100,
		backoff = @Backoff(100)
	)
	public void decrease(String orderNumber, Long itemId, int quantity) {
		Item item = findItemWithOptimisticLock(itemId);

		boolean success;
		if (item.validateTimeDeal()) {
			success = timeDealItemRepository.findByItemId(itemId)
					.filter(tdi -> tdi.getTimeDeal().getStatus() == TimeDeal.TimeDealStatus.ONGOING)
					.map(tdi -> itemStockRepository.decreaseTimeDealStockIfEnough(tdi.getId(), quantity))
					.orElseGet(() -> itemStockRepository.decreaseStockIfEnough(itemId, quantity));
		} else {
			success = itemStockRepository.decreaseStockIfEnough(itemId, quantity);
		}

		if (!success) {
			throw new InvalidOrderException(BaseResponseStatus.STOCK_REDUCE_FAILED);
		}

		registerLogAfterCommit(StockChangeEvent.decrease(itemId, orderNumber, quantity));
	}

	@DistributedLock(key = "#itemId", leaseTime = 12L)
	@Transactional
	@Retryable(
		retryFor = {ObjectOptimisticLockingFailureException.class},
		maxAttempts = 100,
		backoff = @Backoff(100)
	)
	public void restore(String orderNumber, Long itemId, int quantity) {
		Item item = findItemWithOptimisticLock(itemId);
		boolean success;

		if (item.validateTimeDeal()) {
			success = timeDealItemRepository.findByItemId(itemId)
				.filter(tdi -> {
					TimeDeal.TimeDealStatus status = tdi.getTimeDeal().getStatus();
					return status == TimeDeal.TimeDealStatus.ONGOING || status == TimeDeal.TimeDealStatus.ENDED;
				})
				.map(tdi -> itemStockRepository.restoreTimeDealStockIfPossible(tdi.getId(), quantity))
				.orElseGet(() -> itemStockRepository.restoreStockIfPossible(itemId, quantity));
		} else {
			success = itemStockRepository.restoreStockIfPossible(itemId, quantity);
		}

		if (!success) {
			throw new InvalidOrderException(BaseResponseStatus.STOCK_RESTORE_FAILED);
		}

		registerLogAfterCommit(StockChangeEvent.restore(itemId, orderNumber, quantity));
	}

	private Item findItemWithOptimisticLock(Long itemId) {
		return itemRepository.findByIdWithOptimisticLock(itemId)
			.orElseThrow(() -> new InvalidOrderException(BaseResponseStatus.ITEM_NOT_FOUND));
	}

	private void registerLogAfterCommit(StockChangeEvent event) {
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				asyncStockHistoryUploader.upload(event);
			}
		});
	}
}