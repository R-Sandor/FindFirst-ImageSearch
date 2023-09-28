package dev.findfirst.bookmarkit.service;

import dev.findfirst.bookmarkit.model.AcademicImage;
import dev.findfirst.bookmarkit.repository.elastic.AcademicImageRepository;
import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.StringQuery;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;
import org.springframework.web.reactive.function.client.WebClient.UriSpec;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ImageSearchService {

  @Value("${microservice.pytorch.url: http://localhost:5000}") private String pytorchUrl;

  private final AcademicImageRepository imageRepo;
  private final ElasticsearchOperations elasticsearchOperations;

  public Optional<AcademicImage> findById(String id) {
    return imageRepo.findByImageId(id);
  }

  public void findByQuery(String text) {

    var vector = buildRequest(text).image_embeddings();
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
                "params": { "queryVector": %s }
                }
              }
            }
            """
                .formatted(Arrays.toString(vector)));

    SearchHits<AcademicImage> searchHits =
        elasticsearchOperations.search(query, AcademicImage.class);
  }

  private EmbeddingVector buildRequest(String text) {

    WebClient client =
        WebClient.builder()
            .baseUrl(pytorchUrl)
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

  // Used documentation from https://spring.io/guides/gs/uploading-files/
  public void findSimilarImages(MultipartFile file) throws Exception {
    String type = file.getContentType();
    if (file.isEmpty() || !isImage(type)) {
      throw new Exception("Empty file or Wrong type.");
    }

    MultipartBodyBuilder builder = new MultipartBodyBuilder();
    builder.part("file", file.getResource());
    MultiValueMap<String, HttpEntity<?>> parts = builder.build();
    WebClient client = WebClient.create(pytorchUrl);
    Mono<EmbeddingVector> result =
        client
            .post()
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .bodyValue(parts)
            .retrieve()
            .bodyToMono(EmbeddingVector.class);

    result.block(Duration.ofMillis(2000));
  }

  private boolean isImage(String type) {
    var imageTypes = type.split("/")[1];
    return imageTypes.equalsIgnoreCase("png")
        || imageTypes.equalsIgnoreCase("jpeg")
        || imageTypes.equalsIgnoreCase("jpg");
  }

  record EmbeddingVector(double[] image_embeddings) {}
}
