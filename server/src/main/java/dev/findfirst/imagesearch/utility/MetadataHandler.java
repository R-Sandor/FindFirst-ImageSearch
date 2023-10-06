package dev.findfirst.imagesearch.utility;

import dev.findfirst.imagesearch.service.TorchService;
import dev.findfirst.imagesearch.service.TorchService.Predictions;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
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

  public void readJSON(Path p) {
    if (p.toFile().exists() && p.toFile().isFile()) {
      log.info("file exists: {}", p.toFile().exists());
      log.info("Reading fom {}", p);
      
      try {
        // We know that the file size is adequate and do not need to use lazy list.
        var map = new JacksonJsonParser().parseMap(Files.readString(p));

        // Three types of figures for figure data. Look for the formatted first.
        // Ignore if there is already a match that is not empty.
        var formattedFigures = (List<Map>) map.get("figures");

        var rawTypes = (Map) map.get("raw_pdffigures_output");
        // List of RawFigures MetaData.
        var rawFigListMap = (List<Map>) rawTypes.get(("figures"));
        // List of regionless  captioned figures.
        var regionless = (List<Map>) rawTypes.get(("regionless-captions"));

        // Used map to handle key collision with merge function.
        var figures =
            Stream.of(formattedFigures, rawFigListMap, regionless)
                .flatMap(listOfMap -> listOfMap.stream())
                .filter(m -> m != null) // Skipping nulls
                .map(
                    mapEntry -> {
                      return new MetaData(mapEntry, Paths.get(prefix(p) ));
                    })
                .collect(
                    Collectors.toMap(
                        (f) -> f.type() + f.figName(),
                        Function.identity(),
                        (x, y) -> {
                          return (!x.figName().equals("") && !x.type().equals("")) ? x : y;
                        }));

        this.saveMetaDataToDb(figures.values(), p);
      } catch (IOException e) {
        log.error(e.toString());
      }
    }
  }

  private void saveMetaDataToDb(Collection<MetaData> figures, Path jsonPath) {
    var figureMap = new HashMap<String, MetaData>();
    figures.stream()
        .forEach(
            metaData -> {
              var imageId = documentID(jsonPath, metaData);
              figureMap.put(imageId, metaData);
            });

    // Predict the figures types.
    var updatedMetaData =
        this.predictFigures(figureMap.values().stream().filter(md -> md.type().equals("Figure")));
    updatedMetaData.forEach(newData -> updateMap(figureMap, newData, jsonPath));

    log.info("imageIds {}", figureMap.values());
  }

  public void updateMap(Map<String, MetaData> figMap, MetaData newMetaData, Path jsonPath) {
    var documentID = documentID(jsonPath, newMetaData);
    log.info("documentID {}", documentID);
    figMap.put(documentID, newMetaData);
  }

  /**
   * Calls the database on those documents that do not have a figure type. The highest confidence of
   * the document types (barchart, scatterplot, etc) is then saved to the metadata then latter to
   * the document for facid search
   *
   * @param stream
   * @return
   */
  private List<MetaData> predictFigures(Stream<MetaData> stream) {
    return stream
        .map(
            md -> {
              Predictions predictions = torch.predict(md);
              return new MetaData(
                  md.type(), md.figName(), md.caption(), md.filePath(), predictions);
            })
        .toList();
  }

  /**
   * Utility function to get the documentID.
   *
   * @param prefix the prefix of the document; Obtained by JSON filename.
   * @param metaData the actual MetaData to get figure, name.
   * @return documentID
   */
  private String documentID(Path jsonPath, MetaData metaData) {
    // Example of what an imageId looks like from the database: P14-2074.pdf-Figure1.
    // Split the file name before deep figure.
    final var dbIDPrefix = prefix(jsonPath);
    log.debug("filename {}", jsonPath.toString());
    log.debug("prefix {}", dbIDPrefix);
    log.debug(Paths.get(dbIDPrefix + metaData.type() + metaData.figName())
        .getFileName().toString());
    return Paths.get(dbIDPrefix + metaData.type() + metaData.figName())
        .getFileName().toString();
  }



  private String prefix(Path fileName) {
    return fileName.toString().split("deepfigures*")[0] + ".pdf-";
  }

}
