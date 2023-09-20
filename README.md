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


# 0 Step. 
We must set up a Python environment to use scripts for image embeddings. 
```bash
$ cd flask-elastic-image-search
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
```



### 2. Load NLP models 
Let's load the NLP model into the application. You will use the `eland` client to load the models. For more details, follow the [documentation](https://www.elastic.co/guide/en/elasticsearch/client/eland/current/index.html).

Go **back** in to the main project directory and import the model using Eland docker image.
```bash
$ cd ../
# wait until each model is loaded and started. If you do not have enough memory, you will see errors sometimes confusing
$ eland_import_hub_model --url https://elastic:changeme@127.0.0.1:9200 --hub-model-id sentence-transformers/clip-ViT-B-32-multilingual-v1 --task-type text_embedding --start --ca-certs app/conf/ca.crt
```
For ESS cloud Elasticsearch deployment use bundled CA certificate.
```bash
$ eland_import_hub_model --url https://elastic:<password>@URL:443 --hub-model-id sentence-transformers/clip-ViT-B-32-multilingual-v1 --task-type text_embedding --start --ca-certs app/conf/ess-cloud.cer
```

Example output:
```bash
eland_import_hub_model --url https://elastic:changeme@127.0.0.1:9200 --hub-model-id sentence-transformers/clip-ViT-B-32-multilingual-v1 --task-type text_embedding --start --ca-certs app/conf/ca.crt
2022-12-12 13:40:52,308 INFO : Establishing connection to Elasticsearch
2022-12-12 13:40:52,327 INFO : Connected to cluster named 'image-search-8.6.0' (version: 8.6.0)
2022-12-12 13:40:52,328 INFO : Loading HuggingFace transformer tokenizer and model 'sentence-transformers/clip-ViT-B-32-multilingual-v1'
/Users/rado/pmm_workspace/ml-nlp-demo/flask-elastic-image-search/.venv/lib/python3.9/site-packages/transformers/models/distilbert/modeling_distilbert.py:217: TracerWarning: torch.tensor results are registered as constants in the trace. You can safely ignore this warning if you use this function to create tensors out of constant variables that would be the same every time you call this function. In any other case, this might cause the trace to be incorrect.
  mask, torch.tensor(torch.finfo(scores.dtype).min)
2022-12-12 13:41:03,032 INFO : Creating model with id 'sentence-transformers__clip-vit-b-32-multilingual-v1'
2022-12-12 13:41:03,050 INFO : Uploading model definition
100%|█████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████| 129/129 [00:42<00:00,  3.01 parts/s]
2022-12-12 13:41:45,902 INFO : Uploading model vocabulary
2022-12-12 13:41:46,120 INFO : Starting model deployment
2022-12-12 13:41:52,825 INFO : Model successfully imported with id 'sentence-transformers__clip-vit-b-32-multilingual-v1'
```

![](docs/img/models.png)

If you see on the screen that some models are missing and you see a message `ML job and trained model synchronization required`, go ahead and click the link to synchronize models.

![](docs/img/model-sync.png)