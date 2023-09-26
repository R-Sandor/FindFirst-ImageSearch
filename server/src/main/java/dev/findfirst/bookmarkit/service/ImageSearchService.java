package dev.findfirst.bookmarkit.service;

import dev.findfirst.bookmarkit.model.AcademicImage;
import dev.findfirst.bookmarkit.repository.elastic.AcademicImageRepository;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ImageSearchService {

  @Autowired AcademicImageRepository imageRepo;

  public Optional<AcademicImage> findById(String id) {
    return imageRepo.findById(id);
  }
}
