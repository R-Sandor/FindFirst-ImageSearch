import pandas as pd
import matplotlib.pyplot as plt
import matplotlib.image as mpimg
from PIL import Image, ImageFile
import sklearn
from tqdm import tqdm
from datetime import datetime
from exif import Image as exifImage
from sentence_transformers import SentenceTransformer
from sklearn.metrics import confusion_matrix, classification_report

import torch

device = "cuda" if torch.cuda.is_available() else "cpu"
labels = [ 
    "algorithm",
    "architecture diagram",
    "bar chart",
    "box plot",
    "confusion matrix",
    "graph",
    "line graph chart",
    "geographical map",
    "natural image",
    "neural network diagram",
    "natural language processing grammar",
    "pareto chart",
    "pie chart",
    "scatter plot",
    "screenshot",
    "table",
    "tree diagram",
    "venn diagram",
    "word cloud"
] 
tkns = ["image of a "+ label for label in labels]
predicted = []

def main():
    img_model = SentenceTransformer('clip-ViT-B-32')
    # Labelled Data
    df  = pd.read_csv('pilot_data.csv')
    act_value = df['label']
    img_filepaths = df['filepath']
    # List to hold the predictions on each of the images.
    
    for fp in tqdm(img_filepaths, desc='Predicting on images', total=len(img_filepaths)):
        image = Image.open(fp)
        embedding = img_model.encode(image, convert_to_tensor=True)
        make_prediction(embedding, img_model)
        
    cr = classification_report(act_value, predicted, labels=labels)
    print(cr)

def make_prediction(image_features, model):
    with torch.no_grad():
        text_features = model.encode(tkns, convert_to_tensor=True)

    # Pick the top 3 most similar labels for the image
    image_features /= image_features.norm(dim=-1, keepdim=True)
    text_features /= text_features.norm(dim=-1, keepdim=True)
    similarity = (100.0 * image_features @ text_features.T).softmax(dim=-1)
    values, index =  similarity.topk(1)
    predicted.append(labels[index.item()])


def image_embedding(image, model):
    return model.encode(image)

if __name__ == '__main__':
    main()