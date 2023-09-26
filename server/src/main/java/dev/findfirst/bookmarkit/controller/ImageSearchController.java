package dev.findfirst.bookmarkit.controller;

import dev.findfirst.bookmarkit.model.AcademicImage;
import dev.findfirst.bookmarkit.service.ImageSearchService;
import dev.findfirst.bookmarkit.utility.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/imagesearch")
public class ImageSearchController {

  @Autowired ImageSearchService imageService;

  @RequestMapping("/id")
  public ResponseEntity<AcademicImage> getImageById(@RequestParam String id) {
    return new Response<AcademicImage>(imageService.findById(id)).get();
  }
}
