package dev.findfirst.imagesearch.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dev.findfirst.imagesearch.service.TorchService.Predictions;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Similarity;

@Document(indexName = "academic-images")
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AcademicImage {

  @Id private String id;

  // @Field(name = "image_name", type = FieldType.Keyword)
  // @JsonAlias("image_name")
  // private String imageName;

  private String caption;

  private String imagename;

  @Field(
      name = "embedding",
      type = FieldType.Dense_Vector,
      similarity = Similarity.BM25,
      index = true)
  private double[] embedding;

  @Field(name = "predictions", type = FieldType.Object)
  private Predictions predictions;
}
