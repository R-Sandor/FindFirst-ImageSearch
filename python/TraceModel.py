from transformers import CLIPModel, CLIPProcessor
import torch
from PIL import Image
import requests
model_name = "openai/clip-vit-base-patch32"

model = CLIPModel.from_pretrained(model_name, torchscript=True, return_dict=False)
processor = CLIPProcessor.from_pretrained(model_name)


# put the model on specific gpu device:
# model.to('cuda:0')

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
text_inputs = processor(text=labels, return_tensors="pt")
url = "http://images.cocodataset.org/val2017/000000039769.jpg"
image = Image.open(requests.get(url, stream=True).raw)
image_inputs = processor(images=image, return_tensors="pt")

# inputs = processor(text=labels, images=image, return_tensors="pt", padding=True)

converted = torch.jit.trace_module(model,  {'get_text_features': [text_inputs['input_ids'], text_inputs['attention_mask']],
'get_image_features': [image_inputs['pixel_values']],
'forward': [text_inputs['input_ids'], image_inputs['pixel_values'], text_inputs['attention_mask']]})

torch.jit.save(converted, "cliptext.pt")
