import sys
sys.path.append('../')
from flask import Flask, request
from flask_cors import cross_origin
from PIL import Image
import torch
import clip
import numpy 
from TorchUtil import encode_image_query, encode_query, encodingToJson
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
@app.route('/predictimage', methods=['POST'])
async def predict_image():
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
        record["confidence"]= round(100 * value.item(),2)
        predictions.append(record)
        print(f"{labels[index]:>16s}: {100 * value.item():.2f}%")
    return { 
        "predictions": predictions
    } 


# This function handles making the confidence level of the categories.
@app.route('/predictembedding', methods=['POST'])
## TODO: Not sure if the embbedings can be used
async def predict_embedding():
    json = request.get_json()
    image_features = json['imageEmbedding']
    print(image_features)
    with torch.no_grad():
        text_features = model.encode_text(text_inputs)
        # Pick the top 3 most similar labels for the image
    image_features = numpy.asarray(image_features)    
    image_features = torch.Tensor(image_features).float().to(device)
    text_features /= text_features.norm(dim=-1, keepdim=True)
    similarity = (100.0 * image_features @ text_features.T).softmax(dim=-1)
    values, indices = similarity[0].topk(3)

    # Print the result
    print("\nTop predictions:\n")
    predictions = []
    for value, index in zip(values, indices):
        record = {}
        record["label"] = labels[index]
        record["confidence"]= round(100 * value.item(),2)
        predictions.append(record)
        print(f"{labels[index]:>16s}: {100 * value.item():.2f}%")
    return { 
        predictions
    } 
