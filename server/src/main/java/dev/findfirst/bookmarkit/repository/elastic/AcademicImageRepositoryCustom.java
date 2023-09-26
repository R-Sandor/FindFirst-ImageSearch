package dev.findfirst.bookmarkit.repository.elastic;

public interface AcademicImageRepositoryCustom {
  public String queryByEmbeddings(double[] embeddings);
}
