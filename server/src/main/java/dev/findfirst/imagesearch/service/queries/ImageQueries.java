package dev.findfirst.imagesearch.service.queries;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortMode;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.NestedQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.json.JsonData;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ImageQueries {

  public static final String ACADEMIC_IMAGES = "academic-images";
  public static final String PREDS = "predictions";
  public static final String PREDS_LBL = "predictions.label";
  public static final String PREDS_CONF = "predictions.confidence";

  /**
   * Basic query to sort by one single imageClass.
   *
   * @param imageClass class to search.
   * @return Query for single classifications.
   */
  public static Query byPredictionType(String imageClass) {
    return MatchQuery.of(m -> m.field(PREDS_LBL).query(imageClass))._toQuery();
  }

  /**
   * Searches by the image classifications, only those documents with the selected image
   * classifications. For example if "Graph and Box plot" are selected then only the documents that
   * have both these predictions in their top k will be returned.
   *
   * @param imageClasses classes of images to search.
   * @return List<Query> that is a list of NestedQuery.
   */
  public static List<Query> classificationQuery(String... imageClasses) {
    var queries = new ArrayList<Query>();
    for (var cls : imageClasses) {
      var q =
          NestedQuery.of(
                  n -> n.path(PREDS).query(pq -> pq.bool(nb -> nb.must(byPredictionType(cls)))))
              ._toQuery();
      queries.add(q);
    }
    return queries;
  }

  public static Query knnQuery(double[] embedding, String... imageClass)
      throws JsonProcessingException {

    var query =
        (imageClass != null && imageClass.length > 0)
            ? Query.of(q -> q.bool(b -> b.must(classificationQuery(imageClass))))
            : QueryBuilders.matchAll().build()._toQuery();

    // spotless:off
    return Query.of(
      q -> q
        .scriptScore(ss -> ss.query(query)
          .script(s -> s
            .inline(i -> i
              .source("cosineSimilarity(params.queryVector, 'embedding')+1.0")
              .params("queryVector", JsonData.fromJson(Arrays.toString(embedding)))))));
    // spotless:on
  }

  /**
   * Sort by confidence of image classifications selected.
   *
   * @param imageClass array of image classes.
   * @return SortOptions that sorts by the predictions.confidence
   */
  public static SortOptions sortByConfidence(String... imageClass) {

    var fieldValues = new ArrayList<FieldValue>();
    for (var cls : imageClass) {
      fieldValues.add(FieldValue.of(cls));
    }

    // spotless:off
    return SortOptions.of(ns -> ns 
        .field(f -> f
        .field(PREDS_CONF)
        .mode(SortMode.Min)
        .order(SortOrder.Desc)
        .nested(nf -> nf 
          .path(PREDS)
          .filter(fq -> fq 
            .terms(t -> t
              .field(PREDS_LBL)
              .terms( tt -> tt.value(fieldValues)))))));
    }
    // spotless:on
}
