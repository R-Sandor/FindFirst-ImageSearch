package dev.findfirst.imagesearch.controller;

import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import dev.findfirst.bookmarkit.utility.Response;
import dev.findfirst.imagesearch.model.AcademicImage;
import dev.findfirst.imagesearch.service.ImageSearchService;
import java.io.IOException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/imagesearch")
@Slf4j
public class ImageSearchController {

  @Autowired ImageSearchService imageService;

  @GetMapping("/id")
  public ResponseEntity<AcademicImage> getImageById(@RequestParam String id) {
    return new Response<AcademicImage>(imageService.findById(id)).get();
  }

  @PostMapping("/text")
  public ResponseEntity<List<AcademicImage>> textSearch(@RequestParam("text") String text) {
    return new Response<List<AcademicImage>>(imageService.findByQuery(text, 10), HttpStatus.OK)
        .get();
  }

  @PostMapping("/image")
  public ResponseEntity<List<AcademicImage>> handleFileUpload(
      @RequestParam("image") MultipartFile file) {
    try {
      return new Response<List<AcademicImage>>(
              imageService.findSimilarImages(file, 10), HttpStatus.OK)
          .get();
    } catch (Exception e) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
  }

  @GetMapping("/class")
  public ResponseEntity<List<AcademicImage>> classSearch(@RequestParam("class") String imageClass) {
    try {
      return new Response<List<AcademicImage>>(
              imageService.findTopResultsforImageClass(imageClass, 10), HttpStatus.OK)
          .get();
    } catch (ElasticsearchException | IOException e) {
      log.error("error while querying for {}, error: {}", imageClass, e);
      return new Response<List<AcademicImage>>(null, HttpStatus.BAD_REQUEST).get();
    }
  }
}
