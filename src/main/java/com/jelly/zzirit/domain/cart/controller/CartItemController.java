package com.jelly.zzirit.domain.cart.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jelly.zzirit.domain.cart.dto.request.CartItemCreateRequest;
import com.jelly.zzirit.domain.cart.dto.request.CartItemDeleteRequest;
import com.jelly.zzirit.domain.cart.dto.response.CartItemFetchResponse;
import com.jelly.zzirit.domain.cart.service.CartItemService;
import com.jelly.zzirit.global.AuthMember;
import com.jelly.zzirit.global.dto.BaseResponse;
import com.jelly.zzirit.global.dto.Empty;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/cart/items")
@RequiredArgsConstructor
@Tag(name = "장바구니 API", description = "장바구니와 관련된 API를 설명합니다.")
public class CartItemController {

	private final CartItemService cartItemService;

	@Operation(summary = "장바구니에 상품 추가", description = "상품 ID와 수량을 전달받아 장바구니에 항목을 추가합니다.")
	@PostMapping
	public CartItemFetchResponse addItemToCart(@Valid @RequestBody CartItemCreateRequest request) {
		return cartItemService.addItemToCart(AuthMember.getMemberId(), request);
	}

	@Operation(summary = "장바구니 항목 삭제", description = "장바구니에서 항목을 제거합니다.")
	@DeleteMapping("/{item-id}")
	public void removeItemToCart(@PathVariable("item-id") Long itemId) {
		cartItemService.removeItemToCart(AuthMember.getMemberId(), itemId);
	}

	@Operation(summary = "장바구니 선택 항목 삭제", description = "장바구니에서 선택한 항목들을 제거합니다.")
	@DeleteMapping
	public void removeSelectedItems(@RequestBody CartItemDeleteRequest request) {
		cartItemService.removeItemsFromCart(AuthMember.getMemberId(), request.itemIds());
	}

	@Operation(summary = "장바구니 전체 삭제", description = "장바구니의 모든 항목을 제거합니다.")
	@DeleteMapping("/all")
	public void removeAllItems() {
		cartItemService.removeAllItemsFromCart(AuthMember.getMemberId());
	}

	@Operation(summary = "장바구니 상품 수량 증가", description = "수량 1 증가")
	@PostMapping("/{item-id}/increase")
	public CartItemFetchResponse increaseQuantity(@PathVariable("item-id") Long itemId) {
		return cartItemService.modifyQuantity(AuthMember.getMemberId(), itemId, +1);
	}

	@Operation(summary = "장바구니 상품 수량 감소", description = "수량 1 감소")
	@PostMapping("/{item-id}/decrease")
	public CartItemFetchResponse decreaseQuantity(@PathVariable("item-id") Long itemId) {
		return cartItemService.modifyQuantity(AuthMember.getMemberId(), itemId, -1);
	}
}