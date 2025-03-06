package webir.booksearchengine.service.impl;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import webir.booksearchengine.dto.BookResponse;
import webir.booksearchengine.dto.SearchRequest;
import webir.booksearchengine.dto.SearchResponse;
import webir.booksearchengine.service.SearchService;
import webir.booksearchengine.util.AuthorNamesUtil;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.th.ThaiAnalyzer;
import org.apache.lucene.document.Document;

import org.apache.lucene.index.DirectoryReader;

import org.apache.lucene.index.StoredFields;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;

import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanClause;

@Service
public class SearchServiceImpl implements SearchService {

    private static final String INDEX_PATH = "index";

    @Cacheable(value = "search", keyGenerator = "searchRequestKeyGenerator")
    public SearchResponse search(SearchRequest searchRequest) {
        try {
            DirectoryReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(INDEX_PATH)));
            IndexSearcher searcher = new IndexSearcher(reader);

            ThaiAnalyzer thaiAnalyzer = new ThaiAnalyzer();

            // Build a Boolean query that can combine multiple search conditions
            BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();

            if (searchRequest.getQueryOption().isUseTitle()) {
                QueryParser titleParser = new QueryParser("title", thaiAnalyzer);
                Query titleQuery = titleParser.parse(searchRequest.getQueryString());
                queryBuilder.add(titleQuery, BooleanClause.Occur.SHOULD);
            }

            if (searchRequest.getQueryOption().isUseDescription()) {
                QueryParser descriptionParser = new QueryParser("description", thaiAnalyzer);
                Query descriptionQuery = descriptionParser.parse(searchRequest.getQueryString());
                queryBuilder.add(descriptionQuery, BooleanClause.Occur.SHOULD);
            }

            if (searchRequest.getQueryOption().isUseAuthors()) {
                QueryParser authorsParser = new QueryParser("authors", thaiAnalyzer);
                Query authorsQuery = authorsParser.parse(searchRequest.getQueryString());
                queryBuilder.add(authorsQuery, BooleanClause.Occur.SHOULD);
            }

            // Check if we should search ISBN (based on search options)
            if (searchRequest.getQueryOption().isUseIsbn()) {
                TermQuery isbnQuery = new TermQuery(new Term("isbn", searchRequest.getQueryString()));
                queryBuilder.add(isbnQuery, BooleanClause.Occur.SHOULD);
            }

            Query query = queryBuilder.build();

            // Calculate start and end indices for pagination
            int page = Math.max(1, searchRequest.getPage()); // Ensure page is at least 1
            int pageSize = Math.max(1, searchRequest.getPageSize()); // Ensure pageSize is at least 1
            int start = (page - 1) * pageSize;
            int hitsPerPage = pageSize;

            // Ensure we always have a positive number of hits to search for
            int numHits = Math.max(1, start + hitsPerPage);
            System.out.println("Search numHits: " + numHits);

            // Collect docs
            TopDocs results = searcher.search(query, numHits);
            ScoreDoc[] hits = results.scoreDocs;

            // Process search results
            List<BookResponse> books = new ArrayList<>();
            int end = Math.min(hits.length, start + hitsPerPage);

            StoredFields storedFields = searcher.storedFields();
            for (int i = start; i < end; i++) {
                Document doc = storedFields.document(hits[i].doc);
                BookResponse book = new BookResponse();
                book.setTitle(doc.get("title"));
                book.setIsbn(doc.get("isbn"));
                book.setDescription(doc.get("description"));
                book.setUrl(doc.get("url"));
                book.setImageUrl(doc.get("image_url"));

                // Handle null authors field
                String authorsField = doc.get("authors");
                if (authorsField != null && !authorsField.isEmpty()) {
                    String[] authors = AuthorNamesUtil.splitAuthorNames(authorsField);
                    book.setAuthors(authors);
                } else {
                    book.setAuthors(new String[0]);
                }

                books.add(book);
            }

            SearchResponse response = new SearchResponse();
            response.setBooks(books.toArray(new BookResponse[0]));
            response.setTotalHits((int) results.totalHits.value());
            response.setPage(page);
            response.setPageSize(pageSize);

            reader.close();
            return response;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new SearchResponse();
    }
}
