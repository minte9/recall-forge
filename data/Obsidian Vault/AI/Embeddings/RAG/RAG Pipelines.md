Essential · Java 21 / Spring Boot / Spring AI
## 1. Document Loading and Chunking

A RAG pipeline begins by loading documents from external sources such as Markdown files, PDFs, databases, or APIs. These documents contain the knowledge that the application will later retrieve when answering questions.

Because documents can be large, they are divided into smaller pieces called chunks. Each chunk is independently converted into an embedding and stored in a vector database. Chunking allows the application to retrieve only the relevant parts of a document instead of sending the entire document to the LLM.

For example, a Markdown file containing 20 sections about Spring Boot could be split into 20 or more chunks, depending on their length.

![[Pasted image 20261031103705.png]]

Spring AI provides `TokenTextSplitter` for dividing documents into smaller chunks.

```java
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;

import java.util.List;

String markdown = """
    # Spring Boot

    Spring Boot simplifies Java application development.

    ## Dependency Injection

    Spring manages objects through its IoC container.

    ## REST Controllers

    REST endpoints can be created using @RestController.
    """;

Document document = new Document(markdown);

TokenTextSplitter splitter = new TokenTextSplitter();

List<Document> chunks = splitter.apply(
    List.of(document)
);
```

The splitter uses token-based limits, not Markdown heading boundaries. Small documents may produce only one chunk.

### 2. Embedding and Indexing Documents

After chunking, each chunk is converted into a numerical vector using an embedding model. These vectors represent the semantic meaning of the text, allowing similar concepts to be found even when different words are used.

Spring AI's `VectorStore` abstraction simplifies this process. When documents are added to a vector store, the implementation generates embeddings and saves them with their content and metadata.

For our application, we'll use PostgreSQL with pgvector, which you already explored in your Embeddings category.

### Practical Project — ragdemo

We'll create a Spring Boot application that loads Markdown documents, indexes them into PostgreSQL, and answers questions using OpenAI.

Project structure

```groovy
ragdemo/
├── compose.yaml
├── build.gradle
├── documents/
│   └── spring-boot.md
└── src/main/
    ├── java/com/example/ragdemo/
    │   ├── RagDemoApplication.java
    │   ├── RagIndexer.java
    │   ├── RagService.java
    │   └── RagController.java
    └── resources/
        └── application.yml
```

### Step 1 — PostgreSQL with pgvector

Create `compose.yaml`:

```yaml
services:
  postgres:
    image: pgvector/pgvector:pg16
    environment:
      POSTGRES_DB: ragdemo
      POSTGRES_USER: app
      POSTGRES_PASSWORD: secret
    ports:
      - "127.0.0.1:5433:5432"
    volumes:
      - ragdata:/var/lib/postgresql/data

volumes:
  ragdata:
```

Start PostgreSQL:

```sh
docker compose -p ragdemo up -d
```

Using an explicit Compose project name avoids conflicts with other projects. If your existing vector demo already uses port `5433`, change the host port to `5434`.

### Step 2 — Spring AI Dependencies

Create a Spring Boot application with Java 21, Spring Web, and the Spring AI OpenAI and pgvector starters.

In `build.gradle`, the relevant dependencies are:

```groovy
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'

    implementation 'org.springframework.ai:spring-ai-starter-model-openai'
    implementation 'org.springframework.ai:spring-ai-starter-vector-store-pgvector'

    implementation 'org.springframework.boot:spring-boot-starter-jdbc'

    runtimeOnly 'org.postgresql:postgresql'
}
```

Use a Spring AI BOM compatible with your chosen Spring Boot version. The code below follows the Spring AI 1.x API style; verify dependency compatibility if using Spring Boot 4.

### Step 3 — Configure Spring AI

Create `application.yml`:

```yaml
server:
  port: 8087

spring:
  datasource:
    url: jdbc:postgresql://localhost:5433/ragdemo
    username: app
    password: secret

  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      chat:
        options:
          model: gpt-4.1-mini
      embedding:
        options:
          model: text-embedding-3-small

    vectorstore:
      pgvector:
        initialize-schema: true
        dimensions: 1536
        distance-type: COSINE_DISTANCE
```

Here, two different OpenAI models serve different purposes:

- `text-embedding-3-small` generates numerical vectors for indexing and retrieval.
- `gpt-4.1-mini` generates natural-language answers using the retrieved context.
    
The chat model is an example configuration; for a new deployment, select a currently supported chat model. The embedding dimension of 1536 corresponds to the default output size of `text-embedding-3-small`.

### Step 4 — Create a Markdown Document

Create `documents/spring-boot.md`:

```markdown
# Spring Boot

Spring Boot is a Java framework that simplifies
building standalone Spring applications.

## Dependency Injection

Spring manages object creation and dependencies
using an Inversion of Control container.

Constructor injection is recommended because
dependencies are explicit and easier to test.

## REST Controllers

@RestController creates REST API controllers.

@GetMapping maps HTTP GET requests to methods.

@PostMapping maps HTTP POST requests to methods.

## Spring Security

Spring Security provides authentication
and authorization for Java applications.
```

### Step 5 — Index the Documents

Create `RagIndexer.java`:

