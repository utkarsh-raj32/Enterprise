package com.enterprise.hrm.common;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * ============================================================
 * PAGE RESPONSE WRAPPER
 * ============================================================
 *
 * WHY a custom PageResponse instead of Spring's Page<T>?
 *   Spring's Page<T> object contains internal metadata (sort, pageable)
 *   that is verbose and couples the client to Spring internals.
 *   A custom PageResponse exposes only what clients need:
 *   content, page info, total counts.
 *
 *   Example response:
 *   {
 *     "content": [...],
 *     "pageNumber": 0,
 *     "pageSize": 10,
 *     "totalElements": 147,
 *     "totalPages": 15,
 *     "last": false
 *   }
 *
 * PAGINATION INTERVIEW QUESTION:
 *   Q: How does Spring Data JPA pagination work?
 *   A: Repository methods accept a Pageable argument:
 *      repository.findAll(PageRequest.of(page, size, Sort.by("lastName")))
 *      Returns a Page<T> with the slice of data + metadata.
 *      We convert this to PageResponse<T> in the service layer.
 */
@Getter
@Builder
public class PageResponse<T> {

    /** The actual list of items for this page */
    private List<T> content;

    /** Current page index (0-based) */
    private int pageNumber;

    /** Number of items per page */
    private int pageSize;

    /** Total number of items across all pages */
    private long totalElements;

    /** Total number of pages */
    private int totalPages;

    /** True if this is the last page */
    private boolean last;

    /** True if this is the first page */
    private boolean first;
}
