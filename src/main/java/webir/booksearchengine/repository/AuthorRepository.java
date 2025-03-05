package webir.booksearchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import webir.booksearchengine.model.Author;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {

}
