import Bookmark from "@/types/Bookmarks/Bookmark";
import BookmarkCard from "./BookmarkCard";
import NewBookmarkCard from "./NewBookmarkCard";
import { useBookmarks } from "@/contexts/BookmarkContext";
import { useEffect, useState } from "react";
import UseAuth from "@/components/UseAuth";
import api from "@/api/Api";
import BookmarkAction from "@/types/Bookmarks/BookmarkAction";


// Bookmark group composed of Bookmarks.
export default function BookmarkGroup() {
  const bookmarks = useBookmarks();
  const userAuth = UseAuth();
  const [loading, setLoading] = useState(true);

  // Grab the data.
  useEffect(() => {
    let b: Bookmark[] = [];
    if (userAuth) {
      console.log(bookmarks);
      api.getAllBookmarks().then((results) => {
        for (let bkmk of results.data) {
          console.log(bkmk);
          let bkmkAction: BookmarkAction = {
            type: "add",
            bookmark: bkmk,
          };
          bookmarks.push(bkmk);
          setLoading(false);
        }
      });
    }
  }, [userAuth]);

  let bookmarkGroup: any = [];
  bookmarks.map((b, i) => {
    bookmarkGroup.push(
      <div key={i} className="col-6 col-md-12 col-lg-4">
        <BookmarkCard bookmark={b} />
      </div>
    );
  });

  return !loading ? (
    <div>
      <div className="input-group">
        <input
          type="search"
          className="form-control rounded"
          placeholder="Search"
          aria-label="Search"
          aria-describedby="search-addon"
        />
        <button type="button" className="btn btn-outline-primary">
          search
        </button>
      </div>

      <div className="row">
        <div className="col-6 col-md-12 col-lg-4">
          <NewBookmarkCard />
        </div>
        {bookmarkGroup}
      </div>
    </div>
  ) : (
    <p>loading</p>
  );
}