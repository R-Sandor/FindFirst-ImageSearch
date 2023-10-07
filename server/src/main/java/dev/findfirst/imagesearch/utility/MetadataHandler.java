package dev.findfirst.imagesearch.utility;

import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import dev.findfirst.imagesearch.service.ImageSearchService;
import dev.findfirst.imagesearch.service.TorchService;
import dev.findfirst.imagesearch.service.TorchService.Prediction;
import dev.findfirst.imagesearch.service.TorchService.Predictions;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class MetadataHandler {

  private final TorchService torch;
  private final ImageSearchService imageSearchService;

  public void updateMetadata(Path jsonMetadDirectory) {
    Map<String, MetaData> figureMetaData = new HashMap<>();
    try (var jsonFiles = Files.list(jsonMetadDirectory)) {
      // Iterate over each JSON file and get the image metadata.
      jsonFiles
          .parallel()
          .limit(1)
          .forEach(
              jsonPath -> {
                figureMetaData.putAll(readJSON(jsonPath));
              });

      figureMetaData.values().stream()
          .limit(1)
          .forEach(
              md -> {
                try {
                  imageSearchService.updateImage(md);
                } catch (ElasticsearchException | IOException e) {
                  // TODO Auto-generated catch block
                  e.printStackTrace();
                }
              });
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public Map<String, MetaData> readJSON(Path p) {
    if (p.toFile().exists() && p.toFile().isFile()) {
      log.info("file exists: {}", p.toFile().exists());
      log.info("Reading fom {}", p);
      try {
        // We know that the file size is adequate and do not need to use lazy list.
        var map = new JacksonJsonParser().parseMap(Files.readString(p));

        // Three types of figures for figure data. Look for the formatted first.
        // Ignore if there is already a match that is not empty.
        var formattedFigures = (List<Map>) map.get("figures");
        // Raw types have two types of figures: raw and regionless.
        var rawTypes = (Map) map.get("raw_pdffigures_output");
        // List of RawFigures MetaData.
        var rawFigListMap = (List<Map>) rawTypes.get(("figures"));
        // List of regionless  captioned figures.
        var regionless = (List<Map>) rawTypes.get(("regionless-captions"));

        // Used map to handle key collision with merge function.
        var figuresMetada =
            Stream.of(formattedFigures, rawFigListMap, regionless)
                .flatMap(listOfMap -> listOfMap.stream())
                .filter(m -> m != null) // Skipping nulls
                .map(
                    mapEntry -> {
                      return new MetaData(mapEntry, p);
                    })
                .collect(
                    Collectors.toMap(
                        MetaData::documentID,
                        Function.identity(),
                        (x, y) -> {
                          return (!x.figName().equals("") && !x.type().equals("")) ? x : y;
                        }));

        this.addPredictionsToMetaData(figuresMetada);
        return figuresMetada;
      } catch (IOException e) {
        log.error(e.toString());
      }
    }
    // file does not exist return an empty list as this will not cause exeception.
    return new HashMap<String, MetaData>();
  }

  private void addPredictionsToMetaData(Map<String, MetaData> figures) {
    // Predict the figures types.
    var figuresMetaData = this.makePredictions(figures.values().stream());
    figuresMetaData.forEach(newData -> updateMap(figures, newData));
  }

  public void updateMap(Map<String, MetaData> figMap, MetaData newMetaData) {
    figMap.put(newMetaData.documentID(), newMetaData);
  }

  /**
   * Calls Pytorch microservice on those documents that do not have a figure type. The highest
   * confidence of the document types (barchart, scatterplot, etc) is then saved to the metadata
   * then latter to the document for facet search
   *
   * @param stream figures to make predictions on.
   * @return List of MetaData with predictions.
   */
  private List<MetaData> makePredictions(Stream<MetaData> stream) {
    return stream
        .map(
            md -> {
              return md.type().equals("Table")
                  ? new MetaData(
                      md.documentID(),
                      md.type(),
                      md.figName(),
                      md.caption(),
                      md.jsonPath(),
                      md.imagePath(),
                      new Predictions(new Prediction[] {new Prediction("Table", "100")}))
                  : new MetaData(
                      md.documentID(),
                      md.type(),
                      md.figName(),
                      md.caption(),
                      md.jsonPath(),
                      md.imagePath(),
                      torch.predict(md));
            })
        .toList();
  }
}
