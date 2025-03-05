package webir.booksearchengine.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookResponse {
    private String title;
    private String isbn;
    private String description;
    private String[] authors;
    private String url;
    private String imageUrl;
}
