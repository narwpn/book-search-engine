package webir.booksearchengine.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import webir.booksearchengine.util.SearchRequestKeyGenerator;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public KeyGenerator searchRequestKeyGenerator() {
        return new SearchRequestKeyGenerator();
    }
}
