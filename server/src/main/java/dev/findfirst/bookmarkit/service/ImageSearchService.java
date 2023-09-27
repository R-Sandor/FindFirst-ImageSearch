package dev.findfirst.bookmarkit.service;

import dev.findfirst.bookmarkit.model.AcademicImage;
import dev.findfirst.bookmarkit.repository.elastic.AcademicImageRepository;
import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.StringQuery;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;
import org.springframework.web.reactive.function.client.WebClient.UriSpec;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ImageSearchService {

  private final AcademicImageRepository imageRepo;
  private final ElasticsearchOperations elasticsearchOperations;

  public Optional<AcademicImage> findById(String id) {
    return imageRepo.findByImageId(id);
  }

  public void findByEmbedding() {

    var vector = buildRequest("dog playinging snow").image_embeddings();
    var query =
        new StringQuery(
            """
            {
            "script_score": {
              "query": {
                "match_all": {}
            },
            "script": {
                "source": "cosineSimilarity(params.queryVector, 'image_embedding')+1.0",
                "params": {
                    "queryVector": %s
                }
            }
        }
    }
        """.formatted(Arrays.toString(vector)));
    SearchHits<AcademicImage> searchHits =
        elasticsearchOperations.search(query, AcademicImage.class);
  }

  private EmbeddingVector buildRequest(String text) {
    // WebClient client = WebClient.create("http://localhost:5000");

WebClient client = WebClient.builder()
  .baseUrl("http://localhost:5000")
  .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE) 
  .build();

    UriSpec<RequestBodySpec> uriSpec = client.method(HttpMethod.GET);

    RequestBodySpec bodySpec = uriSpec.uri(uriBuilder -> uriBuilder.pathSegment("/").build());
    RequestHeadersSpec<?> headersSpec =
        bodySpec.bodyValue("""
      {"query": "dog in snow"}
      """);
    Mono<EmbeddingVector> vector =
        headersSpec.accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(EmbeddingVector.class);

    return vector.block(Duration.ofMillis(2000));
  }

  record EmbeddingVector(double[] image_embeddings) {}
}
