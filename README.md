# FindFirst-ImageSearch
Project for finding academic figures. In this project creating an image search engine and 
image simularity search. 

The goal is that a user should be able to query for academic figures are recieve relevant documents to the query. 

### Live Demo: https://findfirst.dev

## Acknowledgements: 
This project is borrowing heavily on others works, I would like to acknowledge these projects where applicable but
broadly state my sources here: 

- https://github.com/radoondas/flask-elastic-image-search 
  - Used for the basis of the indexing and document emmbedding. 
  - Including the setup in this very ReadMe.md.

# Application Setup/Run
## Requirements & Assumptions: 
- Unix Operating System
- Modern CPU (>= ~3.5GHz; Multicore)
- 16GB of RAM
- 32GB of storage (for images)
- Docker & Docker Compose installed on Host.
<details open><summary>Application Setup/Local Execution</summary>

### 0 Step. 
We must set up a Python environment to use scripts for image embeddings. 
```bash
$ cd python/
$ python3 -m venv .venv
$ source .venv/bin/activate
$ pip install -r requirements.txt
```

### 1. Elasticsearch cluster
You can use the docker-compose bundled in the repository, your cluster.
To run the Elasticsearch cluster locally, use the following docker-compose example.
```bash
# Only start up some of the static. 
$ docker-compose up setup es01 es02 kibana
```
Check if the cluster is running using Kibana or `curl` or simple check the containers statuses.

Once the cluster is up and running, get the CA certificate out from the Elasticsearch cluster to use it in the rest of the setup.
```bash
$ docker cp findfirst-es01-1://usr/share/elasticsearch/config/certs/ca/ca.crt ./conf
```

### 2. Generate image embeddings
Generate the image embeddings from the photos. These embeddings will be used for kNN (vector) search in Elasticsearch.

**Put all the photos in to the folder `{workspaceFolder}/data` ** . 
In this environment the following directory structure was used:
```
data/
├── SciFig
│   ├── metadata
│   └── png
└── SciFig-pilot
    ├── algorithms
    ├── architecture diagram
    ├── bar charts
    ├── boxplots
    ├── confusion matrix
    ├── graph
    ├── Line graph_chart
    ├── maps
    ├── metadata
    ├── natural images
    ├── neural networks
    ├── NLP text_grammar_eg
    ├── pareto
    ├── pie chart
    ├── png
    ├── scatter plot
    ├── Screenshots
    ├── tables
    ├── trees
    ├── venn diagram
    └── word cloud
```

```bash
$ cd python/image_embeddings/image_processing
$ python3 create-image-embeddings.py --es_host='https://127.0.0.1:9200' \
  --es_user='elastic' --es_password='changeme' \
  --ca_certs='../../../conf/ca.crt'
```

By default the SciFig-pilot is used, but easy to switch by providing the parameter to the create image script `--data_path <PATH>`

In production data/SciFig is used. Testing and optimization the test dataset SciFig-Pilot is used. 
#### About the data
##### SciFig
- metadata: contianing json about the images that used in the frontend. 
  - Name
  - Caption
- png: Contains dataset of images
##### SciFig-Pilot
- png: contains flattend (combined) images of each subdirectory.
- Labels: The labels for the test set is the directory where the images are located, e.g. `word cloud, trees, etc.` directories. 
```bash
# command used for flattening 
$ cd dataset; mkdir png
$ cd SciFig-pilot;
$ find . -type f -print0 | xargs -0 cp ../png;
$ mv ../png .
```

## 3. Spring Setup
Spring requires that the user have a private and public key before starting this service. 
```bash
$ cd conf
$ chmod u+x createServerKey.sh
$ ./createServerKey.sh
$ ../
```

## 4. Frontend 
Since the application is using the image dataset, NextJS needs those images at compile time. 

```bash
# Either dataset can be used. 
$ cp -r data/SciFig-pilot/png
$ frontend/public/png
```

## Run the Application Stack
```bash
$ docker compose up db frontend backend
```
</details>


# CLIP
As mentioned in https://github.com/openai/CLIP the original zero-shot clip model has been trained on 1.28M labelled examples which allows for the user of the model to be able to do semantic search. This allows the user of the model to provide an a sentence in the from of string characters to the model and recieve and vector representing the string. The same is true for providing an image, such png or jpeg, to the model. 

This allows the user to use the vector data to compare texts to and image, and use the image in search engines such as elastic search which can use  k-nearest-neighbors (KNN) search. 

<details><summary>More about CLIP and research</summary>

## CLIP SentenceTransformers
There is one problem with this, while model itself provides adequate results for general text to image, and image to image translation e.g. "cats on a bed", it doesn't perform well for tasks of classification on labels. Using the zero shot model in the application of classifying images on 19 different labels from ACL Academic Figure set revealed that there is glaring bias to certian classifications. For example, when the figure has more text, such as captions included, the model assumes that the figure contains data about Natural Language Processing (N.L.P.).

# References 
<details>
<summary>References</summary>
- 

</details>

</details>

