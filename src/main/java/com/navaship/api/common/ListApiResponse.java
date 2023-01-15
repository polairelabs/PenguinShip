package com.navaship.api.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ListApiResponse<T> {
    private int count;
    private int currentPage;
    private int totalPages;
    private List<T> data;
}
