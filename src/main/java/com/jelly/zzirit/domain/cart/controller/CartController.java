package com.jelly.zzirit.domain.cart.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jelly.zzirit.domain.cart.dto.response.CartFetchResponse;
import com.jelly.zzirit.domain.cart.service.CartService;
import com.jelly.zzirit.global.AuthMember;
import com.jelly.zzirit.global.dto.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "장바구니 API", description = "장바구니와 관련된 API를 설명합니다.")
public class CartController {

	private final CartService cartService;

	@Operation(summary = "내 장바구니 조회", description = "현재 로그인된 사용자의 장바구니를 조회합니다.")
	@GetMapping("/me")
	public CartFetchResponse getMyCart() {
		return cartService.getMyCart(AuthMember.getMemberId());
	}
}