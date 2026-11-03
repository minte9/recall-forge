Embeddings and Vector Search · Language: Python
## 1. What are embeddings?

An embedding is a numerical representation of data, such as text, images, or audio. For text embeddings, a model converts a sentence into a vector of floating-point numbers. Texts with similar meanings generally produce vectors that are close to each other in the embedding space.  

**Example**

For example, "Java is a programming language" and "Java is used to build software" should have relatively similar embeddings, while "I enjoy hiking in the mountains" should be less similar. Unlike keyword matching, embeddings can capture semantic relationships even when sentences use different words.

**Conceptual example — text to vectors**

![[Pasted image 20261030105455.png]]

## 2. Embedding models

An embedding model is a machine learning model trained to produce meaningful vector representations. Unlike a generative language model, which produces text, an embedding model produces numerical vectors suitable for similarity search, clustering, and retrieval.

For learning, we'll use Sentence Transformers with the `all-MiniLM-L6-v2` model. It runs locally, does not require an API key, and generates 384-dimensional vectors. Later, you can use OpenAI embedding models or Spring AI's `EmbeddingModel` abstraction.

### Python setup on Ubuntu

Create a small project and virtual environment:

```

mkdir embeddings-demo
cd embeddings-demo

pyenv install 3.12.16
pyenv local 3.12.16

"$(pyenv prefix)/bin/python" -m venv .venv
source .venv/bin/activate

python --version
python -m pip --version

python -m pip install --upgrade pip
pip install sentence-transformers numpy
```

The first time you run the program, the model weights will be downloaded. Subsequent runs reuse the locally cached model.

## 3. Generating text embeddings

Create `embeddings.py`:

```python
from sentence_transformers import SentenceTransformer

# Load a local embedding model
model = SentenceTransformer("all-MiniLM-L6-v2")

sentences = [
    "Java is a programming language",
    "Java is used to build software",
    "I enjoy hiking in the mountains"
]

# Generate vectors
embeddings = model.encode(sentences)

for sentence, vector in zip(sentences, embeddings):
    print(f"\nText: {sentence}")
    print(f"Dimensions: {len(vector)}")
    print(f"First 5 values: {vector[:5]}")
")
```

Run:

```sh
python embeddings.py
```

Expected output structure (values vary):

```txt
Text: Java is a programming language
Dimensions: 384
First 5 values: [...]

Text: Java is used to build software
Dimensions: 384
First 5 values: [...]

Text: I enjoy hiking in the mountains
Dimensions: 384
First 5 values: [...]
```

Notice that every sentence produces a vector of the same size. The model maps sentences of different lengths into the same 384-dimensional space, making numerical comparison possible.