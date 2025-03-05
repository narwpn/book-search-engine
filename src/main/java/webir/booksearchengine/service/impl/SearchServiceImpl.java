package webir.booksearchengine.service.impl;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import webir.booksearchengine.dto.SearchRequest;
import webir.booksearchengine.dto.SearchResponse;
import webir.booksearchengine.service.SearchService;

@Service
public class SearchServiceImpl implements SearchService {

    @Cacheable(value = "search", key = "#searchRequest")
    public SearchResponse search(SearchRequest searchRequest) {
        return new SearchResponse();
    }
}
