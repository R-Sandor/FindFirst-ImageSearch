"use client";
import "./main.css";
import  SearchBar  from "@/components/search/Searchbar";
import UseAuth from "@components/UseAuth";
import { Badge } from "react-bootstrap";
import { FigureData } from "@/types/Bookmarks/FigureData";
import { useEffect, useState } from "react";
const IMAGE_DIR = process.env.IMAGE_DIR; 

function imagePath(path:string): string {
  // use the specified directory or default
  return IMAGE_DIR != (undefined || null) ? IMAGE_DIR+"/"+path  : "png/"+ path
}

export default function App() {
  const userAuth = UseAuth();
  const catagories = [
    "Algorithms",
    "Architecture/Pipeline diagrams",
    "Bar charts",
    "Box Plots",
    "Confusion Matrix",
    "Graph",
    "Line Chart",
    "Maps",
    "Natural Images",
    "Neural Networks",
    "NLP rules/grammar",
    "Pie chart",
    "Scatter Plot",
    "Screenshots",
    "Tables",
    "Trees",
    "Pareto chart",
    "Venn Diagram",
    "Word Cloud",
  ];

  const[cardData, setCardData] = useState<FigureData[]>([])

  useEffect(() => {
    console.log(cardData)
  }, [cardData])

  return (
    <div className="row">
      <div className="row">
        <SearchBar setCardData={setCardData} searchResults={cardData}/> 
      </div>
      <div className="col-2">
        <div className="ml-6 features">
          Figure Types:
          {
            catagories.map((val, i) => {
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
            console.log(card)
            return (
              <div key={i} className="card mr-10 cstyle">
                <img
                  className="card-img"
                  src={imagePath(card.relativePath)}
                  alt="Card image cap"
                />
                <div className="card-body">
                  <h5 className="card-title">{card.title}</h5>
                  <p className="card-text">{card.pdf}</p>
                  {/* <a href="#" className="btn btn-primary">
                    Go somewhere
                  </a> */}
                </div>
                  <div className="card-footer text-muted">
                    <Badge> {card.figure}</Badge>
                  </div>
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
}
