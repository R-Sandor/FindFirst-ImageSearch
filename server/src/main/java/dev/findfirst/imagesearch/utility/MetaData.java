package dev.findfirst.imagesearch.utility;

import java.util.Map;

record MetaData(String type, String figName, String caption) {
  public MetaData(Map m) {
    this(getFigure(m), m.get("name"), getCaption(m));
  }

  public MetaData(Object t, Object f, Object c) {
    this((String) t, (String) f, (String) c);
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
