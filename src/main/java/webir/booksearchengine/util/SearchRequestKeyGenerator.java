package webir.booksearchengine.util;

import org.springframework.cache.interceptor.KeyGenerator;
import webir.booksearchengine.dto.SearchRequest;

import java.lang.reflect.Method;

public class SearchRequestKeyGenerator implements KeyGenerator {

    @Override
    public Object generate(Object target, Method method, Object... params) {
        if (params.length > 0 && params[0] instanceof SearchRequest) {
            SearchRequest request = (SearchRequest) params[0];

            // Get values from the SearchRequest object including all the nested fields
            return String.format("SearchRequest::%s::%b::%b::%b::%b::%d::%d",
                    request.getQueryString(),
                    request.getQueryOption().isUseTitle(),
                    request.getQueryOption().isUseIsbn(),
                    request.getQueryOption().isUseDescription(),
                    request.getQueryOption().isUseAuthors(),
                    request.getPage(),
                    request.getPageSize());
        }

        // Return a default key if the parameters don't match what we expect
        return "DefaultSearchCacheKey";
    }
}