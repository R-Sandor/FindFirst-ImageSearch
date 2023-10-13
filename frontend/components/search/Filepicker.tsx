// adapted from https://github.com/Jaaneek/useFilePicker
import api from "@/api/Api";
import { SearchResultsContext } from "@/contexts/SearchContext";
import { FigureData } from "@/types/Bookmarks/FigureData";
import { Dispatch, SetStateAction, useContext } from "react";
import { useFilePicker } from "use-file-picker";
import {
  FileAmountLimitValidator,
  FileSizeValidator,
  ImageDimensionsValidator,
} from "use-file-picker/validators";

export default function FilePicker(pickerProp: {setSearch:Dispatch<SetStateAction<string>>}) {
  const searchResults  = useContext(SearchResultsContext);
  const { openFilePicker, filesContent, loading, errors } = useFilePicker({
    readAs: "DataURL",
    accept: "image/*",
    multiple: true,
    validators: [
      new FileAmountLimitValidator({ max: 1 }),
      new FileSizeValidator({ maxFileSize: 50 * 1024 * 1024 /* 50 MB */ }),
      new ImageDimensionsValidator({
        maxHeight: 3200, // in pixels
        maxWidth: 3200,
        minHeight: 1,
        minWidth: 1,
      }),
    ],
    onFilesSuccessfullySelected: ({ plainFiles, filesContent }) => {
      // this callback is called when there were no validation errors
      pickerProp.setSearch(plainFiles[0].name)
      api.ImageSearchImage(plainFiles[0]).then((response) => {
          searchResults.setSearchData(response.data)
      });
    },
  });


  if (loading) {
    return <div>Loading...</div>;
  }

  if (errors.length) {
    {console.log(errors)}
    return <div>Error...</div>;
  }
  if (filesContent) {
  }
  return (
    <div>
      <i
        className="bi bi-camera "
        onClick={() => openFilePicker()}
      ></i>
      <br />
    </div>
  );
}
