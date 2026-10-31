Embeddings and Vector Search · Language: Python
## 1. Nearest Neighbors

Vector search finds data whose embeddings are closest to a query embedding. Instead of comparing exact words, it compares numerical vectors representing semantic meaning. A nearest-neighbor search returns the vectors most similar to a given query, usually ranked by cosine similarity, Euclidean distance, or dot product.

The simplest approach is exact search, which compares the query against every stored vector. This works well for small datasets but becomes expensive when searching millions of embeddings. The number of returned results is usually controlled by `k`, such as the five most similar documents.

Illustrative 2D embedding space. 

![[Pasted image 20261030145239.png]]

### Python example: exact similarity search

We'll use the same embedding model from the previous lesson.

```python
import numpy as np
from sentence_transformers import SentenceTransformer

model = SentenceTransformer("all-MiniLM-L6-v2")

documents = [
    "Spring Boot is used for Java backend development",
    "PostgreSQL is a relational database",
    "Docker packages applications into containers",
    "Python is popular for machine learning",
    "Java supports building REST APIs"
]

vectors = model.encode(
    documents,
    normalize_embeddings=True
)

query = "How do I create a backend API with Java?"

query_vector = model.encode(
    query,
    normalize_embeddings=True
)

# Cosine similarity for normalized vectors
scores = vectors @ query_vector

k = 3
top_indices = np.argsort(scores)[::-1][:k]

for index in top_indices:
    print(f"{scores[index]:.3f} - {documents[index]}")
```

Result:

```txt
0.578 - Java supports building REST APIs
0.570 - Spring Boot is used for Java backend development
0.240 - Docker packages applications into containers
```

This implementation compares the query against every document, so its search cost grows linearly with the number of stored vectors.

## 2. Vector Indexing

### ANN

Approximate Nearest Neighbor (ANN) search improves performance by avoiding exhaustive comparisons. Instead of guaranteeing the mathematically closest vectors, ANN algorithms aim to find sufficiently close results much faster, trading a small amount of accuracy for speed.
### HNSW

Hierarchical Navigable Small World (HNSW) is a popular ANN indexing algorithm. It organizes vectors into a multilayer graph, allowing searches to navigate toward nearby vectors without scanning the entire dataset. HNSW is supported by vector-search systems such as FAISS and pgvector.

Conceptual HNSW graph:

![[Pasted image 20261030150649.png]]

Simplified illustration of multiple graph layers. Actual HNSW graphs contain neighbor connections determined by vector proximity, not a regular sequence.

### Python example: FAISS

Install FAISS:

```sh
pip install faiss-cpu
```

Create `ann_search.py`:

```python
import faiss
import numpy as np
from sentence_transformers import SentenceTransformer

model = SentenceTransformer("all-MiniLM-L6-v2")

documents = [
    "Java supports REST API development",
    "Spring Boot simplifies backend development",
    "PostgreSQL stores relational data",
    "Docker runs applications in containers",
    "Python is used for machine learning"
]

vectors = model.encode(
    documents,
    normalize_embeddings=True
).astype("float32")

dimensions = vectors.shape[1]

# HNSW approximate index
index = faiss.IndexHNSWFlat(
    dimensions,
    16,  # M: graph connectivity
    faiss.METRIC_INNER_PRODUCT
)

index.hnsw.efConstruction = 40
index.hnsw.efSearch = 32

index.add(vectors)

query = "How can I build backend services with Java?"

query_vector = model.encode(
    [query],
    normalize_embeddings=True
).astype("float32")

scores, indices = index.search(query_vector, k=3)

for score, idx in zip(scores[0], indices[0]):
    if idx != -1:
        print(f"{score:.3f} - {documents[idx]}")
```

Result:

```txt
0.554 - Java supports REST API development
0.454 - Spring Boot simplifies backend development
0.144 - Docker runs applications in containers
```

Because the vectors are normalized, inner product corresponds to cosine similarity.

Two important HNSW parameters are `M`, which controls graph connectivity, and `efSearch`, which controls how broadly the graph is explored during retrieval. Higher values generally improve recall but increase memory usage or search cost.

For this small dataset, exact search would be simpler. HNSW becomes useful when your document collection grows substantially.

## 3. Metadata Filtering

Vector similarity alone is not always enough. Applications often need to restrict search results by metadata such as category, language, owner, document type, or visibility.

