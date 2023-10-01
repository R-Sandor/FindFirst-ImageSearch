package dev.findfirst.imagesearch.repository;

public interface AcademicImageRepositoryCustom {
  public String queryByEmbeddings(double[] embeddings);
}
