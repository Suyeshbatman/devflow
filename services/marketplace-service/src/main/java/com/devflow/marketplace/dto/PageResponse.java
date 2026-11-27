package com.devflow.marketplace.dto;

import org.springframework.data.domain.Page;
import java.util.List;

// [RECORD] A GENERIC record: <T> is a TYPE PARAMETER, a placeholder for "the kind of item on the page".
//   PageResponse<ProductResponse> = a page of products; PageResponse<OrderResponse> = a page of orders.
// Why not return Spring's Page directly? Its JSON shape is an internal detail that can change between
//   versions. Our own record gives the frontend a STABLE contract.
public record PageResponse<T> (
        List<T> content,  // [RECORD COMPONENT] the items on this page
        int page,  // the page number (starts at 0)
        int size,  // the requested page size
        long totalElements,  // matching items across ALL pages
        int totalPages,
        boolean first,  // is this the first page?
        boolean last  // is this the last page?
) {
    // [STATIC METHOD] A GENERIC METHOD. The <T> before the return type declares the method's own type parameter.
    //   [PARAMETER] page: a Page<T> OBJECT from Spring Data.
    public static <T> PageResponse <T> from(Page<T> page){
        // [CONSTRUCTOR CALL] new PageResponse<>(...): the diamond <> lets Java infer T.
        //   Each argument is a [METHOD CALL] on the Page OBJECT.
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }
}
