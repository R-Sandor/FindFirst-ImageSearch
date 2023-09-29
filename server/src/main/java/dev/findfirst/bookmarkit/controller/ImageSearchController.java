package dev.findfirst.bookmarkit.controller;

import dev.findfirst.bookmarkit.model.AcademicImage;
import dev.findfirst.bookmarkit.service.ImageSearchService;
import dev.findfirst.bookmarkit.utility.Response;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/imagesearch")
public class ImageSearchController {

  @Autowired ImageSearchService imageService;

  @RequestMapping("/id")
  public ResponseEntity<AcademicImage> getImageById(@RequestParam String id) {
    return new Response<AcademicImage>(imageService.findById(id)).get();
  }

  @RequestMapping("/text")
  public ResponseEntity<List<AcademicImage>> textSearch(@RequestParam String text) {
    return new Response<List<AcademicImage>>(imageService.findByQuery(text, 10), HttpStatus.OK).get();
  }

  
	@RequestMapping("/image")
	public ResponseEntity<List<AcademicImage>> handleFileUpload(@RequestParam("image") MultipartFile file) {
    try {
      return new Response<List<AcademicImage>>(imageService.findSimilarImages(file, 10), HttpStatus.OK).get();
    } catch (Exception e) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
	}
}
