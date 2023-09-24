from flask import Flask, request,  jsonify
from flask_cors import cross_origin
from sentence_transformers import SentenceTransformer
import pandas as pd

app = Flask(__name__)


model = SentenceTransformer('clip-ViT-B-32')

def hello():
    return 'Hello World!'


def encode_query(query: str):
    features = model.encode([query])
    return features

@app.route('/')
async def search():
    search_term = request.args.get('search', 'dogs playing in the snow')
    text_features =  encode_query(search_term)
    array_list = text_features.tolist()
    return {'image_embeddings':  array_list[0] }