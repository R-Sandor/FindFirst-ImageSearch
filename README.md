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

