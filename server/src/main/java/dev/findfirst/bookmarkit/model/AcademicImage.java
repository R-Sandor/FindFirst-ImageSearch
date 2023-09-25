package dev.findfirst.bookmarkit.model;

import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import org.springframework.data.annotation.Id;

import lombok.Data;

@Data
@Document(indexName = "academic-images")
public class AcademicImage {
    @Id
    private String id; 
    
    @Field(name="image_name", type = FieldType.Keyword)
    private String imageName;

    @Field(name="relative_path", type = FieldType.Keyword)
    private String relativePath;
}
