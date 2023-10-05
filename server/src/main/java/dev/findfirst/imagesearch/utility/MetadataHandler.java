package dev.findfirst.imagesearch.utility;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MetadataHandler {

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
                .map(MetaData::new)
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
    var fname = jsonPath.getFileName().toString();
    log.info("filename {}", fname);
    final var prefix = fname.split("deepfigures*")[0];
    final var dbIDPrefix = prefix + ".pdf-";
    log.info("prefix {}", prefix);
    // Example of what an imageId looks like from the database: P14-2074.pdf-Figure1.
    // Split the file name before deep figure.
    var figureMap = new HashMap<String, MetaData>();
    figures.stream().forEach( 
      metaData -> {
        var imageId =  documentID(dbIDPrefix, metaData);
        figureMap.put(imageId, metaData);
    });

    // Predict the figures types.
    var updatedMetaData = this.predictFigures(figureMap.values().stream().filter(md -> md.type().equals("Figure")));
    //updatedMetaData.forEach(newData -> figureMap.put(documentID(dbIDPrefix, newData) , newData));

    log.info("imageIds {}", figureMap.values());

  }

  /**
   * Calls the database on those documents that do not have a figure type. 
   * The highest confidence of the document types (barchart, scatterplot, etc) is then 
   * saved to the metadata then latter to the document for facid search 
   * @param stream
   * @return
   */
  private List<MetaData> predictFigures(Stream<MetaData> stream) {

      return null;
  }

  /**
   * Utility function to get the documentID
   * @param prefix the prefix of the document; Obtained by JSON filename.
   * @param metaData the actual MetaData to get figure, name.
   * @return documentID
   */
  private String documentID(String prefix, MetaData metaData) {
    return prefix +  metaData.type() + metaData.figName();
  }

  private void predictFigures(List<String> imageIds) {
  }
}
