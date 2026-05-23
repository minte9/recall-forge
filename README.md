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
<details>


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