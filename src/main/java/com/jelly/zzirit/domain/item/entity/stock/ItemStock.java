package com.jelly.zzirit.domain.item.entity.stock;

import com.jelly.zzirit.domain.admin.dto.request.ItemCreateRequest;
import com.jelly.zzirit.domain.admin.dto.request.ItemUpdateRequest;
import com.jelly.zzirit.domain.item.entity.Item;
import com.jelly.zzirit.domain.item.entity.timedeal.TimeDealItem;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.dto.Empty;
import com.jelly.zzirit.global.entity.BaseEntity;
import com.jelly.zzirit.global.exception.custom.InvalidItemException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ItemStock extends BaseEntity {

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "item_id", unique = true)
	private Item item;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "timedeal_item_id", unique = true)
	private TimeDealItem timeDealItem;

	// 단일 quantity 는 "누가 선점했는지", "확정인지", 구분이 안 돼서 정합성 흐트러짐
	@Column(name = "quantity", nullable = false)
	private int quantity; // 최초 설정된 총 재고

	@Column(name = "sold_quantity")
	private int soldQuantity; // 결제 완료되어 확정된 수량

	@Version
	private Integer version;

	public ItemStock(Item item, TimeDealItem timeDealItem, int quantity, int soldQuantity) {
		this.item = item;
		this.timeDealItem = timeDealItem;
		this.quantity = quantity;
		this.soldQuantity = soldQuantity;
	}

	public void update(ItemCreateRequest request, Item item) {
		this.item = item;

		// 팔린 만큼 재고 채워넣어야 함(임시 비즈니스 로직)
		if (request.stockQuantity() < this.soldQuantity) {
			throw new InvalidItemException(BaseResponseStatus.OUT_OF_STOCK);
		}

		this.quantity = request.stockQuantity(); // todo: 최초 설정된 재고만 업데이트 가능?
	}

	public void changeQuantity(int newQuantity) {
		if (newQuantity < 0) {
			throw new InvalidItemException(BaseResponseStatus.INVALID_STOCK);
		}
		if (this.soldQuantity > newQuantity) {
			throw new InvalidItemException(BaseResponseStatus.OUT_OF_STOCK);
		}
		this.quantity = newQuantity;
	}

	@PrePersist
	@PreUpdate
	private void validateExclusiveAssociation() {
		boolean hasItem = this.item != null;
		boolean hasTimeDealItem = this.timeDealItem != null;

		if (hasItem == hasTimeDealItem) {
			throw new IllegalStateException("ItemStock은 item과 timeDealItem 중 하나만 참조해야 합니다. 현재 상태: item="
				+ (item != null) + ", timeDealItem=" + (timeDealItem != null));
		}
	}
}