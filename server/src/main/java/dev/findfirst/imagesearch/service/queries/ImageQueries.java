package dev.findfirst.imagesearch.service.queries;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortMode;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.NestedQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import java.util.ArrayList;
import java.util.List;

public class ImageQueries {

  public static final String ACADEMIC_IMAGES = "academic-images";
  public static final String PREDS = "predictions";
  public static final String PREDS_LBL = "predictions.label";
  public static final String PREDS_CONF = "predictions.confidence";

  public static Query byPredictionType(String imageClass) {
    return MatchQuery.of(m -> m.field(PREDS_LBL).query(imageClass))._toQuery();
  }

  /**
   * Searches by the image classifications, only those documents with the selected image
   * classifications. For example if "Graph and Box plot" are selected then only the documents that
   * have both these predictions in their top k will be returned.
   *
   * @param imageClasses
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
