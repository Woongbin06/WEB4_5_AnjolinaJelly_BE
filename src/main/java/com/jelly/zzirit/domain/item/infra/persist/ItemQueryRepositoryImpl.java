package com.jelly.zzirit.domain.item.infra.persist;

import static com.jelly.zzirit.domain.item.entity.QBrand.*;
import static com.jelly.zzirit.domain.item.entity.QItem.*;
import static com.jelly.zzirit.domain.item.entity.QType.*;
import static com.jelly.zzirit.domain.item.entity.QTypeBrand.*;
import static com.jelly.zzirit.domain.item.entity.timedeal.QTimeDeal.*;
import static com.jelly.zzirit.domain.item.entity.timedeal.QTimeDealItem.*;
import static com.jelly.zzirit.domain.item.entity.timedeal.TimeDeal.TimeDealStatus.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.jelly.zzirit.domain.admin.dto.response.AdminItemFetchResponse;
import com.jelly.zzirit.domain.item.dto.request.ItemFilterRequest;
import com.jelly.zzirit.domain.item.dto.response.ItemFetchQueryResponse;
import com.jelly.zzirit.domain.item.dto.response.QItemFetchQueryResponse;
import com.jelly.zzirit.domain.item.entity.Item;
import com.jelly.zzirit.domain.item.entity.QBrand;
import com.jelly.zzirit.domain.item.entity.QItem;
import com.jelly.zzirit.domain.item.entity.QType;
import com.jelly.zzirit.domain.item.entity.QTypeBrand;
import com.jelly.zzirit.domain.item.entity.stock.QItemStock;
import com.jelly.zzirit.domain.item.entity.timedeal.QTimeDeal;
import com.jelly.zzirit.domain.item.entity.timedeal.QTimeDealItem;
import com.jelly.zzirit.domain.item.entity.timedeal.TimeDeal;
import com.jelly.zzirit.domain.item.repository.ItemQueryRepository;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemQueryRepositoryImpl implements ItemQueryRepository {

	private final JPAQueryFactory queryFactory;

	@Override
	public List<ItemFetchQueryResponse> findItems(
		ItemFilterRequest filter,
		String sort,
		Long lastItemId,
		Long lastPrice,
		int size
	) {
		return queryFactory.select(
			new QItemFetchQueryResponse(
				item.id,
				item.name,
				type.name,
				brand.name,
				item.imageUrl,
				item.price,
				timeDealItem.price,
				item.itemStatus,
				timeDeal.discountRatio,
				timeDeal.endTime
			))
			.from(item)
			.leftJoin(timeDealItem)
				.on(timeDealItem.item.eq(item)
					.and(isOngoingTimeDeal()))
			.join(item.typeBrand, typeBrand)
			.join(typeBrand.type, type)
			.join(typeBrand.brand, brand)
			.where(
				cursorCondition(sort, lastPrice, lastItemId),
				isKeywordContain(filter.keyword()),
				isTypeContain(filter.types()),
				isBrandContain(filter.brands())
			)
			.limit(size)
			.orderBy(sortByPrice(sort))
			.fetch();
	}

	private BooleanExpression isOngoingTimeDeal() {
		return timeDealItem.timeDeal.status.eq(ONGOING);
	}

	@Override
	public Optional<Item> findItemWithTypeJoin(Long itemId) {
		return Optional.ofNullable(
			queryFactory.selectFrom(item)
				.join(item.typeBrand, typeBrand).fetchJoin()
				.join(typeBrand.type, type).fetchJoin()
				.join(typeBrand.brand, brand).fetchJoin()
				.where(item.id.eq(itemId))
				.fetchOne()
		);
	}

	private BooleanExpression cursorCondition(String sort, Long lastPrice, Long lastItemId) {
		if(lastPrice == null || lastItemId == null) {
			return null;
		}

		BigDecimal price = BigDecimal.valueOf(lastPrice);

		if (sort.equals("priceDesc")) {
			return item.price.lt(lastPrice)
				.or(item.price.eq(price).and(item.id.lt(lastItemId)));
		}
		return item.price.gt(lastPrice)
			.or(item.price.eq(price).and(item.id.lt(lastItemId)));
	}

	private OrderSpecifier<?> sortByPrice(String sort) {
		if (sort.equals("priceDesc")) {
			return item.price.desc();
		}
		return item.price.asc();
	}

	private BooleanExpression isKeywordContain(String keyword) {
		if (keyword == null) {
			return null;
		}
		return item.name.containsIgnoreCase(keyword)
			.or(brand.name.containsIgnoreCase(keyword))
			.or(type.name.containsIgnoreCase(keyword));
	}

	private BooleanExpression isTypeContain(List<String> types) {
		if (types == null || types.isEmpty()) {
			return null;
		}
		return type.name.in(types);
	}

	private BooleanExpression isBrandContain(List<String> brands) {
		if (brands == null || brands.isEmpty()) {
			return null;
		}
		return brand.name.in(brands);
	}

	@Override
	public Optional<AdminItemFetchResponse> findAdminItemById(Long itemId) {
		QItem i = QItem.item;
		QItemStock s = QItemStock.itemStock;
		QTypeBrand tb = QTypeBrand.typeBrand;
		QType t = QType.type;
		QBrand b = QBrand.brand;

		AdminItemFetchResponse dto = queryFactory
				.select(Projections.constructor(AdminItemFetchResponse.class,
						i.id,
						i.name,
						i.imageUrl,
						t.name,
						b.name,
						i.price,
						s.quantity
				))
				.from(i)
				.join(i.typeBrand, tb)
				.join(tb.type, t)
				.join(tb.brand, b)
				.join(s).on(s.item.eq(i))
				.where(i.id.eq(itemId))
				.fetchOne(); // 단건 조회

		return Optional.ofNullable(dto);
	}

	@Override
	public Page<AdminItemFetchResponse> findAdminItems(String name, String sort, Pageable pageable) {
		QItem i = QItem.item;
		QItemStock s = QItemStock.itemStock;
		QTypeBrand tb = QTypeBrand.typeBrand;
		QType t = QType.type;
		QBrand b = QBrand.brand;

		OrderSpecifier<?> orderSpecifier = sort.equalsIgnoreCase("asc")
				? i.createdAt.asc()
				: i.createdAt.desc();

		List<AdminItemFetchResponse> content = queryFactory
				.select(Projections.constructor(AdminItemFetchResponse.class,
						i.id,
						i.name,
						i.imageUrl,
						t.name,
						b.name,
						i.price,
						s.quantity
				))
				.from(i)
				.join(i.typeBrand, tb)
				.join(tb.type, t)
				.join(tb.brand, b)
				.join(s).on(s.item.eq(i))
				.where(containsName(name))
				.orderBy(orderSpecifier)
				.offset(pageable.getOffset())
				.limit(pageable.getPageSize())
				.fetch();

		Long count = queryFactory
				.select(i.count())
				.from(i)
				.join(i.typeBrand, tb)
				.join(tb.type, t)
				.join(tb.brand, b)
				.join(s).on(s.item.eq(i))
				.where(containsName(name))
				.fetchOne();

		return PageableExecutionUtils.getPage(content, pageable, () -> count == null ? 0 : count);
	}

	private BooleanExpression containsName(String name) {
		return (name == null || name.isBlank()) ? null : QItem.item.name.lower().contains(name.toLowerCase());
	}
}
