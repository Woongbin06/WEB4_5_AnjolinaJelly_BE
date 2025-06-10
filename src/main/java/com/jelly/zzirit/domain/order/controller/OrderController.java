package com.jelly.zzirit.domain.order.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jelly.zzirit.domain.order.dto.response.OrderFetchResponse;
import com.jelly.zzirit.domain.order.service.order.cancel.OrderCancellationFacade;
import com.jelly.zzirit.domain.order.service.order.QueryOrderService;
import com.jelly.zzirit.global.AuthMember;
import com.jelly.zzirit.global.dto.BaseResponse;
import com.jelly.zzirit.global.dto.Empty;
import com.jelly.zzirit.global.dto.PageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import static org.springframework.data.domain.Sort.Direction.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "주문 API", description = "주문과 관련된 API 를 설명합니다.")
public class OrderController {

    private final QueryOrderService queryOrderService;
    private final OrderCancellationFacade orderCancellationFacade;

    @GetMapping
    @Operation(summary = "주문 전체 조회 API", description = "전체 주문을 페이징 및 정렬 처리해 조회합니다.")
    public PageResponse<OrderFetchResponse> fetchAllOrders(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "desc") String sort
        ) {
        Long memberId = AuthMember.getMemberId();
        Direction direction = sort.equalsIgnoreCase("desc") ? DESC : ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "createdAt"));

        return PageResponse.from(
            queryOrderService.findPagedOrders(memberId, pageable)
                .map(OrderFetchResponse::from)
        );
    }

    @DeleteMapping("/{order-id}")
    @Operation(summary = "주문 취소 및 환불 API", description = "아이디에 해당되는 주문을 취소하고 총 주문 금액을 환불합니다.")
    public void cancelOrder(@PathVariable(name = "order-id") Long orderId) {
        orderCancellationFacade.cancelOrderAndRefund(
            orderId,
            AuthMember.getAuthUser()
        );
    }
}