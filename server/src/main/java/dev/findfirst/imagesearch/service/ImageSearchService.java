package dev.findfirst.imagesearch.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.core.UpdateRequest;
import dev.findfirst.imagesearch.model.AcademicImage;
import dev.findfirst.imagesearch.repository.AcademicImageRepository;
import dev.findfirst.imagesearch.utility.MetaData;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.StringQuery;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageSearchService {

  private final AcademicImageRepository imageRepo;
  private final ElasticsearchOperations elasticsearchOperations;
  private final ElasticsearchClient esClient;
  private final TorchService torch;

  public Optional<AcademicImage> findById(String id) {
    return imageRepo.findById(id);
  }

  public List<AcademicImage> findByQuery(String text, int k) {
    return createQuery(torch.getEmbeddings(text)).stream()
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

    return createQuery(torch.getEmbeddings(file)).stream()
        .limit(k)
        .map(sh -> sh.getContent())
        .collect(Collectors.toList());
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
                "source": "cosineSimilarity(params.queryVector, 'embedding')+1.0",
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

  public void updateImage(MetaData metaData) throws ElasticsearchException, IOException {
   var ai = esClient.get(u -> u.index("academic-images").id(metaData.documentID()),
    AcademicImage.class).source();
    ai.setCaption(metaData.caption());
    ai.setPredictions(metaData.predictions());
    ai.setImagename(metaData.documentID());
    ai.setId(metaData.documentID());

    esClient.update(
        u -> u.index("academic-images").id(metaData.documentID())
        .doc(ai),
        AcademicImage.class);
  }
}
