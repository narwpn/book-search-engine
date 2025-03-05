package webir.booksearchengine.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponse {
    private BookResponse[] books;
    private int page;
    private int pageSize;
    private int totalHits;
}
