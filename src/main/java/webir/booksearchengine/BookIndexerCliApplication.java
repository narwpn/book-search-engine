package webir.booksearchengine;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import webir.booksearchengine.service.IndexService;

@SpringBootApplication()
public class BookIndexerCliApplication {
    public static void main(String[] args) {
        // Don't enable web server for this command line program
        var context = new SpringApplicationBuilder(BookIndexerCliApplication.class)
                .web(WebApplicationType.NONE)
                .run(args);
        var indexService = context.getBean(IndexService.class);
        indexService.indexAll();
        context.close();
    }
}
