package com.jelly.zzirit.domain.item.repository.stock;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jelly.zzirit.domain.item.entity.Item;
import com.jelly.zzirit.domain.item.entity.stock.ItemStock;
import com.jelly.zzirit.domain.item.entity.timedeal.TimeDealItem;

@Repository
public interface ItemStockRepository extends JpaRepository<ItemStock, Long>, ItemStockRepositoryCustom {
	// 상품 id 목록을 받아서 재고개수 목록 조회
	List<ItemStock> findAllByItemIdIn(List<Long> itemIds);

	Optional<ItemStock> findByItemId(Long itemId);

	Optional<ItemStock> findByItem(Item item);

	Optional<ItemStock> findByTimeDealItem(TimeDealItem tdi);

	@Query("SELECT s FROM ItemStock s WHERE s.timeDealItem.id = :timeDealItemId")
	Optional<ItemStock> findByTimeDealItemId(@Param("timeDealItemId") Long timeDealItemId);
}