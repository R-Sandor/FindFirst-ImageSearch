# Borrowed from https://github.com/radoondas/flask-elastic-image-search/blob/main/image_embeddings/create-image-embeddings.py
# Starting template.
import os
import sys
import glob
import time
import json
import argparse
from sentence_transformers import SentenceTransformer
from elasticsearch import Elasticsearch, SSLError
from elasticsearch.helpers import parallel_bulk
from PIL import Image
from tqdm import tqdm
from datetime import datetime
from exif import Image as exifImage
import torch

ES_HOST = "https://127.0.0.1:9200/"
ES_USER = "elastic"
ES_PASSWORD = "changeme"
ES_TIMEOUT = 3600

DEST_INDEX = "academic-images"
DELETE_EXISTING = True
CHUNK_SIZE = 100

# Sample set
#PATH_TO_IMAGES = "../../frontend/public/*.png"
#PREFIX="../../frontend/public/"

PATH_TO_IMAGES = "../../../data/SciFig-pilot/png/*.png" 

PREFIX = "../../../data/SciFig-pilot/png/"

CA_CERT='../../../conf/ca.crt'

parser = argparse.ArgumentParser()
parser.add_argument('--es_host', dest='es_host', required=False, default=ES_HOST,
                    help="Elasticsearch hostname. Must include HOST and PORT. Default: " + ES_HOST)
parser.add_argument('--data-path', dest='data_path', required=False, default=PATH_TO_IMAGES,
                    help="Dataset file location. Default: " + PATH_TO_IMAGES)
parser.add_argument('--es_user', dest='es_user', required=False, default=ES_USER,
                    help="Elasticsearch username. Default: " + ES_USER)
parser.add_argument('--es_password', dest='es_password', required=False, default=ES_PASSWORD,
                    help="Elasticsearch password. Default: " + ES_PASSWORD)
parser.add_argument('--verify_certs', dest='verify_certs', required=False, default=True,
                    action=argparse.BooleanOptionalAction,
                    help="Verify certificates. Default: True")
parser.add_argument('--thread_count', dest='thread_count', required=False, default=4, type=int,
                    help="Number of indexing threads. Default: 4")
parser.add_argument('--chunk_size', dest='chunk_size', required=False, default=CHUNK_SIZE, type=int,
                    help="Default: " + str(CHUNK_SIZE))
parser.add_argument('--timeout', dest='timeout', required=False, default=ES_TIMEOUT, type=int,
                    help="Request timeout in seconds. Default: " + str(ES_TIMEOUT))
parser.add_argument('--delete_existing', dest='delete_existing', required=False, default=True,
                    action=argparse.BooleanOptionalAction,
                    help="Delete existing indices if they are present in the cluster. Default: True")
parser.add_argument('--ca_certs', dest='ca_certs', required=False, default=CA_CERT,
                    help="Path to CA certificate.") # Default: ../app/conf/ess-cloud.cer")
parser.add_argument('--extract_GPS_location', dest='gps_location', required=False, default=False,
                    action=argparse.BooleanOptionalAction,
                    help="[Experimental] Extract GPS location from photos if available. Default: False")

args = parser.parse_args()
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
tkns = ["image of a "+ label for label in labels]

def make_prediction(img, model):
    with torch.no_grad():
        image_features = model.encode(img, convert_to_tensor=True)
        text_features = model.encode(tkns, convert_to_tensor=True)

    # Pick the top 3 most similar labels for the image
    image_features /= image_features.norm(dim=-1, keepdim=True)
    text_features /= text_features.norm(dim=-1, keepdim=True)
    similarity = (100.0 * image_features @ text_features.T).softmax(dim=-1)
    values, indices =  similarity.topk(3)
    predictions = []
    for value, index in zip(values, indices):
        record = {}
        record["label"] = labels[index]
        record["confidence"]= round(100 * value.item(),2)
        predictions.append(record)
    return predictions


