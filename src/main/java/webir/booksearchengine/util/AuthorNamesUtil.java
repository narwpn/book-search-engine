package webir.booksearchengine.util;

import java.util.List;

public class AuthorNamesUtil {

    private static final String SEPARATOR = "_";

    public static String[] splitAuthorNames(String authorNames) {
        return authorNames.split(SEPARATOR);
    }

    public static String joinAuthorNames(List<String> authorNames) {
        return String.join(SEPARATOR, authorNames);
    }
}
