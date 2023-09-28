package dev.findfirst.bookmarkit.service;

import dev.findfirst.bookmarkit.model.AcademicImage;
import dev.findfirst.bookmarkit.repository.elastic.AcademicImageRepository;
import jakarta.activation.MimetypesFileTypeMap;

import java.io.File;
import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

import lombok.RequiredArgsConstructor;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
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
import org.springframework.web.reactive.function.BodyInserters;
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


  // Used documentation from https://spring.io/guides/gs/uploading-files/
  // TODO: validation on the image.
  public void findSimilarImages(MultipartFile file) throws Exception {
    String type = file.getContentType();
			if (file.isEmpty() || !isImage(type)) {
				throw new Exception("Empty file or Wrong type.");
			}

      MultipartBodyBuilder builder = new MultipartBodyBuilder();
      builder.part("file", file.getResource());
      MultiValueMap<String, HttpEntity<?>> parts = builder.build();
      WebClient client = WebClient.create("http://localhost:5000/");
      Mono<EmbeddingVector> result = client.post()
  		  .contentType(MediaType.MULTIPART_FORM_DATA)
	  	  .bodyValue(parts)
		    .retrieve()
		   .bodyToMono(EmbeddingVector.class);      

    result.block(Duration.ofMillis(2000));
        
  }

  private boolean isImage(String type) { 
      var imageTypes = type.split("/")[1];
      return imageTypes.equalsIgnoreCase("png") || imageTypes.equalsIgnoreCase("jpeg") || imageTypes.equalsIgnoreCase("jpg");
    }

   public MultiValueMap<String, HttpEntity<?>> fromFile(File file) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", new FileSystemResource(file));
        return builder.build();
    }

  record EmbeddingVector(double[] image_embeddings) {}
}
