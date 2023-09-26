package dev.findfirst.bookmarkit.repository.elastic;

import dev.findfirst.bookmarkit.model.AcademicImage;
import java.util.Optional;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface AcademicImageRepository extends ElasticsearchRepository<AcademicImage, String> {
  Optional<AcademicImage> findById(String id);
}
