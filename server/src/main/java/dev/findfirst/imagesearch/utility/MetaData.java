package dev.findfirst.imagesearch.utility;

import dev.findfirst.imagesearch.service.TorchService.Predictions;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public record MetaData(
    String documentID,
    String type,
    String figName,
    String caption,
    Path jsonPath,
    Path imagePath,
    Predictions predictions) {
  public MetaData {
    // canonicaol constructor sets the file path and prediction.
    var fileName = jsonPath.getFileName();
    var tmpfp = jsonPath.getParent().getParent().resolve("png");
    log.debug("temp file path {}", tmpfp);
    documentID = makeDocID(fileName, type, figName);
    log.debug(documentID);
    imagePath = Paths.get(tmpfp.toString(), documentID + ".png");
  }

  public boolean imageExists() {
    return this.imagePath.toFile().exists();
  }

  public MetaData(Map m, Path jsonPath) {
    this("", getFigure(m), m.get("name"), getCaption(m), jsonPath, null);
  }

  public MetaData(String documentId, Object t, Object f, Object c, Path jsonPath, Path imagePath) {
    this(documentId, (String) t, (String) f, (String) c, jsonPath, null, null);
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

  /**
   * Utility function to get the documentID.
   *
   * @param prefix the prefix of the document; Obtained by JSON filename.
   * @param metaData the actual MetaData to get figure, name.
   * @return documentID
   */
  private String makeDocID(Path jsonPath, String type, String figName) {
    var prefix = jsonPath.getFileName().toString().split("deepfigures*")[0] + ".pdf-";
    return prefix + type + figName;
  }
}
