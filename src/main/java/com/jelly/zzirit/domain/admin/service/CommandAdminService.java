package com.jelly.zzirit.domain.admin.service;

import com.jelly.zzirit.domain.admin.dto.request.ItemCreateRequest;
import com.jelly.zzirit.domain.admin.dto.request.ItemUpdateRequest;
import com.jelly.zzirit.domain.item.entity.Item;
import com.jelly.zzirit.domain.item.entity.TypeBrand;
import com.jelly.zzirit.domain.item.entity.stock.ItemStock;
import com.jelly.zzirit.domain.item.repository.ItemRepository;
import com.jelly.zzirit.domain.item.repository.TypeBrandRepository;
import com.jelly.zzirit.domain.item.repository.stock.ItemStockRepository;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidItemException;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CommandAdminService {
	private final ItemRepository itemRepository;
	private final ItemStockRepository itemStockRepository;
	private final TypeBrandRepository typeBrandRepository;
	private final CommandS3Service commandS3Service;

	@CacheEvict(cacheNames = "items", keyGenerator = "customKeyGenerator")
	public void createItem(ItemCreateRequest request) {
		if (request.imageUrl() == null || request.imageUrl().isBlank()) {
			throw new InvalidItemException(BaseResponseStatus.IMAGE_REQUIRED);
		}

		TypeBrand typeBrand = typeBrandRepository.findByTypeIdAndBrandId(request.typeId(), request.brandId())
			.orElseThrow(() -> new InvalidItemException(BaseResponseStatus.TYPE_BRAND_NOT_FOUND));

		// 상품생성요청 dto -> 상품 객체 생성, 저장
		Item item = request.toItemEntity(typeBrand);
		itemRepository.save(item); // save 후 item 객체에 id 채워짐

		// 상품생성요청 dto -> 재고 객체 생성, 저장
		ItemStock itemStock = request.toItemStockEntity(item);
		itemStockRepository.save(itemStock);
	}

	@CacheEvict(cacheNames = "items", key = "customKeyGenerator")
	public void updateItem(@NotNull Long itemId, ItemUpdateRequest request) {
		Item item = itemRepository.findById(itemId)
			.orElseThrow(() -> new InvalidItemException(BaseResponseStatus.ITEM_NOT_FOUND));

		ItemStock itemStock = itemStockRepository.findByItem(item)
			.orElseThrow(() -> new InvalidItemException(BaseResponseStatus.ITEM_STOCK_NOT_FOUND));


		item.update(request);
		itemStock.changeQuantity(request.stockQuantity());
	}

	@CacheEvict(cacheNames = "items", key = "customKeyGenerator")
	public void deleteItem(@NotNull Long itemId) {
		// todo: 삭제 검증 로직 논의 필요 - 이미 판매된 상품은 삭제 불가 등
		// todo: soft delete 사용할지 논의 필요

		Item item = itemRepository.findById(itemId)
			.orElseThrow(() -> new InvalidItemException(BaseResponseStatus.ITEM_NOT_FOUND));

		// S3 이미지 삭제 처리
		String imageUrl = item.getImageUrl();
		if (imageUrl != null && !imageUrl.isBlank()) {
			try {
				commandS3Service.delete(imageUrl);
			} catch (Exception e) {
				log.warn("S3 이미지 삭제 실패: itemId={}, url={}", itemId, imageUrl, e);
			}
		} else {
			log.info("삭제할 이미지 없음: itemId={}", itemId);
		}

		// 재고 삭제
		ItemStock itemStock = itemStockRepository.findByItemId(itemId)
			.orElseThrow(() -> new InvalidItemException(BaseResponseStatus.ITEM_STOCK_NOT_FOUND));
		itemStockRepository.delete(itemStock);

		// 상품 삭제
		itemRepository.delete(item);
	}
}