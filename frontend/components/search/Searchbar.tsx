import { useContext, useEffect, useState } from "react";
import Filepicker from "@/components/search/Filepicker";
import api from "@/api/Api";
import { SearchResultsContext } from "@/contexts/SearchContext";
import { FileSelectContext } from "@/contexts/FileSelectContext";

function SearchBar({ classifications }: { classifications: string[] }) {
  const [searchText, setSearch] = useState<string>("");
  const searchResults = useContext(SearchResultsContext);
  const fileSelect = useContext(FileSelectContext);

  function handleSearchChange(e: any) {
    // If the user has changed the textbox value then
    // erase the file.
    fileSelect.setFileData(undefined);
    setSearch(e.target.value);
  }

  useEffect(() => {
    if (classifications.length > 0) {
      searchNow();
    } else if ( classifications.length == 0 && searchText.length > 0) {
      if (fileSelect.fileData != undefined ) {
        api
          .ImageSearchImage(fileSelect.fileData, [])
          .then((response) => {
            searchResults.setSearchData(response.data);
          });
      } else {
        api.ImageSearchText(searchText, []).then((response) => {
          searchResults.setSearchData(response.data);
        });
      }
    }
  }, [classifications]);

  function searchNow() {
    if (searchText.trim() == "" && classifications) {
      console.log("Searching By Class");
      console.log(classifications);
      searchByClasses();
    } else if (searchText.trim() != "") {
      if (fileSelect.fileData != undefined) {
        console.log("Image searchng")
        api
          .ImageSearchImage(fileSelect.fileData, classifications)
          .then((response) => {
            searchResults.setSearchData(response.data);
          });
      } else {
        api.ImageSearchText(searchText, classifications).then((response) => {
          searchResults.setSearchData(response.data);
        });
      }
    }
  }

  function searchByClasses() {
    api.ImageSearchClassification(classifications).then((response) => {
      searchResults.setSearchData(response.data);
    });
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
          onChange={handleSearchChange}
          onKeyDown={onKeyDown}
          value={searchText}
        />
        <div className="input-group-append">
          <Filepicker setSearch={setSearch} classifications={classifications} />
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
