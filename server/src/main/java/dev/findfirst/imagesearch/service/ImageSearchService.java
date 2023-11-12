package dev.findfirst.imagesearch.service;

import static dev.findfirst.imagesearch.service.queries.ImageQueries.ACADEMIC_IMAGES;
import static dev.findfirst.imagesearch.service.queries.ImageQueries.classificationQuery;
import static dev.findfirst.imagesearch.service.queries.ImageQueries.knnQuery;
import static dev.findfirst.imagesearch.service.queries.ImageQueries.sortByConfidence;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import dev.findfirst.imagesearch.model.AcademicImage;
import dev.findfirst.imagesearch.repository.AcademicImageRepository;
import dev.findfirst.imagesearch.utility.MetaData;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageSearchService {

  private final AcademicImageRepository imageRepo;
  private final ElasticsearchClient esClient;
  private final TorchService torch;

  public Optional<AcademicImage> findById(String id) {
    return imageRepo.findById(id);
  }

  /**
   * Text Query for image.
   *
   * @param text the users query for an image.
   * @param k the number of hits to return.
   * @return List of top hits.
   */
  public List<AcademicImage> findByQuery(String text, int k) {
    return queryByImageVect(torch.getEmbeddings(text)).stream().map(h -> h.source()).toList();
  }

  /**
   * Finds the top results for a classification.
   *
   * @param imageClass (i.e. graph, scatter plot, box chart, etc)
   * @return List of Academic-Figures
   * @throws ElasticsearchException error if elastic search is down.
   * @throws IOException error if deserialization fails etc.
   */
  public List<AcademicImage> findTopResultsforImageClass(int k, String... imageClass)
      throws ElasticsearchException, IOException {

    // spotless:off 
    SearchResponse<AcademicImage> response = esClient.search(s -> s 
    .index(ACADEMIC_IMAGES)
    .query(q -> q
      .bool(b -> b
       .must(classificationQuery(imageClass)))
    )
    .sort(sortByConfidence(imageClass[0])),AcademicImage.class);
    // spotless:on

    return response.hits().hits().stream().limit(k).map(aih -> aih.source()).toList();
  }

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
  public List<AcademicImage> findSimilarImages(MultipartFile file, int k, String... imageClasses)
      throws Exception {
    String type = file.getContentType();
    if (file.isEmpty() || !isImage(type)) {
      throw new Exception("Empty file or Wrong type.");
    }
    return queryByImageVect(torch.getEmbeddings(file), imageClasses).stream() // search results
        .limit(k)
        .map(hit -> hit.source())
        .toList();
  }

  /**
   * Finds the images via K-NN search and provides images classifications.
   *
   * @param embedding the image embedding vector to search against.
   * @param imageClasses classes if any selected.
   * @return List of hits.
   */
  private List<Hit<AcademicImage>> queryByImageVect(double[] embedding, String... imageClasses) {
    try {
      Query q = knnQuery(embedding, imageClasses);
      return esClient.search(s -> s.query(q), AcademicImage.class).hits().hits();
    } catch (ElasticsearchException | IOException e) {
      log.error("Error searching for image by embedding {}", e);
    }
    return null;
  }

  /**
   * Utility to check if its an image.
   *
   * @param type file type.
   * @return true or false if the type is an image.
   */
  private boolean isImage(String type) {
    var imageTypes = type.split("/")[1];
    return imageTypes.equalsIgnoreCase("png")
        || imageTypes.equalsIgnoreCase("jpeg")
        || imageTypes.equalsIgnoreCase("jpg");
  }

  /**
   * Update for metadata of a document.
   *
   * @param metaData metadata (caption, text, titles, etc)
   * @throws ElasticsearchException If elastic search is not availabe.
   * @throws IOException IO errors.
   */
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
