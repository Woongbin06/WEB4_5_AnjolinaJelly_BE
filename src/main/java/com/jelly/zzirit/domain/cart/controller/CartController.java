package com.jelly.zzirit.domain.cart.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jelly.zzirit.domain.cart.dto.response.CartFetchResponse;
import com.jelly.zzirit.domain.cart.service.CartService;
import com.jelly.zzirit.global.AuthMember;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

	private final CartService cartService;

	@GetMapping("/me")
	public CartFetchResponse getMyCart() {
		return cartService.getMyCart(AuthMember.getMemberId());
	}
}