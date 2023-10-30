import { useContext, useState } from "react";
import Filepicker from "@/components/search/Filepicker";
import api from "@/api/Api";
import { SearchResultsContext } from "@/contexts/SearchContext";

function SearchBar({ classifications }: { classifications: string[] }) {
  const [searchText, setSearch] = useState<string>("");
  const searchResults = useContext(SearchResultsContext);

  function handleChange(e: any) {
    setSearch(e.target.value);
  }

  function searchNow() {
    console.log("searching: ", searchText);
    api.ImageSearchText(searchText).then((response) => {
      searchResults.setSearchData(response.data);
    });
    // console.log(searchResults.searchData);
  }

  function searchByClasses(classifications: string[]) {
    console.log("searching: ", searchText);
    api.ImageSearchClassification(searchText).then((response) => {
      searchResults.setSearchData(response.data);
    });
    // console.log(searchResults.searchData);
  }

  function onKeyDown(e: any) {
    const { keyCode } = e;
    if (keyCode == 13) {
      if (searchText.trim() == "" && classifications) {
        console.log("Searching By Class")
        searchByClasses(classifications)
      }
      else if (searchText.trim() != "")
        searchNow();
    }
  }

  return (
    <div className="pt-5 pb-5 half-width-form-control">
      <div className="input-group">
        <input
          type="search"
          className="form-control"
          placeholder="Describe your figure!"
          aria-label="Search"
          aria-describedby="search-addon"
          onChange={handleChange}
          onKeyDown={onKeyDown}
          value={searchText}
        />
        <div className="input-group-append">
          <Filepicker setSearch={setSearch} />
          <button
            type="button"
            onClick={() => searchNow()}
            className="search-btn btn btn-outline-secondary"
          >
            search
          </button>
        </div>
      </div>
    </div>
  );
}
export default SearchBar;
