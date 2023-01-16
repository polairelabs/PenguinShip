package com.navaship.api.common;

import org.springframework.data.domain.Sort;

public class SearchApiConstants {
    public static final int DEFAULT_SEARCH_PAGE_NUMBER = 1;
    public static final int DEFAULT_SEARCH_PAGE_SIZE = 20;
    public static final String DEFAULT_SEARCH_SORT_FIELD = "createdAt";
    public static final Sort.Direction DEFAULT_SEARCH_DIRECTION = Sort.Direction.DESC;
}
