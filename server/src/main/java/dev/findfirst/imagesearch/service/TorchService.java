package dev.findfirst.imagesearch.service;

import static org.springframework.web.reactive.function.BodyInserters.fromMultipartData;

import dev.findfirst.imagesearch.utility.MetaData;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
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
@Slf4j
public class TorchService {

  @Value("${microservice.pytorch.url:http://localhost:5000/}") private String pytorchUrl;

  public double[] getEmbeddings(String text) {
    WebClient client =
        WebClient.builder()
            .baseUrl(pytorchUrl)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();

    UriSpec<RequestBodySpec> uriSpec = client.method(HttpMethod.GET); // GET

    RequestBodySpec bodySpec = uriSpec.uri(uriBuilder -> uriBuilder.pathSegment("/").build());
    RequestHeadersSpec<?> headersSpec =
        bodySpec.bodyValue("""
      {"query": "%s" }
      """.formatted(text));
    Mono<EmbeddingVector> vector =
        headersSpec.accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(EmbeddingVector.class);

    return vector.block(Duration.ofMillis(2000)).image_embeddings();
  }

  public double[] getEmbeddings(MultipartFile file) {
    WebClient client = WebClient.create(pytorchUrl);
    var result =
        client
            .post() // POST
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(fromMultipartData("file", file.getResource()))
            .retrieve()
            .bodyToMono(EmbeddingVector.class);
    return result.block(Duration.ofMillis(2000)).image_embeddings();
  }

  public Predictions predict(MetaData metaData) {
    WebClient client = WebClient.create(pytorchUrl);
    log.info("File  {}", metaData.filePath().toString());
    log.info("File exists {}", metaData.filePath().toFile().exists());
    var result =
        client
            .post() // POST
            .uri("predict")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(fromMultipartData("file", new FileSystemResource(metaData.filePath())))
            .retrieve()
            .bodyToMono(Predictions.class);

    return result.block(Duration.ofMillis(2000));
  }

  public record EmbeddingVector(double[] image_embeddings) {}

  public record Predictions(Prediction[] predictions) {}

  public record Prediction(String label, String confidence) {}
}
