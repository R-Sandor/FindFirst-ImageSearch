package dev.findfirst.bookmarkit.repository;

import dev.findfirst.bookmarkit.model.Tag;
import dev.findfirst.security.tenant.repository.TenantableRepository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource
public interface TagRepository extends TenantableRepository<Tag>, TagRepoCustom {
  @Query("SELECT t FROM Tag t WHERE t.tag_title =?1")
  Optional<Tag> findByTagTitle(String title);

  @Query("SELECT b.tags FROM Bookmark b WHERE b.id =?1")
  List<Tag> findTagsByBookmarkId(long Id);

  //   @Query(
  //   "SELECT new dev.findfirst.bookmarkit.model.TagCntRecord(" +
  //   "new dev.findfirst.bookmarkit.model.Tag(tag.id, tag.title, tag.url), COUNT(tag.id))" +
  //   "FROM tag JOIN bookmark ON bookmark.tag_id" +
  //   "GROUP BY tag.id"
  // )
  // List<TagCntRecord> test();
}
