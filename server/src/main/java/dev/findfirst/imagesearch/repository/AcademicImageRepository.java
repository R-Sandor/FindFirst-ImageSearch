package dev.findfirst.imagesearch.repository;

import dev.findfirst.imagesearch.model.AcademicImage;
import java.util.Optional;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface AcademicImageRepository extends ElasticsearchRepository<AcademicImage, String> {
  Optional<AcademicImage> findById(String id);

  Optional<AcademicImage> findByImageId(String id);
}
