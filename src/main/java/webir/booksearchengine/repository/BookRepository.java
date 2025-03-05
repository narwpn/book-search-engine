package webir.booksearchengine.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import webir.booksearchengine.model.Book;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    @Query("SELECT b FROM Book b LEFT JOIN FETCH b.authors WHERE b.isIndexed = false")
    Page<Book> findByIsIndexedFalse(Pageable pageable);

    List<Book> findByIsIndexedFalse();
}
