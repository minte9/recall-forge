# Recall Forge - v1.0.1

The application import topics from a local README.md, ask review questions,  
evaluates answers with OpenAI, and stores memory in database.  

## 1. Backend-only
v1.0.1

- 1.1 Project structure
- 1.2 Gradle Settings 
- 1.3 Gradle Build
- 1.4 Docker Compose
- 1.5 OpenAPI 
- 1.6 Application Properties
- 1.7 Main App
- 1.8 Domain model (JPA)
- 1.9 Repositories
- 1.10 DTOs
- 1.11 OpenAI Config
- 1.12 Services
- 1.13 Controllers
- 1.14 Run the project
- 1.15 Api Requests

## 2. Repetition System
v1.0.2

### 2.1 Review History Endpoint

#### Add DTO

src/main/../dto/ReviewHistoryResponse.java

<details>
<summary>ReviewHistoryResponse.java</summary>

~~~java
package dev.recallforge.dto;

import dev.recallforge.domain.Review;

import java.time.LocalDateTime;

public record ReviewHistoryResponse(
        Long id,
        Long topicId,
        String topicTitle,
        String question,
        String userAnswer,
        double score,
        String feedback,
        LocalDateTime reviewedAt
) {
    public static ReviewHistoryResponse from(Review review) {
        return new ReviewHistoryResponse(
                review.getId(),
                review.getTopic().getId(),
                review.getTopic().getTitle(),
                review.getQuestion(),
                review.getUserAnswer(),
                review.getScore(),
                review.getFeedback(),
                review.getReviewedAt()
        );
    }
}
~~~
</details>

#### Update ReviewService

~~~java
public List<ReviewHistoryResponse> getReviewHistoryForTopic(Long topicId) {
    topicService.getTopic(topicId);

    return reviewRepository.findByTopicIdOrderByReviewedAtDesc(topicId)
            .stream()
            .map(ReviewHistoryResponse::from)
            .toList();
}
~~~

### Update Controller

~~~java
@GetMapping("/{topicId}/reviews")
public List<ReviewHistoryResponse> getReviewHistory(
        @PathVariable Long topicId
) {
    return reviewService.getReviewHistoryForTopic(topicId);
}
~~~

#### Test it

~~~sh
./gradlew bootRun
~~~

#### API request

~~~sh
curl http://localhost:9090/api/topics/2/reviews | jq
~~~

<details>
<summary>Review History Response</summary>

~~~json
[
  {
    "id": 4,
    "topicId": 2,
    "topicTitle": "Pipeline Model",
    "question": "What is the main purpose of using a pipeline model in data processing?",
    "userAnswer": "A pipeline model process data in steps",
    "score": 0.5,
    "feedback": "The answer correctly states that a pipeline processes data in steps but does not explain the main purpose, such as simplifying complex workflows or improving understanding and debugging.",
    "reviewedAt": "2026-05-23T16:58:06.344937"
  },
  {
    "id": 3,
    "topicId": 2,
    "topicTitle": "Pipeline Model",
    "question": "What is the main purpose of using a pipeline model in data processing?",
    "userAnswer": "A pipeline model process data in steps",
    "score": 0.5,
    "feedback": "The answer correctly states that a pipeline processes data in steps but does not explain the main purpose, such as simplifying complex workflows or improving understanding and debugging.",
    "reviewedAt": "2026-05-23T16:51:18.051198"
  },
  {
    "id": 2,
    "topicId": 2,
    "topicTitle": "Pipeline Model",
    "question": "What is the main purpose of using a pipeline model in data processing?",
    "userAnswer": "A pipeline model process data in steps",
    "score": 0.5,
    "feedback": "The answer correctly identifies that a pipeline processes data in steps but does not explain the main purpose, such as simplifying complex workflows or improving understanding and debugging.",
    "reviewedAt": "2026-05-23T16:24:28.434414"
  }
]
~~~
</details>


### 2.2 Due Topics Endpoint

Right now GET /topics returns all topics.  
But in real spaced repetition GET /topics/due returns only topics for review.  

TopicController.java

~~~java
@GetMapping("/due")
public List<TopicResponse> getDueTopics() {

    return topicService
            .getDueTopics()
            .stream()
            .map(TopicResponse::from)
            .toList();
}
~~~

### 2.3 Test it

~~~sh
curl http://localhost:9090/api/topics/due | jq
~~~

~~~json
[
  {
    "id": 3,
    "title": "Agent Loop",
    "memoryScore": 0.5,
    "nextReviewAt": "2026-05-22T18:29:01.600257"
  },
  {
    "id": 4,
    "title": "Tool Calling",
    "memoryScore": 0.5,
    "nextReviewAt": "2026-05-22T18:29:01.604235"
  },
  {
    "id": 5,
    "title": "Spaced Repetition",
    "memoryScore": 0.5,
    "nextReviewAt": "2026-05-22T18:29:01.609641"
  }
]
~~~
~~~sh
curl -X POST http://localhost:9090/api/reviews/start | jq
~~~
~~~json
{
  "topicId": 3,
  "topicTitle": "Agent Loop",
  "question": "What are the four main steps in an agent loop?"
}
~~~

You should see reviewd topic disappears (until nextReviewAt).  


## 3. Dashboard

### 3.1 Index file

Create this file:

~~~sh
src/main/resources/static/index.html
~~~

Run

