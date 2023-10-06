package dev.findfirst.imagesearch.utility;

import dev.findfirst.imagesearch.service.TorchService.Predictions;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
@Slf4j
public record MetaData(
    String type, String figName, String caption, Path filePath, Predictions predictions) {
  public MetaData {
    // canonicaol constructor sets the file path and prediction.
    if (filePath != null) {
      var fileName = filePath.getFileName();
      var tmpfp = filePath.getParent().getParent().resolve("png");
      log.debug("temp file path {}", tmpfp);
      filePath = Paths.get(tmpfp.toString(), fileName.toString()  + type + figName + ".png");
    }
  }

  public MetaData(Map m, Path filePath) {
    this(getFigure(m), m.get("name"), getCaption(m), filePath);
  }

  public MetaData(Object t, Object f, Object c, Path filePath) {
    this((String) t, (String) f, (String) c, filePath, null);
  }

  public static String findAttr(Map m, String... opts) {
    for (var i = 0; i < opts.length; i++) {
      var v = m.get(opts[i]);
      if (v != null) return (String) v;
    }
    return "";
  }

  private static String getCaption(Map m) {
    return findAttr(m, "caption_text", "caption", "text");
  }

  private static String getFigure(Map m) {
    return findAttr(m, "figType", "figure_type");
  }

}
