## 1. Cosine similiarity

Cosine similarity measures the angle between two vectors, rather than their absolute distance. A value close to 1 indicates vectors pointing in similar directions, 0 indicates orthogonal vectors, and -1 indicates opposite directions. With text embeddings, higher cosine similarity generally indicates greater semantic relatedness, although the score is not a calibrated probability of matching meaning.

The formula is:

\[ \operatorname{sim}(A,B)=\frac{A\cdot B}{\|A\|\|B\|} \]

For example, if two vectors point in the same direction, their cosine similarity is 1. If they are perpendicular, their similarity is 0.

![[Pasted image 20261030120640.png]]

### Python example

Create `similarity.py`:

```python
import numpy as np
from sentence_transformers import SentenceTransformer

model = SentenceTransformer("all-MiniLM-L6-v2")

sentences = [
    "Java is a programming language",
    "Java is used to build software",
    "I enjoy hiking in the mountains"
]

# Normalized embeddings
vectors = model.encode(
    sentences,
    normalize_embeddings=True
)

# Cosine similarity = dot product for unit vectors
def similarity(a, b):
    return float(np.dot(a, b))

print("Java vs Software:",
      similarity(vectors[0], vectors[1]))

print("Java vs Hiking:",
      similarity(vectors[0], vectors[2]))

```

The first pair should generally have a higher similarity score than the second. Exact values depend on the embedding model.

```txt
Java vs Software: 0.8076908588409424
Java vs Hiking:   0.041172679513692856
```

## 2. Semantic search

Now let's use embeddings to find the most relevant document for a question. This is the basic mechanism behind semantic retrieval, which we'll later use in RAG.

Create `search.py`:

```python
import numpy as np
from sentence_transformers import SentenceTransformer

model = SentenceTransformer("all-MiniLM-L6-v2")

documents = [
    "Spring Boot simplifies Java backend development",
    "PostgreSQL is a relational database",
    "Docker packages applications into containers",
    "Python is widely used for machine learning"
]

# Embed documents once
doc_vectors = model.encode(
    documents,
    normalize_embeddings=True
)

query = "How can I develop backend applications with Java?"

# Embed the search query
query_vector = model.encode(
    query,
    normalize_embeddings=True
)

# Compare query against all documents
scores = np.dot(doc_vectors, query_vector)

# Rank from most similar to least similar
ranked = np.argsort(scores)[::-1]

for index in ranked:
    print(f"{scores[index]:.3f} - {documents[index]}")

```

The Spring Boot document should generally rank highest because it is semantically related to Java backend development.

```txt
0.611 - Spring Boot simplifies Java backend development
0.240 - Docker packages applications into containers
0.171 - PostgreSQL is a relational database
0.150 - Python is widely used for machine learning
```

This is a simple in-memory vector search. There is no vector database yet. In the next subcategories, we'll explore efficient nearest-neighbor search and eventually store these vectors in PostgreSQL with pgvector.

## 3. Key concepts to remember

|Concept|Meaning|
|---|---|
|Embedding|Numerical representation of data|
|Embedding model|Model that converts text into vectors|
|Dimension|Number of values in a vector|
|Semantic similarity|Relatedness based on meaning|
|Cosine similarity|Similarity based on vector direction|
|Semantic search|Retrieving documents by vector similarity|
