package dev.findfirst;

import dev.findfirst.imagesearch.utility.MetadataHandler;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@SpringBootApplication
@Slf4j
public class BookmarkitApplication implements ApplicationRunner {

  @Autowired private MetadataHandler metadataHandler;

  public static void main(String[] args) {
    SpringApplication.run(BookmarkitApplication.class, args);
  }

  // Fix the CORS errors
  @Bean
  public FilterRegistrationBean<CorsFilter> simpleCorsFilter() {
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowCredentials(true);
    // *** URL below needs to match the Vue client URL and port ***
    // Local host and 127.0.0.1 are the same
    config.setAllowedOrigins(
        Arrays.asList(
            "https://localhost:3000",
            "http://localhost:3000",
            "https://devfindfirst.dev:3000",
            "http://localhost:8080",
            "http://127.0.0.1:8080",
            "http://localhost",
            "http://127.0.0.1"));
    config.setAllowedMethods(Collections.singletonList("*"));
    config.setAllowedHeaders(Collections.singletonList("*"));
    source.registerCorsConfiguration("/**", config);
    FilterRegistrationBean<CorsFilter> bean =
        new FilterRegistrationBean<CorsFilter>(new CorsFilter(source));
    bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
    return bean;
  }

  @Override
  public void run(ApplicationArguments args) throws Exception {
    System.out.println(args.getOptionNames());
    if (args.containsOption("readMetadata")) {
      log.debug("Uploading metadata");
      var path = Paths.get(args.getOptionValues("readMetadata").get(0));
      metadataHandler.updateMetadata(path);
    }
    // TODO Auto-generated method stub
  }
}
