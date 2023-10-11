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

@Document(indexName = "academic-images")
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AcademicImage {
  @Id private String id;

  @Field(name = "image_id", type = FieldType.Keyword)
  @JsonAlias("image_id")
  private String imageId;

  @Field(name = "image_name", type = FieldType.Keyword)
  @JsonAlias("image_name")
  private String imageName;

  private String caption;

  @Field(name = "relative_path", type = FieldType.Keyword)
  @JsonAlias("relative_path")
  private String relativePath;

  @Field(name = "image_embedding", type = FieldType.Dense_Vector, similarity = "cosine", index = true)
  @JsonAlias("image_embedding")
  private double[] imageEmbeddings;

  @Field(name = "predictions", type = FieldType.Object)
  @JsonAlias("predictions")
  private Predictions predictions;
}
