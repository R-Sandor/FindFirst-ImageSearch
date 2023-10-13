import { FigureData } from "@/types/Bookmarks/FigureData";
import { Dispatch, SetStateAction, createContext, useState } from "react";

export interface SearchState {
  searchData: FigureData[];
  setSearchData: Dispatch<SetStateAction<FigureData[]>>;
}

export const SearchResultsContext = createContext<SearchState>({
  searchData: [],
  setSearchData: () => {},
});

export function SearchResultProvider({
  children,
}: {
  children: React.ReactNode;
}) {
  const [searchResults, setSearch] = useState<FigureData[]>([]);

  return (
    <SearchResultsContext.Provider
      value={{ searchData: searchResults, setSearchData: setSearch }}
    >
      {children}
    </SearchResultsContext.Provider>
  );
}
