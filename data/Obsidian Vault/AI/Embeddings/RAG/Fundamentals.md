Essential · Java / Spring Boot
## 1. What is RAG

Retrieval-Augmented Generation (RAG) is a technique that improves LLM responses by retrieving relevant information from external sources before generating an answer.

A traditional LLM answers questions using knowledge learned during training and information provided in its prompt. RAG extends this by retrieving relevant information from documents, databases, or knowledge bases. The retrieved information becomes part of the prompt, allowing the model to answer questions using data that was not included in its training.

For example, we could use RAG to answer questions about a user's uploaded Markdown notes. Instead of sending an entire document to OpenAI, the application retrieves only the relevant sections and provides them as context.

Simplified RAG workflow: retrieval provides context for generation.

![[Pasted image 20261031091903.png]]

## 2. RAG vs Fine-Tuning

RAG and fine-tuning solve different problems. RAG provides external knowledge at request time, while fine-tuning changes a model's behavior through additional training.

|Feature|RAG|Fine-tuning|
|---|---|---|
|Purpose|Provide knowledge|Adapt model behavior|
|Updates|Update documents|Retrain model|
|External database|Usually required|Not required|
|Model weights|Unchanged|Modified|
|Source citations|Easier to support|Not inherent|
|Best for|Documentation, knowledge bases|Style, formats, specialized tasks|

For example, RAG is generally the better choice for answering questions about uploaded Markdown files. Documents can change frequently, and users have different knowledge bases. Fine-tuning would not be an efficient way to maintain that information.

The two techniques can also be combined: a fine-tuned model can use RAG to retrieve current information.

## 3. Retrieval and Generation

A RAG application usually has two separate processes: indexing, which prepares documents for searching, and retrieval and generation, which answers user questions.

**Indexing** — when documents are uploaded

```json
Markdown -> Chunks -> Embeddings -> pgvector
```

**Retrieval and generation** — when a question is asked

```json
Question -> Search -> Context -> LLM answer
```

During indexing, documents are divided into smaller chunks. An embedding model converts each chunk into a numerical vector, which is stored in a vector database such as PostgreSQL with pgvector.

When a user asks a question, the application embeds the question using the same embedding model, searches for similar document chunks, and adds the retrieved text to the LLM prompt. The LLM then generates an answer using that context. Importantly, the LLM does not normally query the vector database directly; the application handles retrieval.

## 4. RAG Architecture

A typical Java/Spring Boot RAG application contains the following components.

|Component|Responsibility|
|---|---|
|Document Reader|Loads Markdown, PDF, or text|
|Text Splitter|Divides documents into chunks|
|Embedding Model|Converts text into vectors|
|Vector Store|Stores and searches embeddings|
|Retriever|Finds relevant chunks|
|Prompt Builder|Combines question and context|
|Chat Model|Generates the answer|

Spring AI provides abstractions for many of these components, including `DocumentReader`, `TokenTextSplitter`, `EmbeddingModel`, `VectorStore`, and `ChatClient`.

### Practical Example — Spring Boot

We'll use a simple RAG service that searches previously indexed documents in PostgreSQL with pgvector.

This example assumes the documents have already been indexed. We'll implement document loading, chunking, and indexing in the next lesson.

`RagService.java`

```java
package com.example.rag;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

@Service
public class RagService {

    private final VectorStore vectorStore;
    private final ChatClient chatClient;

    public RagService(
            VectorStore vectorStore,
            ChatClient.Builder builder) {

        this.vectorStore = vectorStore;
        this.chatClient = builder.build();
    }

    public String ask(String question) {

        // 1. Retrieve relevant documents
        List<Document> documents = vectorStore
            .similaritySearch(
                SearchRequest.builder()
                    .query(question)
                    .topK(3)
                    .build()
            );

        // 2. Build context
        String context = documents.stream()
            .map(Document::getText)
            .collect(Collectors.joining("\n\n"));

        // 3. Generate answer
        return chatClient.prompt()
            .user(u -> u.text("""
                Answer using only the context below.
                If the context is insufficient,
                say you don't know.

                Context:
                {context}

                Question:
                {question}
                """)
                .param("context", context)
                .param("question", question))
            .call()
            .content();
    }
}
```

`RagController.java`

```java
package com.example.rag;

import com.example.rag.RagService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rag")
public class RagController {

    private final RagService ragService;

    public RagController(RagService ragService) {
        this.ragService = ragService;
    }

    @GetMapping("/ask")
    public String ask(@RequestParam String question) {
        return ragService.ask(question);
    }
}
```

Test the endpoint:

```sh
curl -G http://localhost:8087/api/rag/ask \
  --data-urlencode "question=What is MCP?"
```

Response:

```txt
I don't know — the provided context does not mention or define MCP.
```

Test another:

```sh
curl -G http://localhost:8087/api/rag/ask \
  --data-urlencode "question=How can I build backend services with Java?"
```

Response:

```txt
Use Spring Boot to build Java REST APIs — Spring Boot simplifies Java REST API development. For persistent storage you can use PostgreSQL, a relational database.
```

The flow is:

1. `VectorStore` retrieves the three most similar chunks.
2. The application combines them into a context string.
3. `ChatClient` sends the question and context to the LLM.
4. The model generates an answer grounded in the retrieved text.
    
For production use, the retrieval must also filter by the authenticated user's document ownership and permissions. A similarity search without that restriction could expose another user's content.

Also, prompt instructions reduce hallucinations but cannot guarantee that every generated statement is supported by the documents.

## 5. Key Concepts to Remember

- RAG = Retrieval + Augmented Prompt + Generation.
- Embedding models find semantically relevant information; chat models generate answers.
- Vector databases store embeddings and support similarity search.
- RAG does not retrain the LLM.
- Indexing occurs when knowledge is prepared; retrieval happens when questions are asked.