```java
package com.example.ragdemo;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class RagIndexer implements CommandLineRunner {

    private final VectorStore vectorStore;

    public RagIndexer(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Override
    public void run(String... args) throws Exception {

        Path path = Path.of("documents/spring-boot.md");

        String content = Files.readString(path);

        Document document = new Document(
            content,
            Map.of("source", path.toString())
        );

        TokenTextSplitter splitter = new TokenTextSplitter();

        List<Document> chunks = splitter.apply(
            List.of(document)
        );

        vectorStore.add(chunks);

        System.out.println(
            "Indexed " + chunks.size() + " chunks"
        );
    }
}
```

When the application starts:

1. The Markdown document is loaded.
2. The text is split into chunks.
3. Spring AI generates embeddings.
4. The chunks and vectors are stored in PostgreSQL.
    
This is a learning example. Running it repeatedly may insert duplicate chunks because new document IDs are generated. A production implementation should use stable IDs or delete and replace previously indexed chunks.

## 3. Context Retrieval and Prompt Construction

When a user asks a question, the application retrieves the most relevant chunks using vector similarity search.

The retrieved text is then inserted into the prompt as context. This is the augmentation step of RAG: the LLM receives information from external documents in addition to the user's question.

A good RAG prompt instructs the model to answer using the retrieved context and to acknowledge when information is insufficient.

### Step 6 — Create the RAG Service

Create `RagService.java`:

```java
package com.example.ragdemo;

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

        // 1. Retrieve relevant chunks
        List<Document> documents =
            vectorStore.similaritySearch(
                SearchRequest.builder()
                    .query(question)
                    .topK(3)
                    .build()
            );

        // 2. Build context
        String context = documents.stream()
            .map(Document::getText)
            .collect(Collectors.joining("\n\n"));

        // 3. Ask the LLM
        return chatClient.prompt()
            .user(u -> u.text("""
                Answer the question using only
                the provided context.

                If the answer is not present,
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

Notice that `similaritySearch()` does not call the chat model. It uses the embedding model to represent the question and PostgreSQL to find similar vectors. Only the final `ChatClient` call uses the chat model.

### Step 7 — Expose a REST Endpoint

Create `RagController.java`:

```java
package com.example.ragdemo;

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

Run the application with `OPENAI_API_KEY` configured and test:

```sh
curl -G http://localhost:8087/api/rag/ask \
  --data-urlencode "question=What is constructor injection?"
```

Expected answer, approximately:

```txt
Constructor injection is a way of managing dependencies in Spring where 
dependencies are provided through a class constructor. It is recommended 
because dependencies are explicit and easier to test.
```

The exact wording depends on the model.

## 4. Building RAG with Spring AI

Spring AI also provides `QuestionAnswerAdvisor`, which can automate the retrieval and prompt augmentation steps.

Instead of manually calling `similaritySearch()` and constructing the context, you can configure the advisor directly on `ChatClient`.

Add dependency in `build.gradle`

```yml
implementation 'org.springframework.ai:spring-ai-vector-store-advisor'
```

`RagAdvisorServe.java`

```java
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

@Service
public class RagAdvisorService {

    private final ChatClient chatClient;

    public RagAdvisorService(
            ChatClient.Builder builder,
            VectorStore vectorStore) {

        this.chatClient = builder
            .defaultAdvisors(
                QuestionAnswerAdvisor.builder(vectorStore)
                    .build()
            )
            .build();
    }

    public String ask(String question) {
        return chatClient.prompt()
            .user(question)
            .call()
            .content();
    }
}
```

Test it:

```sh
curl -G http://localhost:8087/api/rag/ask \
  --data-urlencode "question=What is constructor injection?"
```

Result:

```txt
Constructor injection is a form of dependency injection where dependencies are 
provided to a class through its constructor. In Spring Boot, this approach is 
recommended because it makes dependencies explicit and easier to test.
```

Both implementations follow the same basic RAG workflow.

|Manual RAG|Spring AI Advisor|
|---|---|
|Explicit retrieval|Automatic retrieval|
|Custom prompt construction|Advisor handles augmentation|
|More control|Less boilerplate|
|Useful for learning and customization|Useful for standard RAG applications|

For production, document ownership filtering remains essential. Every retrieval must be restricted to the authenticated user's permitted documents.

## 5. Verify the Pipeline

You can inspect the stored embeddings directly in PostgreSQL.

Connect:

```sh
docker exec -it ragdemo-postgres-1 \
  psql -U app -d ragdemo
```

Then:

```sql
SELECT
    id,
    content,
    metadata
FROM vector_store;
```

The `vector_store` table contains the document content, metadata, and embedding vectors.

You can also test the retrieval stage independently of the chat model:

```java
List<Document> results = vectorStore.similaritySearch(
    SearchRequest.builder()
        .query("How does Spring create objects?")
        .topK(3)
        .build()
);

results.forEach(doc ->
    System.out.println(doc.getText())
);
```

This is particularly useful for debugging. If the correct chunks are not retrieved, changing the chat model may not solve the problem.

## Key Concepts to Remember

- Indexing: Load documents, split them, generate embeddings, and store them.
- Retrieval: Embed the question and search for semantically similar chunks.
- Augmentation: Add retrieved content to the LLM prompt.
- Generation: Use the chat model to produce an answer based on that context.
- Spring AI: `VectorStore` handles vector operations, while `ChatClient` and `QuestionAnswerAdvisor` simplify generation and retrieval integration.
    
This architecture could eventually support an Assistant that answers questions from the user's own Markdown knowledge base, rather than relying exclusively on the model's training knowledge.