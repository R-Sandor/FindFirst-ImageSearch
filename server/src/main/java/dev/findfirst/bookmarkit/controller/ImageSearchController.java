package dev.findfirst.bookmarkit.controller;

import dev.findfirst.bookmarkit.model.AcademicImage;
import dev.findfirst.bookmarkit.service.ImageSearchService;
import dev.findfirst.bookmarkit.utility.Response;
import org.springframework.beans.factory.annotation.Autowired;
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
  public ResponseEntity<AcademicImage> textSearch(@RequestParam String text) {
    // TODO figure out how to modify the hit list to a number of records
    imageService.findByQuery(text);
    return null;
  }

  
	@RequestMapping("/image")
	public ResponseEntity<AcademicImage> handleFileUpload(@RequestParam("image") MultipartFile file) {
    try {
      imageService.findSimilarImages(file);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return null;
	}

}