For example, an application might search only documents belonging to the current user and some category. Metadata filtering ensures that only eligible documents are considered or returned. In a production multi-user application, user ownership and visibility must be enforced by the backend, not trusted from user-provided filters.
### Python example

Create `metadata_search.py`:

```python
import numpy as np
from sentence_transformers import SentenceTransformer

model = SentenceTransformer("all-MiniLM-L6-v2")

documents = [
    {
        "text": "Spring Boot creates REST APIs",
        "category": "Java"
    },
    {
        "text": "FastAPI builds Python APIs",
        "category": "Python"
    },
    {
        "text": "Java supports microservices",
        "category": "Java"
    }
]

vectors = model.encode(
    [doc["text"] for doc in documents],
    normalize_embeddings=True
)

query = "How to build an API?"
query_vector = model.encode(
    query,
    normalize_embeddings=True
)

scores = vectors @ query_vector

# Restrict results to Java documents
results = [
    (float(scores[i]), doc["text"])
    for i, doc in enumerate(documents)
    if doc["category"] == "Java"
]

results.sort(reverse=True)

for score, text in results:
    print(f"{score:.3f} - {text}")
```

Result:

```txt
0.376 - Spring Boot creates REST APIs
0.167 - Java supports microservices
```

This example performs filtering in Python. In a vector database, filtering can be integrated with retrieval, depending on the database and index.

## 4. Keyword Search vs Semantic Search

Keyword search matches literal terms, often using algorithms such as BM25 to rank documents by term frequency and relevance. It works especially well for exact identifiers, error messages, API names, and technical terminology.

Semantic search compares embeddings and can retrieve related documents even when the query uses different vocabulary. However, it may miss exact identifiers or confuse closely related technical concepts. Combining both approaches is called hybrid search, which we'll study in RAG Optimization.

Compare search strategies:

**Conceptual query**

![[Pasted image 20261030151631.png]]

**Exact term query**

![[Pasted image 20261030151651.png]]

### Python example: keyword vs semantic search

Create `keyword_vs_semantic.py`:

```python
import numpy as np
from sentence_transformers import SentenceTransformer

documents = [
    "Spring Boot simplifies REST API development",
    "FastAPI builds Python web APIs",
    "Docker runs applications in containers",
    "PostgreSQL supports relational queries"
]

query = "How do I create a web service?"

# Basic keyword overlap
query_words = set(query.lower().split())

keyword_scores = [
    len(query_words & set(doc.lower().split()))
    for doc in documents
]

# Semantic similarity
model = SentenceTransformer("all-MiniLM-L6-v2")

vectors = model.encode(
    documents,
    normalize_embeddings=True
)

query_vector = model.encode(
    query,
    normalize_embeddings=True
)

semantic_scores = vectors @ query_vector

print("Keyword ranking:")
for i in np.argsort(keyword_scores)[::-1]:
    print(keyword_scores[i], documents[i])

print("\nSemantic ranking:")
for i in np.argsort(semantic_scores)[::-1]:
    print(f"{semantic_scores[i]:.3f}", documents[i])
```

Result:

```txt
Keyword ranking:
1 FastAPI builds Python web APIs
0 PostgreSQL supports relational queries
0 Docker runs applications in containers
0 Spring Boot simplifies REST API development

Semantic ranking:
0.224 FastAPI builds Python web APIs
0.165 Spring Boot simplifies REST API development
0.077 Docker runs applications in containers
0.006 PostgreSQL supports relational queries
```

The keyword implementation is intentionally basic; production keyword search generally uses tokenization, normalization, and BM25 rather than simple word overlap.

## 5. Key concepts to remember

| Concept            | Meaning                                                         |
| ------------------ | --------------------------------------------------------------- |
| Nearest neighbor   | Vector closest to a query under a distance or similarity metric |
| Exact search       | Compares the query against all candidate vectors                |
| ANN                | Faster approximate nearest-neighbor retrieval                   |
| HNSW               | Graph-based ANN indexing algorithm                              |
| Top K              | Maximum number of nearest results requested                     |
| Metadata filtering | Restricting results by document attributes                      |
| Keyword search     | Retrieval based on lexical matches                              |
| Semantic search    | Retrieval based on embedding similarity                         |
| Hybrid search      | Combining lexical and semantic retrieval                        |