~~~sh
./gradlew bootRun
~~~

Open:

~~~sh
http://localhost:9090
~~~

~~~sh
lsof -i :9090

COMMAND    PID    USER   FD   TYPE  DEVICE SIZE/OFF NODE NAME
java    882866 catalin   89u  IPv6 2652026      0t0  TCP *:9090 (LISTEN)

kill -9 882866
~~~

## 4. Upload markdown file

Markdown become part of the Topic stored in DB.  
When /api/reviews/start returns the next review, it also returns the markdown content.  

### 4.1 MarkdownFile Entity

Create Markdown file entity:

/domain/MarkdownFile.java

Connect Topic to MarkdownFile

/domain/Topic.java

~~~java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "markdown_file_id")
private MarkdownFile markdownFile;
~~~

### 4.2 Add Repository

/repository/MarkdownFileRepository

~~~java
public interface MarkdownFileRepository extends JpaRepository<MarkdownFile, Long> {
}
~~~

### 4.3 Add upload endpoint

/controller/MarkdownController.java

~~~java
@RestController
@RequestMapping("/api/markdown")
public class MarkdownUploadController {

    private final MarkdownUploadService markdownUploadService;

    public MarkdownUploadController(MarkdownUploadService markdownUploadService) {
        this.markdownUploadService = markdownUploadService;
    }

    @PostMapping("/upload")
    public void uploadMarkdown(@RequestParam("file") MultipartFile file) throws Exception {
        markdownUploadService.upload(file);
    }
}
~~~

### 4.4 Upload Service

This stores the full file, than creates topics from markdown headings.  

/service/MarkdownService.java

~~~java
@Service
public class MarkdownService {

    private final MarkdownFileRepository markdownFileRepository;
    private final TopicRepository topicRepository;
    private final MarkdownTopicImporter markdownTopicImporter;

    public MarkdownService(
            MarkdownFileRepository markdownFileRepository,
            TopicRepository topicRepository,
            MarkdownTopicImporter markdownTopicImporter
    ) {
        this.markdownFileRepository = markdownFileRepository;
        this.topicRepository = topicRepository;
        this.markdownTopicImporter = markdownTopicImporter;
    }

    public void upload(MultipartFile file) throws Exception {
        String content = new String(file.getBytes(), StandardCharsets.UTF_8);

        MarkdownFile markdownFile = new MarkdownFile(file.getOriginalFilename(), content);
        markdownFileRepository.save(markdownFile);

        createTopicsFromMarkdown(markdownFile, content);
    }

    private void createTopicsFromMarkdown(MarkdownFile markdownFile, String markdown) {
        List<MarkdownTopicImporter.ParsedTopic> parseTopics = 
            markdownTopicImporter.parseMarkdown(markdown);

        for (MarkdownTopicImporter.ParsedTopic parsedTopic : parseTopics) {
            Topic topic = new Topic(
                parsedTopic.title(),
                parsedTopic.content(),
                markdownFile
            );

            topicRepository.save(topic);
        }
    }
}
~~~

Update review response DTO

~~~java
package dev.recallforge.dto;

public record ReviewQuestionResponse(
    Long topicId,
    String topicTitle,
    String question,
    String markdownContent
) {
}
~~~

Update review service

~~~java
public ReviewQuestionResponse startReview() {
        Topic topic = topicService.selectNextTopic();

        String question = openAiService.generateQuestion(
            topic.getTitle(), 
            topic.getContent()
        );

        return new ReviewQuestionResponse(
            topic.getId(), 
            topic.getTitle(), 
            question,
            topic.getMarkdownFile().getContent()
        );
    }
~~~


### 4.5 Update dashboar upload

Replace the browser-only upload with real backend upload.  

~~~javascript
async uploadMarkdown(event) {
  const file = event.target.files[0];

  if (!file) {
    return;
  }

  const formData = new FormData();
  formData.append("file", file);

  await fetch("/api/markdown/upload", {
    method: "POST",
    body: formData
  });

  await this.startReview();
}
~~~


### 4.6 Clear old data

~~~sh
sudo apt install postgresql-client

psql -h localhost -p 5432 -U recallforge -d recallforge

\dt

delete from reviews;
delete from topics;
delete from markdown_files;

OR

truncate reviews, topics, markdown_files restart identity cascade;
~~~

Since you already created the table:

~~~sh
alter table markdown_files
add column content_hash varchar(64);

update markdown_files
set content_hash = md5(content);

alter table markdown_files
alter column content_hash set not null;

alter table markdown_files
add constraint uk_markdown_hash
unique(content_hash);
~~~

Now:


~~~sh
README.md (content A) → accepted
README.md (content B) → accepted
README.md (same content A) → rejected
notes.md (same content A) → rejected
~~~


### 4.7 Check due topics

~~~sh
sudo apt install postgresql-client

psql -h localhost -p 5432 -U recallforge -d recallforge

\dt

select id, title, next_review_at, memory_score
from topics
order by next_review_at;
~~~

### 4.8 Add categories

### 4.9 Dump db

Dump from the container:

~~~sh
docker exec -t recallforge-postgres pg_dump -U recallforge -d recallforge > recallforge.sql

ls -lh recallforge.sql
~~~

Restore on another computer/container:

~~~sh
cat recallforge.sql | docker exec -i recallforge-postgres psql -U recallforge -d recallforge
~~~