package webir.booksearchengine.service.impl;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.th.ThaiAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import webir.booksearchengine.model.Book;
import webir.booksearchengine.repository.BookRepository;
import webir.booksearchengine.util.AuthorNamesUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class IndexPageServiceImpl {

    private final BookRepository bookRepository;

    private final Path indexPath = Paths.get("index");
    private Directory indexDirectory;
    private final Analyzer analyzer = new ThaiAnalyzer();
    private final IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer)
            .setOpenMode(IndexWriterConfig.OpenMode.CREATE);
    private IndexWriter indexWriter;

    public IndexPageServiceImpl(BookRepository bookRepository, ApplicationEventPublisher publisher) {
        this.bookRepository = bookRepository;

        if (Files.notExists(indexPath)) {
            try {
                Files.createDirectory(indexPath);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }

        try {
            indexDirectory = FSDirectory.open(indexPath);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            indexWriter = new IndexWriter(indexDirectory, indexWriterConfig);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CompletableFuture<List<Long>> indexPage(int pageNumber, int pageSize, Runnable onComplete) {
        // Must sort by something to ensure mutual exclusivity between all async calls
        Pageable pageRequest = PageRequest.of(pageNumber, pageSize, Sort.by("id").ascending());
        Page<Book> bookPage = bookRepository.findByIsIndexedFalse(pageRequest);

        List<Long> completedBookIds = new ArrayList<>();
        List<Document> documents = new ArrayList<>();

        for (Book book : bookPage) {
            try {
                // Force initialization of lazy collections
                book.getAuthors().size();
                documents.add(getBookDocument(book));
                completedBookIds.add(book.getId());
            } catch (Exception e) {
                System.err.println("Error processing book ID: " + book.getId() + ": " + e.getMessage());
            }
        }

        try {
            indexWriter.addDocuments(documents);
            indexWriter.commit();
        } catch (IOException e) {
            System.err.println("Error committing documents: " + e.getMessage());
        }

        if (onComplete != null) {
            onComplete.run();
        }

        return CompletableFuture.completedFuture(completedBookIds);
    }

    private Document getBookDocument(Book book) {
        Document document = new Document();

        // Handle null values by providing empty string defaults
        String url = book.getUrl() != null ? book.getUrl() : "";
        String imageUrl = book.getImageUrl() != null ? book.getImageUrl() : "";
        String title = book.getTitle() != null ? book.getTitle() : "";
        String description = book.getDescription() != null ? book.getDescription() : "";
        String isbn = book.getIsbn() != null ? book.getIsbn() : "";

        document.add(new StoredField("url", url));
        document.add(new StoredField("image_url", imageUrl));
        document.add(new TextField("title", title, TextField.Store.YES));

        // Clean ISBN before adding to index (after ensuring it's not null)
        String cleanIsbn = isbn.replaceAll("[^0-9]", "");
        document.add(new StringField("isbn", cleanIsbn, StringField.Store.YES));
        document.add(new TextField("description", description, TextField.Store.YES));

        // String join authors before adding to index, handling null collection
        String authorsString = "";
        if (book.getAuthors() != null && !book.getAuthors().isEmpty()) {
            List<String> authors = book.getAuthors().stream()
                    .map(author -> author != null ? author.getName() : "")
                    .filter(name -> name != null && !name.isEmpty())
                    .toList();
            authorsString = AuthorNamesUtil.joinAuthorNames(authors);
        }
        document.add(new TextField("authors", authorsString, TextField.Store.YES));

        return document;
    }
}
