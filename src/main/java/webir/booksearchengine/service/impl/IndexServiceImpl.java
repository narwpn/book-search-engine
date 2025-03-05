package webir.booksearchengine.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.th.ThaiAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import webir.booksearchengine.model.Book;
import webir.booksearchengine.repository.BookRepository;
import webir.booksearchengine.service.IndexService;
import webir.booksearchengine.util.AuthorNamesUtil;

@Service
public class IndexServiceImpl implements IndexService {

    // Execution
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Future<?> future;
    private AtomicBoolean isRunning = new AtomicBoolean(false);

    // Repository
    private BookRepository bookRepository;

    // Indexing
    private final int BATCH_SIZE = 100;
    private Path indexPath = Paths.get("index");
    private Directory indexDirectory;
    private Analyzer analyzer = new ThaiAnalyzer();
    private IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer)
            .setOpenMode(OpenMode.CREATE);
    private IndexWriter indexWriter;

    public IndexServiceImpl(BookRepository bookRepository) {
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

    @Transactional
    public void indexAll() {
        if (isRunning.get() || (future != null && !future.isDone())) {
            return; // Don't start if already running
        }

        FutureTask<Void> task = new FutureTask<>(() -> {
            isRunning.set(true);
            try {
                int pageNumber = 0;
                Page<Book> bookPage;
                do {
                    try {
                        // Process this batch in its own transaction
                        processBookBatch(pageNumber);
                        pageNumber++;
                    } catch (Exception e) {
                        // Log the error but continue with next batch
                        System.err.println("Error processing batch " + pageNumber + ": " + e.getMessage());
                        e.printStackTrace();
                        // Maybe skip this batch and continue
                        pageNumber++;
                    }

                    // Check if there are more unindexed books
                    Pageable checkRequest = PageRequest.of(pageNumber, BATCH_SIZE);
                    bookPage = bookRepository.findByIsIndexedFalse(checkRequest);
                } while (!bookPage.isEmpty() && isRunning.get());
            } finally {
                isRunning.set(false);
            }
            return null;
        });

        future = executorService.submit(task);
    }

    @Transactional
    public void processBookBatch(int pageNumber) throws IOException {
        List<Document> documents = new ArrayList<>();
        Pageable pageRequest = PageRequest.of(pageNumber, BATCH_SIZE);
        Page<Book> bookPage = bookRepository.findByIsIndexedFalse(pageRequest);
        List<Book> books = bookPage.getContent();

        for (Book book : books) {
            try {
                documents.add(getBookDocument(book));
                book.setIndexed(true);
            } catch (Exception e) {
                System.err.println("Error processing book ID: " + book.getId() + ": " + e.getMessage());
                // Continue with next book
            }
        }

        // Batch save books
        if (!books.isEmpty()) {
            bookRepository.saveAll(books);
            indexWriter.addDocuments(documents);
            indexWriter.commit();
        }
    }

    public void stopIndexing() {
        isRunning.set(false);
    }

    private Document getBookDocument(Book book) {
        System.out.println("Indexing book id: " + book.getId());
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
