package webir.booksearchengine.controller;

import org.springframework.web.bind.annotation.*;

import webir.booksearchengine.dto.SearchRequest;
import webir.booksearchengine.dto.SearchResponse;
import webir.booksearchengine.service.SearchService;

@RestController
public class SearchController {

    private SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @PostMapping("/search")
    public SearchResponse search(@RequestBody SearchRequest searchRequest) {
        return searchService.search(searchRequest);
    }
}
