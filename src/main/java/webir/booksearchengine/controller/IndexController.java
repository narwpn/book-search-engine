package webir.booksearchengine.controller;

import org.springframework.web.bind.annotation.*;

import webir.booksearchengine.service.IndexService;

@RestController
@RequestMapping("/index")
public class IndexController {

    private IndexService indexService;

    public IndexController(IndexService indexService) {
        this.indexService = indexService;
    }

    @PostMapping("/start")
    public void startIndexing() {
        indexService.indexAll();
    }

    @PostMapping("/stop")
    public void stopIndexing() {
        indexService.stopIndexing();
    }
}
