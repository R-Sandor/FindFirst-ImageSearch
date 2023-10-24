package dev.findfirst.imagesearch.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dev.findfirst.imagesearch.service.TorchService.Prediction;
import dev.findfirst.imagesearch.utility.MetaData;
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

  public void setMetadata(MetaData metaData) {
    this.id = metaData.documentID();
    this.caption = metaData.caption();
    this.predictions = metaData.predictions().predictions();
  }

  private String caption;

  private String imagename;

  private String path;

  @Field(name = "embedding", type = FieldType.Dense_Vector, similarity = "cosine", index = true)
  private double[] embedding;

  // @Field(name = "predictions", type = FieldType.Nested)
  private Prediction predictions[];
}
