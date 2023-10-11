export interface FigureData {
  imageId: string;
  caption: string;
  predictions: Predictions;
  relativePath: string;
  figure: string;
}
export interface Predictions {
  predictions: Prediction[]
}

export interface Prediction { 
  label: string, 
  confidence: string
}