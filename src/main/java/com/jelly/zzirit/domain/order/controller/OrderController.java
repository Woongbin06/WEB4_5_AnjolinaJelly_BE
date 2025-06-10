package com.jelly.zzirit.domain.order.controller;

import static org.springframework.data.domain.Sort.Direction.*;

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
import com.jelly.zzirit.domain.order.service.order.QueryOrderService;
import com.jelly.zzirit.domain.order.service.order.cancel.OrderCancellationFacade;
import com.jelly.zzirit.global.AuthMember;
import com.jelly.zzirit.global.dto.PageResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final QueryOrderService queryOrderService;
    private final OrderCancellationFacade orderCancellationFacade;

    @GetMapping
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
    public void cancelOrder(@PathVariable(name = "order-id") Long orderId) {
        orderCancellationFacade.cancelOrderAndRefund(
            orderId,
            AuthMember.getAuthUser()
        );
    }
}