package dev.findfirst.imagesearch.service.queries;

import co.elastic.clients.elasticsearch._types.SortMode;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;

public class ImageQueries {

  public static final String ACADEMIC_IMAGES = "academic-images";
  public static final String PREDS = "predictions";
  public static final String PREDS_LBL = "predictions.label";
  public static final String PREDS_CONF = "predictions.confidence";

  public static Query byPredictionType(String imageClass) {
    return MatchQuery.of(m -> m.field(PREDS_LBL).query(imageClass))._toQuery();
  }

  public static SortOptions sortByConfidence(String imageClass) {
    // spotless:off
    return SortOptions.of(ns -> ns 
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
    }
    // spotless:on
}
