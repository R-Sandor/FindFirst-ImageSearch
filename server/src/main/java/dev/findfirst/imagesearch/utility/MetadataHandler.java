package dev.findfirst.imagesearch.utility;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
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

        for (var f : figures.values()) System.out.println(f);

        this.saveMetaDataToDb(figures.values(), p);
      } catch (IOException e) {
        log.error(e.toString());
      }
    }
  }

  private void saveMetaDataToDb(Collection<MetaData> figures, Path jsonPath) {
    var fname = jsonPath.getFileName();
    log.info("filename {}", fname);

    /// Split the file name before deep figure

  }
}
