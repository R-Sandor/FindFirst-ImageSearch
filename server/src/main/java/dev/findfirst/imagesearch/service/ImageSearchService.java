package dev.findfirst.imagesearch.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.SortMode;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
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
  private final String ACADEMIC_IMAGES = "academic-images";
  private final String PREDS = "predictions";
  private final String PREDS_LBL = "predictions.label";
  private final String PREDS_CONF = "predictions.confidence";

  public Optional<AcademicImage> findById(String id) {
    return imageRepo.findById(id);
  }

  public List<AcademicImage> findByQuery(String text, int k) {
    return query(torch.getEmbeddings(text)).stream()
        .limit(k)
        .map(sh -> sh.getContent())
        .collect(Collectors.toList());
  }

  // Return the top results of a given category
  public List<AcademicImage> findTopResultsforImageClass(String imageClass)
      throws ElasticsearchException, IOException {

    // spotless:off
    Query byPredictionsType = MatchQuery.of(m -> m 
      .field(PREDS_LBL)
      .query(imageClass)
    )._toQuery();

    SortOptions sortByConfidence = SortOptions.of(ns -> ns 
      .field(f -> f
        .field(PREDS_CONF)
        .mode(SortMode.Min)
        .order(SortOrder.Desc)
        .nested(nf -> nf 
          .path(PREDS)
          .filter(fq -> fq 
            .term(t -> t 
              .field(PREDS_LBL)
              .value(imageClass))))));

    SearchResponse<AcademicImage> response = esClient.search(s -> s 
    .index(ACADEMIC_IMAGES)
    .query(q -> q
      .nested( nq -> nq
        .path(PREDS)
        .query(pq -> pq 
          .bool(b -> b
            .must(byPredictionsType))))
    )
    .sort(sortByConfidence),AcademicImage.class);
    // spotless:on

    var aiHits = response.hits().hits();
    return aiHits.stream().limit(10).map(aih -> aih.source()).toList();
  }

  // Return the top results for a given query with query

  // multiple categories

  /**
   * Finds a similar image. First calls Pytorch to get the vector then passes that to the query.
   *
   * @param file Image file
   * @param k number of figures to return (10 is default)
   * @return List of Academic-Images.
   * @throws Exception
   * @see https://spring.io/guides/gs/uploading-files/ for how to upload files. Used to produce
   *     function.
   */
  public List<AcademicImage> findSimilarImages(MultipartFile file, int k) throws Exception {
    String type = file.getContentType();
    if (file.isEmpty() || !isImage(type)) {
      throw new Exception("Empty file or Wrong type.");
    }

    return query(torch.getEmbeddings(file)).stream() // search results
        .limit(k)
        .map(searchHits -> searchHits.getContent())
        .collect(Collectors.toList());
  }

  private SearchHits<AcademicImage> query(double[] embedding) {
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
    var ai =
        esClient
            .get(u -> u.index("academic-images").id(metaData.documentID()), AcademicImage.class)
            .source();
    ai.setMetadata(metaData);
    esClient.update(
        u -> u.index("academic-images").id(metaData.documentID()).doc(ai), AcademicImage.class);
  }
}
