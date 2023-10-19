export interface FigureData {
  imagename: string;
  caption: string;
  predictions: Predictions;
  path: string;
  figure: string;
}
export interface Predictions {
  predictions: Prediction[]
}

export interface Prediction { 
  label: string, 
  confidence: string
}