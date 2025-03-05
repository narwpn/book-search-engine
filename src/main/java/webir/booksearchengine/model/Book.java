package webir.booksearchengine.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "html_hash", unique = true, nullable = false)
    private String htmlHash;

    @Column(nullable = false)
    private String url;

    @Column(name = "image_url")
    private String imageUrl;

    private String title;
    private String isbn;
    private String description;

    @OneToMany(mappedBy = "book")
    private List<Author> authors = new ArrayList<>();

    @Column(name = "is_indexed")
    private boolean isIndexed;
}
