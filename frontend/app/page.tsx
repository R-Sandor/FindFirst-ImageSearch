"use client";
import "./main.css";
import SearchBar from "@/components/search/Searchbar";
import { Badge } from "react-bootstrap";
import { Prediction } from "@/types/Bookmarks/FigureData";
import { useContext, useEffect, useState } from "react";
import {
  SearchResultProvider,
  SearchResultsContext,
} from "@/contexts/SearchContext";
const IMAGE_DIR = process.env.IMAGE_DIR;
const catagories = [
    "Algorithms",
    "Architecture Diagram",
    "Bar charts",
    "Box Plots",
    "Confusion Matrix",
    "Graph",
    "Line Graph Chart",
    "Geographical Maps",
    "Natural Images",
    "Neural Networks",
    "NLP text grammar",
    "Pareto",
    "Pie chart",
    "Scatter Plot",
    "Screenshots",
    "Tables",
    "Trees",
    "Venn Diagram",
    "Word Cloud",
  ];

function imagePath(path: string): string {
  // use the specified directory or default
  console.log(path)
  // path = path + ".png"
  return IMAGE_DIR != (undefined || null)
    ? IMAGE_DIR + "/" + path
    : "png/" + path;
}

// cleans up messy labels from the data
function cleanLabel(label: string) {
  switch (label) {
    case "natural language processing":
      return "NLP";
    case "natural language processing grammar":
      return "NLP Grammar";
    default:
      return label;
  }
}

function makeBadge(predictions: Prediction[]): JSX.Element {
  // if it is a table just return the one prediction, we know it's a table from the metadata.
  let confidenceStlye: string[] = [];
  if (predictions[0].label === "Table") {
    confidenceStlye.push("badge-primary");
    return (
      <Badge>
        {" "}
        {predictions[0].label}:
        {predictions[0].confidence}%
      </Badge>
    );
  }
  predictions.forEach((prediction: Prediction) => {
    if (parseFloat(prediction.confidence) > 80) {
      confidenceStlye.push("bg-primary");
    } else if (parseFloat(prediction.confidence) > 50) {
      confidenceStlye.push("bg-warning");
    } else {
      confidenceStlye.push("bg-secondary");
    }
  });

  return (
    <div>
      <Badge className={"mr-3 " + confidenceStlye[0]}>
        {cleanLabel(predictions[0].label)}:
        {predictions[0].confidence}%
      </Badge>
      <Badge className={"mr-3 " + confidenceStlye[1]}>
        {cleanLabel(predictions[1].label)}:
        {predictions[1].confidence}%
      </Badge>
      <Badge className={"mr-3 " + confidenceStlye[2]}>
        {cleanLabel(predictions[2].label)}:
        {predictions[2].confidence}%
      </Badge>
    </div>
  );
}

export default function App() {
  const [checkedState, setCheckedState] = useState(
    new Array(catagories.length).fill(false));
  const [checkedLbl,setCheckedLbl] = useState<string[]>([])
  function classSearch() {
    return checkedState.flatMap((v, i) => { 
      console.log(v)
      if (v == true ) {
        console.log(i)
        return i;
      }
      return []
    })
  }

  useEffect(() => {
    console.log(checkedState)
  }, [checkedState])

  const handleOnChange = (position: number) => {
    console.log(position)
    setCheckedState(checkedState.map((item, index) =>
      index === position ? !item : item
    ))
    let lbls = checkedState.flatMap((v, i) => { 
      console.log(v)
      if (v == true ) {
        console.log(i)
        return catagories[i];
      }
      return []
    })
    setCheckedLbl(lbls)
    console.log(checkedLbl)
  }
  

  return (
    <SearchResultProvider>
      <div className="row">
        <div className="row">
          <SearchBar classifications={checkedLbl}/>
        </div>
        <div className="col-2">
          <div className="ml-6 features">
            Figure Types:
            {catagories.map((val, i) => {
              return (
                <div key={i} className="form-check">
                  <input
                    className="ml-3 form-check-input"
                    type="checkbox"
                    value=""
                    id="flexCheckChecked"
                    onChange={() => handleOnChange(i)}
                  />
                  <label
                    className="form-check-label"
                    htmlFor="flexCheckChecked"
                  >
                    {val}
                  </label>
                </div>
              );
            })}
          </div>
        </div>
        <MainSearchResults/>
      </div>
    </SearchResultProvider>
  );
}

function MainSearchResults() {
  const searchResults = useContext(SearchResultsContext);
  // console.log(searchResults.searchData)
  return (
    <div className="col-9">
          <div className="row">
            {searchResults.searchData.map((card, i) => {
              // console.log(card);
              return (
                <div key={i} className="card mr-10 cstyle">
                  <img
                    className="card-img"
                    src={imagePath(card.path)}
                    alt="Card image cap"
                  />
                  <div className="card-body">
                    <h5 className="card-title">{card.path}</h5>
                    <p id="text" className="card-text">
                      {card.caption}
                    </p>
                  </div>
                  <div className="card-footer text-muted">
                    {makeBadge(card.predictions)}
                  </div>
                </div>
              );
            })}
          </div>
    </div>
  );
}
