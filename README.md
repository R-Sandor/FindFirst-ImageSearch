# FindFirst-ImageSearch
Project for finding academic figures. In this project creating an image search engine and 
image simularity search. 

The goal is that a user should be able to query for academic figures are recieve relevant documents to the query. 

## Acknowledgements: 
This project is borrowing heavily on others works, I would like to acknowledge these projects where applicable but
broadly state my sources here: 

- https://github.com/radoondas/flask-elastic-image-search 
  - Used for the basis of the indexing and document emmbedding. 
  - Including the setup in this very ReadMe.md.


### 0 Step. 
We must set up a Python environment to use scripts for image embeddings. 
```bash
$ python3 -m venv .venv
$ source .venv/bin/activate
$ pip install -r requirements.txt
```

### 1. Elasticsearch cluster
You can use the docker-compose bundled in the repository, your cluster.
To run the Elasticsearch cluster locally, use the following docker-compose example.
```bash
$ cd es-docker
$ docker-compose up -d
```
Check if the cluster is running using Kibana or `curl`.

Once the cluster is up and running, let's get the CA certificate out from the Elasticsearch cluster so we can use it in the rest of the setup.
```bash
$ # still in the folder "es-docker"
$ docker cp image-search-86-es01-1://usr/share/elasticsearch/config/certs/ca/ca.crt conf/ca.crt
$ docker compose down 
```

### 2. Generate image embeddings
Your next step is to generate the image embeddings from your photos. These embeddings will be used for kNN (vector) search in Elasticsearch.

**Put all your photos in to the folder `{workspaceFolder}/data` ** . 

**Notes**:
- you can use sub-folders to maintain sane structure for your images
- only png file types were tested
- you need to have hundreds of photos to get best results. If you have only a dozen of them, then vector search in the space you create is minimal, and distances between images (vectors) are very similar.

```bash
$ cd python/image_embeddings/image_processing
$ python3 create-image-embeddings.py --es_host='https://127.0.0.1:9200' \
  --es_user='elastic' --es_password='changeme' \
  --ca_certs='../../../conf/ca.crt'
```
