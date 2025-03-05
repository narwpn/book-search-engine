package webir.booksearchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import webir.booksearchengine.model.Book;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

}
