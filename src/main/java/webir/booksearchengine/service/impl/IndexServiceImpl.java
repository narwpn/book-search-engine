package webir.booksearchengine.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import webir.booksearchengine.model.Book;
import webir.booksearchengine.repository.BookRepository;
import webir.booksearchengine.service.IndexService;
import webir.booksearchengine.util.CliProgressBar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class IndexServiceImpl implements IndexService {

    private BookRepository bookRepository;
    private IndexPageServiceImpl indexPageService;
    private CliProgressBar progressBar;
    private final int CHUNK_SIZE = 300;

    public IndexServiceImpl(BookRepository bookRepository, IndexPageServiceImpl indexPageService) {
        this.bookRepository = bookRepository;
        this.indexPageService = indexPageService;
    }

    public void indexAll() {
        System.out.println("Starting indexing");

        Pageable pageRequest = PageRequest.of(0, CHUNK_SIZE);
        Page<Book> bookPage = bookRepository.findByIsIndexedFalse(pageRequest);

        progressBar = new CliProgressBar("index books", bookPage.getTotalPages());

        List<CompletableFuture<List<Long>>> futures = new ArrayList<>();
        for (int i = 0; i < bookPage.getTotalPages(); i++) {
            futures.add(indexPageService.indexPage(i, CHUNK_SIZE, () -> progressBar.incrementWorkDone(1)));
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        List<Long> successBookIds = futures.stream()
                .map(CompletableFuture::join)
                .flatMap(Collection::stream)
                .distinct()
                .toList();
        System.out.println("\nTotal books indexed: " + successBookIds.size());

        System.out.println("Finalizing indexing");

        for (int i = 0; i < successBookIds.size(); i += CHUNK_SIZE) {
            int end = Math.min(i + CHUNK_SIZE, successBookIds.size());
            List<Long> batch = successBookIds.subList(i, end);
            bookRepository.updateIsIndexedByIds(batch);
        }

        System.out.println("Finished indexing");
    }
}
