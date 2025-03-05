package webir.booksearchengine.service;

import webir.booksearchengine.dto.SearchRequest;
import webir.booksearchengine.dto.SearchResponse;

public interface SearchService {
    public SearchResponse search(SearchRequest searchRequest);
}
