from flask import Flask, request 
from flask_cors import cross_origin
from sentence_transformers import SentenceTransformer
from PIL import Image

app = Flask(__name__)

model = SentenceTransformer('clip-ViT-B-32')

def encode_query(query):
    features = model.encode(query)
    return features

def encodingToJson(encodings): 
    return {'image_embeddings':  encodings.tolist() }
    

@app.route('/', methods=['GET'])
async def search():
    # do image search
    if 'file' not in request.files:
        print("file not request.files")
        search_term = request.get_json()
        search_term = search_term['query'] 
        return encodingToJson(encode_query(search_term))
    else:
        file = request.files['file']
        print(file.filename)
        if file.filename == '':
            print("No record")
            return "Record not found", 400
        return encodingToJson(encode_query(Image.open(file)))