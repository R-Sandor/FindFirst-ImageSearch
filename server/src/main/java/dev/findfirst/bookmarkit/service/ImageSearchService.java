package dev.findfirst.bookmarkit.service;

import static org.springframework.web.reactive.function.BodyInserters.fromMultipartData;

import dev.findfirst.bookmarkit.model.AcademicImage;
import dev.findfirst.bookmarkit.repository.elastic.AcademicImageRepository;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.StringQuery;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;
import org.springframework.web.reactive.function.client.WebClient.UriSpec;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ImageSearchService {

  @Value("${microservice.pytorch.url:http://localhost:5000/}") private String pytorchUrl;

  private final AcademicImageRepository imageRepo;
  private final ElasticsearchOperations elasticsearchOperations;

  public Optional<AcademicImage> findById(String id) {
    return imageRepo.findByImageId(id);
  }

  public List<AcademicImage> findByQuery(String text, int k) {
    System.out.println(text);
    return createQuery(getEmbeddings(text)).stream()
        .limit(k)
        .map(sh -> sh.getContent())
        .collect(Collectors.toList());
  }

  // Used documentation from https://spring.io/guides/gs/uploading-files/
  public List<AcademicImage> findSimilarImages(MultipartFile file, int k) throws Exception {
    String type = file.getContentType();
    if (file.isEmpty() || !isImage(type)) {
      throw new Exception("Empty file or Wrong type.");
    }

    return createQuery(getEmbeddings(file)).stream()
        .limit(k)
        .map(sh -> sh.getContent())
        .collect(Collectors.toList());
  }

  private double[] getEmbeddings(String text) {
    WebClient client =
        WebClient.builder()
            .baseUrl(pytorchUrl)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();

    UriSpec<RequestBodySpec> uriSpec = client.method(HttpMethod.GET);

    RequestBodySpec bodySpec = uriSpec.uri(uriBuilder -> uriBuilder.pathSegment("/").build());
    RequestHeadersSpec<?> headersSpec =
        bodySpec.bodyValue("""
      {"query": "%s" }
      """.formatted(text));
    Mono<EmbeddingVector> vector =
        headersSpec.accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(EmbeddingVector.class);

    return vector.block(Duration.ofMillis(2000)).image_embeddings();
  }

  private double[] getEmbeddings(MultipartFile file) {
    WebClient client = WebClient.create(pytorchUrl);
    var result =
        client
            .post()
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(fromMultipartData("file", file.getResource()))
            .retrieve()
            .bodyToMono(EmbeddingVector.class);
    return result.block(Duration.ofMillis(2000)).image_embeddings();
  }

  private SearchHits<AcademicImage> createQuery(double[] embedding) {
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
                .formatted(Arrays.toString(embedding)));

    return elasticsearchOperations.search(query, AcademicImage.class);
  }

  private boolean isImage(String type) {
    var imageTypes = type.split("/")[1];
    return imageTypes.equalsIgnoreCase("png")
        || imageTypes.equalsIgnoreCase("jpeg")
        || imageTypes.equalsIgnoreCase("jpg");
  }

  record EmbeddingVector(double[] image_embeddings) {}
}
