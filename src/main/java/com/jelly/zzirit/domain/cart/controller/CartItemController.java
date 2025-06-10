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

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/cart/items")
@RequiredArgsConstructor
public class CartItemController {

	private final CartItemService cartItemService;

	@PostMapping
	public CartItemFetchResponse addItemToCart(@Valid @RequestBody CartItemCreateRequest request) {
		return cartItemService.addItemToCart(AuthMember.getMemberId(), request);
	}

	@DeleteMapping("/{item-id}")
	public void removeItemToCart(@PathVariable("item-id") Long itemId) {
		cartItemService.removeItemToCart(AuthMember.getMemberId(), itemId);
	}

	@DeleteMapping
	public void removeSelectedItems(@RequestBody CartItemDeleteRequest request) {
		cartItemService.removeItemsFromCart(AuthMember.getMemberId(), request.itemIds());
	}

	@DeleteMapping("/all")
	public void removeAllItems() {
		cartItemService.removeAllItemsFromCart(AuthMember.getMemberId());
	}

	@PostMapping("/{item-id}/increase")
	public CartItemFetchResponse increaseQuantity(@PathVariable("item-id") Long itemId) {
		return cartItemService.modifyQuantity(AuthMember.getMemberId(), itemId, +1);
	}

	@PostMapping("/{item-id}/decrease")
	public CartItemFetchResponse decreaseQuantity(@PathVariable("item-id") Long itemId) {
		return cartItemService.modifyQuantity(AuthMember.getMemberId(), itemId, -1);
	}
}