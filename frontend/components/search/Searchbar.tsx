import { Dispatch, SetStateAction, useState } from "react";
import { FigureData } from "@/types/Bookmarks/FigureData";
import api from "@/api/Api";



function SearchBar({ searchResults, setCardData }: { searchResults: FigureData[], setCardData:  Dispatch<SetStateAction<FigureData[]>>}) {
  const[searchText, setSearch] = useState<string>("");
  function handleChange(e: any) {
    setSearch(e.target.value);
    console.log(e.target.value);
  }
  
  function searchNow() {
    console.log("searching now")
    api.ImageSearchText(searchText).then((response) => {
      searchResults = response.data;
      setCardData(searchResults)
      console.log(searchResults)
    })
  }

  return (
    <div className="pt-5 pb-5 half-width-form-control">
      <div className="input-group">
        <input
          type="search"
          className="form-control rounded"
          placeholder="Describe your figure!"
          aria-label="Search"
          aria-describedby="search-addon"
          onChange={handleChange}
          value={searchText}
          
        />
        <button
          type="button"
          onClick={() => searchNow()}
          className="btn btn-outline-primary"
        >
          search
        </button>
      </div>
    </div>
  );
}
export default SearchBar;
