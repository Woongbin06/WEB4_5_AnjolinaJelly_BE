package com.jelly.zzirit.domain.admin.service;

import com.jelly.zzirit.domain.admin.dto.request.ItemCreateRequest;
import com.jelly.zzirit.domain.admin.dto.request.ItemUpdateRequest;
import com.jelly.zzirit.domain.item.entity.Item;
import com.jelly.zzirit.domain.item.entity.TypeBrand;
import com.jelly.zzirit.domain.item.entity.stock.ItemStock;
import com.jelly.zzirit.domain.item.repository.ItemRepository;
import com.jelly.zzirit.domain.item.repository.stock.ItemStockRepository;
import com.jelly.zzirit.domain.item.repository.TypeBrandRepository;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidItemException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommandAdminServiceTest {
    @InjectMocks
    private CommandAdminService commandAdminItemService;

    @Mock
    private ItemRepository itemRepository;
    @Mock
    private ItemStockRepository itemStockRepository;
    @Mock
    private TypeBrandRepository typeBrandRepository;
    @Mock
    private CommandS3Service commandS3Service;

    @Test
    void 관리자_상품_등록_이미지가_없으면_예외를_던진다() {
        // given: 빈 이미지 URL로 생성 요청을 구성
        ItemCreateRequest request = new ItemCreateRequest(
                "상품 이름", 30, new BigDecimal("100000"), 1L, 1L, ""
        );

        InvalidItemException exception = assertThrows(InvalidItemException.class,
                () -> commandAdminItemService.createItem(request));
        assertEquals(BaseResponseStatus.IMAGE_REQUIRED, exception.getStatus());
    }

    @Test
    void 관리자_상품_등록_TypeBrand가_없으면_예외를_던진다() {
        // given: 정상적인 이미지와 요청 데이터 구성
        ItemCreateRequest request = new ItemCreateRequest(
                "상품 이름", 30, new BigDecimal("100000"), 1L, 1L, "http://image.com/item.jpg"
        );

        // TypeBrandRepository가 빈 값을 반환하도록 설정
        when(typeBrandRepository.findByTypeIdAndBrandId(1L, 1L)).thenReturn(Optional.empty());

        // when & then: 예외 발생을 검증
        InvalidItemException exception = assertThrows(InvalidItemException.class,
                () -> commandAdminItemService.createItem(request));
        assertEquals(BaseResponseStatus.TYPE_BRAND_NOT_FOUND,exception.getStatus());
    }

    @Test
    void 관리자_상품_등록_정상적인_요청이면_Item과_Stock을_저장하고_Empty를_반환한다() {
        // given: 가짜 TypeBrand 객체 생성
        TypeBrand typeBrand = mock(TypeBrand.class);

        // 가짜 Item, ItemStock 객체 생성
        Item item = mock(Item.class);
        ItemStock itemStock = mock(ItemStock.class);

        //정상적인 이미지와 요청 데이터 구성
        ItemCreateRequest request = mock(ItemCreateRequest.class);
        when(request.imageUrl()).thenReturn("http://image.com/item.jpg");
        when(request.typeId()).thenReturn(1L);
        when(request.brandId()).thenReturn(1L);
        when(request.toItemEntity(any())).thenReturn(item);
        when(request.toItemStockEntity(any())).thenReturn(itemStock);

        // request의 toItemEntity(), toItemStockEntity()가 새로운 객체를 생성한다고 가정
        when(typeBrandRepository.findByTypeIdAndBrandId(1L, 1L)).thenReturn(Optional.of(typeBrand));
        when(request.toItemEntity(typeBrand)).thenReturn(item);
        when(request.toItemStockEntity(item)).thenReturn(itemStock);

        // when: 서비스 호출
        commandAdminItemService.createItem(request);

        // then: 저장 메서드가 각각 한 번씩 호출됐는지, 반환값이 Empty인지 검증
        verify(itemRepository).save(item);              // 상품 저장 확인
        verify(itemStockRepository).save(itemStock);    // 재고 저장 확인// 반환값이 Empty 싱글턴인지 확인
    }

    @Test
    void 관리자_상품_수정_상품이_존재하지_않으면_예외를_던진다() {
        // given
        Long itemId = 1L;
        ItemUpdateRequest request = new ItemUpdateRequest(10, new BigDecimal("10000"), "");

        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        // when & then
        InvalidItemException exception = assertThrows(InvalidItemException.class,
                () -> commandAdminItemService.updateItem(itemId, request));

        assertEquals(BaseResponseStatus.ITEM_NOT_FOUND, exception.getStatus());
    }

    @Test
    void 관리자_상품_수정_재고가_존재하지_않으면_예외를_던진다() {
        // given
        Long itemId = 1L;
        Item item = mock(Item.class);
        ItemUpdateRequest request = new ItemUpdateRequest(30, new BigDecimal("15000"), "");

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(itemStockRepository.findByItem(item)).thenReturn(Optional.empty());

        // when & then
        InvalidItemException exception = assertThrows(InvalidItemException.class,
                () -> commandAdminItemService.updateItem(itemId, request));

        assertEquals(BaseResponseStatus.ITEM_STOCK_NOT_FOUND, exception.getStatus());
    }

    @Test
    void 관리자_상품_수정_정상적으로_상품과_재고가_수정된다() {
        // given
        Long itemId = 1L;
        Item item = mock(Item.class);           // 상품 mock
        ItemStock itemStock = mock(ItemStock.class); // 재고 mock

        // 요청: 가격, 재고 수정
        ItemUpdateRequest request = new ItemUpdateRequest(
                50, new BigDecimal("20000"), ""
        );

        // mock 객체 반환 설정
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(itemStockRepository.findByItem(item)).thenReturn(Optional.of(itemStock));

        // when
        commandAdminItemService.updateItem(itemId, request);

        // then
        verify(item).update(request); // 가격 변경 확인
        verify(itemStock).changeQuantity(50);              // 수량 변경 확인         // 반환 값 확인
    }

    @Test
    void 관리자_상품_삭제_상품이_없으면_예외를_던진다() {
        // given
        Long itemId = 2L;

        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        // when & then
        InvalidItemException exception = assertThrows(InvalidItemException.class,
                () -> commandAdminItemService.deleteItem(itemId));

        assertEquals(BaseResponseStatus.ITEM_NOT_FOUND, exception.getStatus());
    }

    @Test
    void 관리자_상품_삭제_재고가_없으면_예외를_던진다() {
        // given
        Long itemId = 3L;
        Item item = mock(Item.class);

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(itemStockRepository.findByItemId(itemId)).thenReturn(Optional.empty());

        // when & then
        InvalidItemException exception = assertThrows(InvalidItemException.class,
                () -> commandAdminItemService.deleteItem(itemId));

        assertEquals(BaseResponseStatus.ITEM_STOCK_NOT_FOUND, exception.getStatus());
    }

    @Test
    void 관리자_상품_삭제_정상적으로_상품과_재고가_삭제된다() {
        // given
        Long itemId = 1L;
        Item item = mock(Item.class);
        ItemStock itemStock = mock(ItemStock.class);

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(itemStockRepository.findByItemId(itemId)).thenReturn(Optional.of(itemStock));

        // when
        commandAdminItemService.deleteItem(itemId);

        // then
        verify(itemStockRepository).delete(itemStock); // 재고 삭제 호출 확인
        verify(itemRepository).delete(item);           // 상품 삭제 호출 확인// 반환값 검증
    }

    @Test
    void 관리자_상품_이미지수정_상품이_없으면_예외를_던진다() {
        // given
        Long itemId = 3L;
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        // when & then
        InvalidItemException exception = assertThrows(InvalidItemException.class,
                () -> commandAdminItemService.updateImageUrl(itemId, "https://s3.new.img"));

        assertEquals(BaseResponseStatus.ITEM_NOT_FOUND, exception.getStatus());
    }

    @Test
    void 관리자_상품_이미지수정_기존이미지와_같으면_S3삭제없이_이미지만_설정한다() {
        // given
        Long itemId = 2L;
        String sameUrl = "https://s3.bucket/image.jpg";

        Item item = mock(Item.class);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(item.getImageUrl()).thenReturn(sameUrl);

        // when
        commandAdminItemService.updateImageUrl(itemId, sameUrl);

        // then
        verify(commandS3Service, never()).delete(any());  // ❌ 삭제 X
        verify(item).setImageUrl(sameUrl);         // ✅ 그대로 설정은 함
    }

    @Test
    void 관리자_상품_이미지수정_기존이미지와_다르면_S3에서_삭제하고_새이미지로_변경한다() {
        // given
        Long itemId = 1L;
        String oldUrl = "https://s3.bucket/old-image.jpg";
        String newUrl = "https://s3.bucket/new-image.jpg";

        Item item = mock(Item.class);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(item.getImageUrl()).thenReturn(oldUrl);

        // when
        commandAdminItemService.updateImageUrl(itemId, newUrl);

        // then
        verify(commandS3Service).delete(oldUrl);          // ✅ S3에서 삭제
        verify(item).setImageUrl(newUrl);          // ✅ 이미지 URL 변경
    }
}
