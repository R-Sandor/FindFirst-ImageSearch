from sanic import Sanic
from sanic.log import logger
from sanic.response import json
from cors import add_cors_headers
# import torch
from sentence_transformers import SentenceTransformer
print(__name__)

model = ""
app = Sanic(__name__)

if __name__ == "__main__":
    app.run()
    model = SentenceTransformer('clip-ViT-B-32')

# Fill in CORS headers
app.register_middleware(add_cors_headers, "response")


# model = ""

def encode_query(query: str):
    features = model.encode([query])
    return features


@app.get("/")
async def search():
    search_term = request.args.get('search', 'dogs playing in the snow')
    text_features = encode_query(search_term)
    print(search_term)
    print(text_features)
    return json(text_features)


# if __name__ == '__main__':
#     app.run(host='0.0.0.0', port=8000)



if __name__ == '__mp-server__':
#     print("cool this is running")
# #     # ctx = mp.get_context('spawn')
# #     # q = ctx.Queue()
# #     # p = ctx.Process(target=foo, args=(q,))
# #     # p.start()
# #     # print(q.get())
# #     # p.join() 
#     model = SentenceTransformer('clip-ViT-B-32')
    logger.info("here")
