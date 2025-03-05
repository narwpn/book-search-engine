package webir.booksearchengine.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchRequest {
    String queryString;
    QueryOption queryOption;
    int page;
    int pageSize;
}