def main():
    global args
    lst = []

    start_time = time.perf_counter()
    img_model = SentenceTransformer('clip-ViT-B-32')
    duration = time.perf_counter() - start_time
    print(f'Duration load model = {duration}')

    filenames = glob.glob(args.data_path, recursive=True)
    start_time = time.perf_counter()
    for filename in tqdm(filenames, desc='Processing files', total=len(filenames)):
        image = Image.open(filename)

        doc = {}
        embedding = image_embedding(image, img_model)
        doc['_id'] = create_image_id(filename)
        doc['embedding'] = embedding.tolist()
        doc['path'] = os.path.relpath(filename).split(PREFIX)[1]
        doc['predictions'] = make_prediction(image, img_model) 

        lst.append(doc)

    duration = time.perf_counter() - start_time
    print(f'Duration creating image embeddings = {duration}')

    es = Elasticsearch(hosts=ES_HOST)
    if args.ca_certs:
        es = Elasticsearch(
            hosts=[args.es_host],
            verify_certs=args.verify_certs,
            basic_auth=(args.es_user, args.es_password),
            ca_certs=args.ca_certs
        )
    else:
        es = Elasticsearch(
            hosts=[args.es_host],
            verify_certs=args.verify_certs,
            basic_auth=(args.es_user, args.es_password)
        )

    es.options(request_timeout=args.timeout)

    # index name to index data into
    index = DEST_INDEX
    try:
        with open("image-embeddings-mappings.json", "r") as config_file:
            config = json.loads(config_file.read())
            if args.delete_existing:
                if es.indices.exists(index=index):
                    print("Deleting existing %s" % index)
                    es.indices.delete(index=index, ignore=[400, 404])

            print("Creating index %s" % index)
            es.indices.create(index=index,
                              mappings=config["mappings"],
                              settings=config["settings"],
                              ignore=[400, 404],
                              request_timeout=args.timeout)


        count = 0
        for success, info in parallel_bulk(
                client=es,
                actions=lst,
                thread_count=4,
                chunk_size=args.chunk_size,
                timeout='%ss' % 120,
                index=index
        ):
            if success:
                count += 1
                if count % args.chunk_size == 0:
                    print('Indexed %s documents' % str(count), flush=True)
                    sys.stdout.flush()
            else:
                print('Doc failed', info)

        print('Indexed %s documents' % str(count), flush=True)
        duration = time.perf_counter() - start_time
        print(f'Total duration = {duration}')
        print("Done!\n")
    except SSLError as e:
        if "SSL: CERTIFICATE_VERIFY_FAILED" in e.message:
            print("\nCERTIFICATE_VERIFY_FAILED exception. Please check the CA path configuration for the script.\n")
            raise
        else:
            raise


def image_embedding(image, model):
    return model.encode(image)


def create_image_id(filename):
    # print("Image filename: ", filename)
    return os.path.splitext(os.path.basename(filename))[0]

def get_exif_date(filename):
    with open(filename, 'rb') as f:
        image = exifImage(f)
        taken = f"{image.datetime_original}"
        date_object = datetime.strptime(taken, "%Y:%m:%d %H:%M:%S")
        prettyDate = date_object.isoformat()
        return prettyDate

def get_exif_location(filename):
    with open(filename, 'rb') as f:
        image = exifImage(f)
        exif = {} 
        lat = dms_coordinates_to_dd_coordinates(image.gps_latitude, image.gps_latitude_ref)
        lon = dms_coordinates_to_dd_coordinates(image.gps_longitude, image.gps_longitude_ref)
        return [lon, lat]


def dms_coordinates_to_dd_coordinates(coordinates, coordinates_ref):
    decimal_degrees = coordinates[0] + \
                      coordinates[1] / 60 + \
                      coordinates[2] / 3600
    
    if coordinates_ref == "S" or coordinates_ref == "W":
        decimal_degrees = -decimal_degrees
    
    return decimal_degrees

if __name__ == '__main__':
    main()