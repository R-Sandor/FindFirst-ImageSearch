import { useContext, useEffect, useState } from "react";
import Filepicker from "@/components/search/Filepicker";
import api from "@/api/Api";
import { SearchResultsContext } from "@/contexts/SearchContext";

function SearchBar({ classifications }: { classifications: string[] }) {
  const [searchText, setSearch] = useState<string>("");
  const searchResults = useContext(SearchResultsContext);

  function handleChange(e: any) {
    setSearch(e.target.value);
  }

  useEffect(() => {
    if (classifications.length > 0) {
      searchNow()
    }
  }, [classifications]);

  function searchNow() {
    if (searchText.trim() == "" && classifications) {
      console.log("Searching By Class")
      console.log(classifications)
      searchByClasses()
    }
    else if (searchText.trim() != "") {
      api.ImageSearchText(searchText).then((response) => {
        searchResults.setSearchData(response.data);
      });
    }
  }

  function searchByClasses() {
    console.log("searching by class: ", classifications);
    api.ImageSearchClassification(classifications).then((response) => {
      searchResults.setSearchData(response.data);
    })
  }


  function onKeyDown(e: any) {
    const { keyCode } = e;
    if (keyCode == 13) {
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
