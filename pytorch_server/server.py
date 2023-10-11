from flask import Flask, request 
from flask_cors import cross_origin
from PIL import Image
import numpy as np
import torch
import clip
import json

app = Flask(__name__)

device = "cuda" if torch.cuda.is_available() else "cpu"
model, preprocess = clip.load("ViT-B/32", device=device)

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

tkns = ["a photo of a "+ label for label in labels]
text_inputs = clip.tokenize(tkns).to(device)
# Holds the embeddings for each image.
with torch.no_grad():
    text_features = model.encode_text(text_inputs)

def encode_image_query(query):
    features = model.encode_image(query)
    features /= features.norm(dim=-1, keepdim=True)
    return features.tolist()[0]

def encode_query(query: str):
    with torch.no_grad():
        text_encoded = model.encode_text(clip.tokenize(query).to(device))
        text_encoded /= text_encoded.norm(dim=-1, keepdim=True)
        return text_encoded.tolist()[0]

def encodingToJson(encodings): 
    return {'image_embeddings':  encodings }

def nomralizeDP(image_features,  text_embeddings): 
    results = []
    v1 = np.array(image_features)
    # Norm of the matrix
    v1 = v1 / np.linalg.norm(image_features)  # normalized
    for v2 in np.array(text_embeddings):
        results.append(np.dot(v1, (v2 / np.linalg.norm(text_embeddings))))
    return results


@app.route('/', methods=['GET', 'POST'])
async def search():
    # do image search
    if 'file' not in request.files:
        print("file not request.files")
        json = request.get_json()
        search_term = json['query'] 
        encoding = encode_query(search_term)

        return encodingToJson(encoding)
    else:
        file = request.files['file']
        if file.filename == '':
            print("No record")
            return "Record not found", 400
        image = preprocess(Image.open(file)).unsqueeze(0).to(device) 
        return encodingToJson(encode_image_query(image))

# This function handles making the confidence level of the categories.
@app.route('/predict', methods=['POST'])
async def predict():
    file = request.files['file']
    if file.filename == '':
        print("No record")
        return "Record not found", 400
    with torch.no_grad():
        imagePrep = preprocess(Image.open(file)).unsqueeze(0).to(device)
        image_features = model.encode_image(imagePrep)
        text_features = model.encode_text(text_inputs)
        # Pick the top 3 most similar labels for the image
    image_features /= image_features.norm(dim=-1, keepdim=True)
    text_features /= text_features.norm(dim=-1, keepdim=True)
    similarity = (100.0 * image_features @ text_features.T).softmax(dim=-1)
    values, indices = similarity[0].topk(3)

    # Print the result
    print("\nTop predictions:\n")
    predictions = []
    for value, index in zip(values, indices):
        record = {}
        record["label"] = labels[index]
        record["confidence"]= f"{100 * value.item():.2f}"
        predictions.append(record)
        print(f"{labels[index]:>16s}: {100 * value.item():.2f}%")
    return { 
        'predictions': predictions
    } 