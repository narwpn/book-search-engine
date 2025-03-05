package webir.booksearchengine.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueryOption {
    boolean useTitle;
    boolean useIsbn;
    boolean useDescription;
    boolean useAuthors;
}
