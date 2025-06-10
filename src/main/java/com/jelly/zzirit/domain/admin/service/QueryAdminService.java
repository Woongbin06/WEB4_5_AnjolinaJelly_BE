package com.jelly.zzirit.domain.admin.service;

import com.jelly.zzirit.domain.admin.dto.response.AdminItemFetchResponse;
import com.jelly.zzirit.domain.item.repository.ItemQueryRepository;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.dto.PageResponse;
import com.jelly.zzirit.global.exception.custom.InvalidItemException;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QueryAdminService {
    
    private final ItemQueryRepository itemQueryRepository;

    public AdminItemFetchResponse getItemById(Long itemId) {
        return itemQueryRepository.findAdminItemById(itemId)
            .orElseThrow(() -> new InvalidItemException(BaseResponseStatus.ITEM_NOT_FOUND));
    }

    public PageResponse<AdminItemFetchResponse> getSearchItems(String name, String sort, Pageable pageable) {
        return PageResponse.from(
                itemQueryRepository.findAdminItems(name, sort, pageable)
        );
    }
}