package dev.findfirst.bookmarkit.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@Document(indexName = "academic-images")
public class AcademicImage {
  @Id private String id;

  @Field(name = "image_id", type = FieldType.Keyword)
  private String imageId;

  @Field(name = "image_name", type = FieldType.Keyword)
  private String imageName;

  @Field(name = "relative_path", type = FieldType.Keyword)
  private String relativePath;
}
