package dev.findfirst.bookmarkit.repository.elastic;
import org.springframework.data.domain.Page;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import dev.findfirst.bookmarkit.model.AcademicImage;
import java.util.List;
import java.util.Optional;


interface AcademicImageRespository extends ElasticsearchRepository<AcademicImage, String> {
    Optional<AcademicImage> findById(String id);
}