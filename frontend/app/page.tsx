"use client";
import "./main.css";
import SearchBar from "@/components/search/Searchbar";
import UseAuth from "@components/UseAuth";
import { Badge } from "react-bootstrap";
import {
  Predictions,
  Prediction,
  FigureData,
} from "@/types/Bookmarks/FigureData";
import { useEffect, useState } from "react";
const IMAGE_DIR = process.env.IMAGE_DIR;

function imagePath(path: string): string {
  // use the specified directory or default
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

function makeBadge(predictions: Predictions): JSX.Element {
  // if it is a table just return the one prediction, we know it's a table from the metadata.
  let confidenceStlye: string[] = [];
  if (predictions.predictions[0].label === "Table") {
    confidenceStlye.push("badge-primary")
    return (
      <Badge>
        {" "}
        {predictions.predictions[0].label}:
        {predictions.predictions[0].confidence}%
      </Badge>
    );
  }
  predictions.predictions.forEach((prediction) => {
    if (parseFloat(prediction.confidence) > 80)  {
      confidenceStlye.push("bg-primary")
    } else if (parseFloat(prediction.confidence) > 50) {
      confidenceStlye.push("bg-warning")
    } else {
      confidenceStlye.push("bg-secondary")
    }
  })

  return (
    <div>
      <Badge className={"mr-3 " + confidenceStlye[0]}>
        {cleanLabel(predictions.predictions[0].label)}:
        {predictions.predictions[0].confidence}%
      </Badge>
      <Badge className={"mr-3 " + confidenceStlye[1]}>
        {cleanLabel(predictions.predictions[1].label)}:
        {predictions.predictions[1].confidence}%
      </Badge>
      <Badge className={"mr-3 " + confidenceStlye[2]}>
        {cleanLabel(predictions.predictions[2].label)}:
        {predictions.predictions[2].confidence}%
      </Badge>
    </div>
  );
}

export default function App() {
  const userAuth = UseAuth();
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

  const [cardData, setCardData] = useState<FigureData[]>([]);

  useEffect(() => {
    console.log(cardData);
  }, [cardData]);

  return (
    <div className="row">
      <div className="row">
        <SearchBar setCardData={setCardData} searchResults={cardData} />
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
                />
                <label className="form-check-label" htmlFor="flexCheckChecked">
                  {val}
                </label>
              </div>
            );
          })}
        </div>
      </div>
      <div className="col-9">
        <div className="row">
          {cardData.map((card, i) => {
            console.log(card);
            return (
              <div key={i} className="card mr-10 cstyle">
                <img
                  className="card-img"
                  src={imagePath(card.relativePath)}
                  alt="Card image cap"
                />
                <div className="card-body">
                  <h5 className="card-title">{card.imageId}</h5>
                  <p id="text" className="card-text">{card.caption}</p>
                  {/* <a href="#" className="btn btn-primary">
                    Go somewhere
                  </a> */}
                </div>
                <div className="card-footer text-muted">
                  {makeBadge(card.predictions)}
                </div>
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
}
