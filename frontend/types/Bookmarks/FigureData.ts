export interface FigureData {
  imagename: string;
  caption: string;
  predictions: Prediction[];
  path: string;
  figure: string;
}

export interface Prediction { 
  label: string, 
  confidence: string
}