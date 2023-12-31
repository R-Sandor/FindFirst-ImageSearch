package dev.findfirst.bookmarkit.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.findfirst.bookmarkit.annotations.IntegrationTestConfig;
import dev.findfirst.bookmarkit.model.Bookmark;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

// @DataJpaTest
@IntegrationTestConfig
public class BookmarkServiceUnitTest {

  @Autowired private BookmarkService bookmarkService;

  @Test
  public void whenApplicationStarts_thenHibernateCreatesInitialRecords() {
    List<Bookmark> bookmarks = bookmarkService.list();
    assertEquals(bookmarks.size(), 2);
  }
}
