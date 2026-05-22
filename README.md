# Recall Forge - v1.0.1

## 1. Backend-only

The application import topics from a local README.md, ask review questions,  
evaluates answers with OpenAI, and stores memory in database.  

This version is backend-only: no login, no frontend, no file uploads yet. 

It uses:

- Java 21
- Spring Boot 4
- Gradle
- PostfreSQL
- Docker Compose
- Local README.md topic source
- OpenAI API via Spring RestClient

Spring 4 also uses:

- Spring Framework 7
- Jakarta EE 11


### 1.1 Project structure

~~~sh
    recall-forge/
    ├── build.gradle
    ├── settings.gradle
    ├── compose.yml
    ├── README.md
    ├── .env.example
    ├── src/
    └── └── main/
            ├── java/
            │   └── dev/
            │       └── recallforge/
            │           ├── RecallForgeApplication.java
            │           ├── config/
            │           │   └── OpenAiConfig.java
            │           ├── controller/
            │           │   ├── ReviewController.java
            │           │   └── TopicController.java
            │           ├── domain/
            │           │   ├── Review.java
            │           │   └── Topic.java
            │           ├── dto/
            │           │   ├── AnswerRequest.java
            │           │   ├── AnswerResponse.java
            │           │   ├── ReviewQuestionResponse.java
            │           │   └── TopicResponse.java
            │           ├── repository/
            │           │   ├── ReviewRepository.java
            │           │   └── TopicRepository.java
            │           └── service/
            │               ├── EvaluationResult.java
            │               ├── MarkdownTopicImporter.java
            │               ├── OpenAiService.java
            │               ├── RepetitionService.java
            │               ├── ReviewService.java
            │               └── TopicService.java
            └── resources/
                └── application.yml
~~~


### 1.2 Gradle Settings 

<details>
<summary>settings.gradle</summary>

~~~groovy
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
    }
}

rootProject.name = 'recallforge'
~~~
</details>


### 1.3 Gradle Build

<details>
<summary>build.gradle</summary>

~~~groovy
plugins {
    id 'java'
    id 'org.springframework.boot' version '4.0.6'
    id 'io.spring.dependency-management' version '1.1.7'
}

group = 'dev.recallforge'
version = '0.0.1-SNAPSHOT'

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-validation'

    // Allows Spring Boot to automatically work with compose.yml during development.
    developmentOnly 'org.springframework.boot:spring-boot-docker-compose'

    runtimeOnly 'org.postgresql:postgresql:42.7.11'

    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}

tasks.named('test') {
    useJUnitPlatform()
}
~~~
</details>

### 1.4 Docker Compose

<details>
<summary>composer.yml</summary>

~~~yml
services:
  postgres:
    image: postgres:16
    container_name: recallforge-postgres
    environment:
      POSTGRES_DB: recallforge
      POSTGRES_USER: recallforge
      POSTGRES_PASSWORD: recallforge
    ports:
      - "5432:5432"
    volumes:
      - recallforge-postgres-data:/var/lib/postgresql/data

volumes:
  recallforge-postgres-data:
~~~
</details>

### 1.5 OpenAPI 

<details>
<summary>.env.examle</summary>

~~~sh
# .env.example
OPENAI_API_KEY=your_api_key_here

# cp .env.example .env
# .gitignore
~~~
</details>


### 1.6 Application Properties

Change port to 9090 (if 8080 already in use).

<details>
<summary>application.yml</summary>

~~~yml
spring:
  application:
    name: recallforge

  datasource:
    url: jdbc:postgresql://localhost:5432/recallforge
    username: recallforge
    password: recallforge

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

  docker:
    compose:
      enabled: true
      file: compose.yml

server:
  port: 9090

recallforge:
  notes:
    path: README.md

openai:
  api-key: ${OPENAI_API_KEY}
  model: gpt-4.1-mini
~~~
</details>


### 1.7 Main App

~~~java
package dev.recallforge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RecallForgeApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(RecallForgeApplication.class, args);
    }
}
~~~

Build and run to see the setup is correct.

~~~sh
docker compose up -d

gradle wrapper
./gradlew build

./gradlew bootRun
~~~


### 1.8 Domain model (JPA)

Hibernate implements JPA. 

Entity represents business data + rules. 
Entity should not contain domain logic, not database logic.  

<details>
<summary>Topic.java</summary>

~~~java
package dev.recallforge.domain;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "topics")
public class Topic {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The title from markdown section 
     * Example: "Agent Loop"
     */
    @Column(nullable = false, unique = true)
    private String title;

    /**
     * The body text under the markdown heading.
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * Average score from previous reviews.
     * 0.0 means unknown / weak.
     * 1.0 means wel known.
     */
    @Column(nullable = false)
    private double memoryScore = 0.5;

    /**
     * When this topic should appear again.
     */
    private LocalDateTime nextReviewAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    protected Topic {  // JPA requires an entity no-arg constructor
    }

    public Topic(String title, String content) {
        this.title = title;
        this.body = body;
        this.memoryScore = 0.5;
        this.nextReviewAt = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public double getMemoryScore() {
        return memoryScore;
    }

    public LocalDateTime getNextReviewAt() {
        return nextReviewAt;
    }

    public void updateMemory(double memoryScore, LocalDateTime nextReviewAt) {
        this.memoryScore = memoryScore;
        this.nextReviewAt = nextReviewAt;
        this.updatedAt = LocalDateTime.now();
    }
}
~~~
</details>


<details>
<summary>Review.java</summary>

~~~java
package dev.recallforge.domain;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Many reviews can belong to one topic.
     */
    @ManyToOne(optional = false)
    private Topic topic;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String userAnswer;

    @Column(nullable = false)
    private double score;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String feedback;

    @Column(nullable = false)
    private LocalDateTime reviwedAt;

    protected Review() {
    }
    
    public Review(Topic topic, String question, String userAnswer, double score, String feedback) {
        this.topic = topic;
        this.question = question;
        this.userAnswer = userAnswer;
        this.score = score;
        this.feedback = feedback;
        this.reviewedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Topic getTopic() {
        return topic;
    }

    public String getQuestion() {
        return question;
    }

    public String getUserAnswer() {
        return userAnswer;
    }

    public double getScore() {
        return score;
    }

    public String getFeedback() {
        return feedback;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }
}
~~~
</details>


### 1.9 Repositories

Repository handles persistence.  

<details>
<summary>TopicRepository.java</summary>

~~~java
package dev.recallforge.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dev.recallforge.domain.Topic;

public interface TopicRepository extends JpaRepository<Topic, Long> {

    Optional<Topic> findByTitle(String title);

    /**
     * Find topics that are due for review.
     */
    @Query("""
        SELECT t
        FROM Topic t
        WHERE t.nextReviewAt <= :now
        ORDER BY t.memoryScore ASC
    """)
    List<Topic> findDueTopics(LocalDateTime now);

    /**
     * Fallback query:
     * if no topic is due, select weakest topic anyway.
     */
    @Query("""
        SELECT t from Topic t order by t.memoryScore asc
    """)
    List<Topic> findWeakestTopics();
}
~~~
</details>

<details>
<summary>ReviewRepository.java</summary>

~~~java
package dev.recallforge.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import dev.recallforge.domain.Review;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Long, Review> {
    
    List<Review> findByTopicIdOrderByReviewedAtDesc(Long topicId);
}
~~~
</details>


### 1.10 DTOs

<details>
<summary>TopicResponse.java</summary>

~~~java
package dev.recallforge.dto;

import java.time.LocalDateTime;
import dev.recallforge.domain.Topic;

public record TopicResponse(
    Long id,
    String title,
    double memoryScore,
    LocalDateTime nextReviewAt
) {
    public static TopicResponse from(Topic topic) {
        return new TopicResponse(
            topic.getId(),
            topic.getTitle(),
            topic.getMemoryScore(),
            topic.getNextReviewAt()
        );
    }
}
~~~
</details>


<details>
<summary>ReviewQuestionResponse.java</summary>

~~~java
package dev.recallforge.dto;

public record ReviewQuestionResponse(
    Long topicId,
    String topicTitle,
    String question
) {
}
~~~
</details>


<details>
<summary>AnswerRequest.java</summary>

~~~java
package dev.recallforge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AnswerRequest(
    @NotNull
    Long topicId,

    @NotBlank
    String question,

    @NotBlank
    String userAnswer
) {
}
~~~
</details>


<details>
<summary>AnswerResponse.java</summary>

~~~java
package dev.recallforge.dto;

public record AnswerResponse(
    Lont topicId,
    String topicTitle,
    double score,
    String feedback,
    double updateMemoryScore,
    LocalDateTime nextReviewAt
) {
}
~~~
</details>


### 1.11 OpenAI Config

This creates a small HTTP client for OpenAI API.  

For v1, using RestClient keeps the project simple.  
Later, we can replace this with the official OpenAI Java SDK or Spring AI.  

OpenAiConfig.java

~~~java
package dev.recallforge.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class OpenAiConfig {
    
    @Bean
    RestClient openAiRestClient(@Value("${openai.api-key}") String apiKey) {
        return RestClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
~~~


### 1.12 Services

<details>
<summary>EvaluationResult.java</summary>

~~~java
package dev.recallforge.service;

public record EvaluationResult(
    double score,
    String feedback
) {
}
~~~
</details>

<details>
<summary>OpenAiService.java</summary>

~~~java
package dev.recallforge.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestClient;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

public class OpenAiService {
    
    private final RestClient openAiRestClient;
    private final String model;

    public OpenAiService(
        RestClient openAiRestClient,
        @Value("${openai.model}") String model
    ) {
        this.openAiRestClient = openAiRestClient;
        this.model = model;
    }

    public String generateQuestion(String topicTitle, String topicContent) {
        String prompt = """
        Create one simple question to test this topic.

        Topic title:
        %s

        Topic content:
        %s

        Rules:
        - Return only the question.
        - Keep it short.
        - Do not include the answer.
        """;
        prompt = prompt.formatted(topicTitle, topicContent);

        return ask(prompt);
    }

    public EvaluationResult evaluateAnswer(String topicTitle, String topicContent, String question, String userAnswer) {
        String prompt = """
        You are evaluating a learning review answer.

        Topic:
        %s

        Topic content:
        %s

        Question:
        %s

        User answer:
        %s

        Grade the answer from 0 to 1:
        - 1.0 = perfectly correct
        - 0.7 to 0.9 = mostly correct
        - 0.4 to 0.6 = partially correct
        - 0.1 to 0.3 = mostly incorrect but relevant
        - 0.0 = completely incorrect

        Return exactly this format:

        score: <number>
        feedback: <short explanation>
        """;
        prompt = prompt.formatted(topicTitle, topicContent, question, userAnswer);

        String rawResponse = ask(prompt);
        
        return parseEvaluation(rawResponse);
    }

    private String ask(String prompt) {
        Map<String, Object> requestBody = Map.of(
            "model", model,
            "messages", new Object[]{
                Map.of(
                    "role", "user", "content", prompt
                )
            },
            "temperature", 0.3
        );

        JsonNode response = openAiRestClient
                .post()
                .uri("/chat/completions")
                .body(requestBody)
                .retrieve()
                .body(JsonNode.class);

        if (response == null) {
            throw new IllegalStateException("OpenAI returned an empty response.");
        }

        JsonNode contentNode = response
                .path("choices")
                .path(0)
                .path("message")
                .path("content");

        if (contentNode.isMissingNode()) {
            throw new IllegalStateException("Could not read OpenAI response: " + response);
        }

        return contentNode.asString().trim();
    }

    private EvaluationResult parseEvaluation(String text) {
        double score = 0.5;
        String feedback = text;

        String[] lines = text.split("\\R");

        for (String line : lines) {
            String normalized = line.trim().toLowerCase();

            if (normalized.startsWith("score:")) {
                String value = line.substring(line.indexOf(":") + 1).trim();

                try {
                    score = Double.parseDouble(value);
                } catch (NumberFormatException ignored) {
                    score = 0.5;
                }
            }

            if (normalized.startsWith("feedback:")) {
                feedback = line.substring(line.indexOf(":") + 1).trim();
            }
        }

        score = Math.max(0.0, Math.min(1.0, score));

        return new EvaluationResult(score, feedback);
    }
}
~~~
</details>

<details>
<summary>MarkdownTopicImporter.java</summary>

~~~java
package dev.recallforge.service;

import dev.recallforge.domain.Topic;
import dev.recallforge.repository.TopicRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
public class MarkdownTopicImporter {

    private final TopicRepository topicRepository;
    private final Path notesPath;

    public MarkdownTopicImporter(
            TopicRepository topicRepository,
            @Value("${recallforge.notes.path}") String notesPath
    ) {
        this.topicRepository = topicRepository;
        this.notesPath = Path.of(notesPath);
    }

    /**
     * Imports topics from the configured markdown file.
     *
     * Existing topics are matched by title and updated.
     * New topics are created when no existing title is found.
     */
    public List<Topic> importFromLocalMarkdownFile() {
        String markdown = readMarkdownFile();

        List<ParsedTopic> parsedTopics = parseMarkdown(markdown);

        List<Topic> savedTopics = new ArrayList<>();

        for (ParsedTopic parsedTopic : parsedTopics) {
            Topic topic = topicRepository
                    .findByTitle(parsedTopic.title())
                    .map(existingTopic -> {
                        // Keep the same database row, but refresh its markdown content.
                        existingTopic.updateContent(parsedTopic.content());
                        return existingTopic;
                    })
                    .orElseGet(() -> 
                        // No existing topic with this title, so create a new entity.
                        new Topic(parsedTopic.title(), parsedTopic.content())
                    );
            
            // save() handles both insert and update.
            savedTopics.add(topicRepository.save(topic));
        }

        return savedTopics;
    }

    /**
     * Reads the markdown file configured by recallforge.notes.path.
     */
    private String readMarkdownFile() {
        try {
            return Files.readString(notesPath);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read markdown file: " + notesPath, e);
        }
    }

    /**
     * Parse markdown into topics.
     * Every markdown heading starts a new topic:
     *
     * # Agent Loop
     * body...
     *
     * Supports headings from # to ######.
     * Everything until the next heading becomes content.
     */
    private List<ParsedTopic> parseMarkdown(String markdown) {

        List<ParsedTopic> topics = new ArrayList<>();

        String currentTitle = null;
        StringBuilder currentContent = new StringBuilder();

        for (String line : markdown.split("\\R")) {

            // Match markdown headings
            boolean isHeading = line.matches("^#{1,6}\\s+.+");

            if (isHeading) {

                // Save previous topic before starting a new one.
                if (currentTitle != null) {

                    String content = currentContent.toString().trim();

                    if (!content.isBlank()) {
                        topics.add(new ParsedTopic(
                                currentTitle,
                                content
                        ));
                    }
                }

                // Remove leading # characters and spaces.
                currentTitle =
                        line.replaceFirst("^#+\\s+", "").trim();

                // Start collecting content for the new topic.
                currentContent = new StringBuilder();

            } else if (currentTitle != null) {

                // Add body lines until the next heading appears.
                currentContent
                        .append(line)
                        .append("\n");
            }
        }

        // Save the final topic after loop ends.
        if (currentTitle != null) {

            String content = currentContent.toString().trim();

            if (!content.isBlank()) {
                topics.add(new ParsedTopic(
                        currentTitle,
                        content
                ));
            }
        }

        return topics;
    }

    private record ParsedTopic(String title, String content) {
    }
}
~~~
</details>

<details>
<summary>RepetitionService.java</summary>

~~~java
package dev.recallforge.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

@Service
public class RepetitionService {
    
    /**
     * Memory score update:
     * 
     * We blend old memory score with the new review score. 
     * This avoids one bad answer destroying the full memory state.
     */
    public double calculateUpdateMemoryScore(double oldScore, double newScore) {
        return oldScore * 0.7 + newScore * 0.3;
    }

    /**
     * Spaced repetition rule:
     * 
     * Weak answers come back soon.
     * Strong answers come back later
     */
    public LocalDateTime calculateNextReviewAt(double score) {
        int intervalDays;

        if (score < 0.4) {
            intervalDays = 1;
        } else
        if (score < 0.7) {
            intervalDays = 3;
        } else
        if (score < 0.9) {
            intervalDays = 7;
        } else {
            intervalDays = 14;
        }

        return LocalDateTime.now().plusDays(intervalDays);
    }
}
~~~
</details>

<details>
<summary>TopicService.java</summary>

~~~java
package dev.recallforge.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import dev.recallforge.domain.Topic;
import dev.recallforge.repository.TopicRepository;

@Service
public class TopicService {
    
    private final TopicRepository repository;
    private final MarkdownTopicImporter importer;

    public TopicService (TopicRepository repository, MarkdownTopicImporter importer) {
        this.repository = repository;
        this.importer = importer;
    }

    public List<Topic> importTopics() {
        return importer.importFromLocalMarkdownFile();
    }

    public List<Topic> getAllTopics() {
        return repository.findAll()
                .stream()
                .sorted(Comparator.comparing(Topic::getMemoryScore))
                .toList();
    }

    public Topic getTopic(Long topicId) {
        return repository.findById(topicId)
                .orElseThrow(() ->
                    new IllegalArgumentException("Topic not found: " + topicId)
                );
    }

    public Topic selectNextTopic() {
        List<Topic> dueTopics = repository.findDueTopics(LocalDateTime.now());

        if (!dueTopics.isEmpty()) {
            return dueTopics.getFirst();
        }

        List<Topic> weakestTopics = repository.findWeakestTopics();

        if (!weakestTopics.isEmpty()) {
            return weakestTopics.getFirst();
        }

        throw new IllegalStateException("No topics found. Import topics first.");
    }

    public Topic save(Topic topic) {
        return repository.save(topic);
    }
}
~~~
</details>

<details>
<summary>ReviewService.java</summary>

~~~java
package dev.recallforge.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import dev.recallforge.domain.Review;
import dev.recallforge.domain.Topic;
import dev.recallforge.dto.AnswerResponse;
import dev.recallforge.dto.ReviewQuestionResponse;
import dev.recallforge.repository.ReviewRepository;
import jakarta.transaction.Transactional;

@Service
public class ReviewService {
    
    private final TopicService topicService;
    private final ReviewRepository reviewRepository;
    private final OpenAiService openAiService;
    private final RepetitionService repetitionService;

    public ReviewService(
        TopicService topicService,
        ReviewRepository reviewRepository,
        OpenAiService openAiService,
        RepetitionService repetitionService
    ) {
        this.topicService = topicService;
        this.reviewRepository = reviewRepository;
        this. openAiService = openAiService;
        this.repetitionService = repetitionService;
    }

    public ReviewQuestionResponse startReview() {
        Topic topic = topicService.selectNextTopic();

        String question = openAiService.generateQuestion(
                topic.getTitle(), 
                topic.getContent()
        );

        return new ReviewQuestionResponse(
            topic.getId(),
            topic.getTitle(),
            question
        );
    }

    @Transactional
    public AnswerResponse answerQuestion(
        Long topicId,
        String question,
        String userAnswer
    ) {
        Topic topic = topicService.getTopic(topicId);

        EvaluationResult evaluation = openAiService.evaluateAnswer(
            topic.getTitle(),
            topic.getContent(),
            question,
            userAnswer
        );

        Review review = new Review(
            topic,
            question,
            userAnswer,
            evaluation.score(),
            evaluation.feedback()
        );

        reviewRepository.save(review);

        double updateMemoryScore = repetitionService.calculateUpdateMemoryScore(
            topic.getMemoryScore(),
            evaluation.score()
        );

        LocalDateTime nextReviewAt = repetitionService.calculateNextReviewAt(
            evaluation.score()
        );

        topic.updateMemory(updateMemoryScore, nextReviewAt);
        topicService.save(topic);

        return new AnswerResponse(
            topic.getId(),
            topic.getTitle(),
            evaluation.score(),
            evaluation.feedback(),
            updateMemoryScore,
            nextReviewAt
        );
    }
}
~~~
</details>


### 1.13 Controllers

<details>
<summary>TopicController.java</summary>

~~~java
package dev.recallforge.controller;

import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.recallforge.dto.TopicResponse;
import dev.recallforge.service.TopicService;

@RestController
@RequestMapping("/api/topics")
public class TopicController {
 
    private final TopicService topicService;

    public TopicController(TopicService topicService) {
        this.topicService = topicService;
    }

    /**
     * Import topics from README.md
     * 
     * Later, this will become:
     * POST /api/topics/upload
     */
    @PostMapping("/import-local")
    public List<TopicResponse> importLocalTopics() {
        return topicService.importTopics()
                .stream()
                .map(TopicResponse::from)
                .toList();
    }
}
~~~
</details>

<details>
<summary>ReviewController.java</summary>

~~~java
package dev.recallforge.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.recallforge.dto.AnswerRequest;
import dev.recallforge.dto.AnswerResponse;
import dev.recallforge.dto.ReviewQuestionResponse;
import dev.recallforge.service.ReviewService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {
    
    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /**
     * Starts a review session.
     * 
     * The backend selects the weakest / due topic
     * and asks OpenAI to generate one question.
     */
    @PostMapping("/start")
    public ReviewQuestionResponse startReview() {
        return reviewService.startReview();
    }

    /**
     * Evaluates the user's answer.
     * 
     * The backend sends the question + user answer + topic content to openAI, 
     * receives a score, stores the review, and updates spaced repetition.
     */
    @PostMapping("/answer")
    public AnswerResponse answerQuestion(
        @Valid @RequestBody AnswerRequest request
    ) {
        return reviewService.answerQuestion(
            request.topicId(),
            request.question(),
            request.userAnswer()
        );
    }
}
~~~
</details>


### 1.14 Run the project

Start Docker:

~~~sh
docker compose up -d

# docker compose down
# docker compose down -v
~~~

Run Spring Boot:

~~~sh
./gradlew bootRun
~~~


### 1.15 Api Requests


#### Import topics

~~~sh
curl -X POST http://localhost:9090/api/topics/import-local | jq
~~~

<details>
<summary>Response</summary>

~~~json
[
  {
    "id": 1,
    "title": "RecallForge",
    "memoryScore": 0.5,
    "nextReviewAt": "2026-05-22T18:29:01.553478"
  },
  {
    "id": 2,
    "title": "Pipeline Model",
    "memoryScore": 0.5,
    "nextReviewAt": "2026-05-22T18:29:01.595478"
  },
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
</details>


#### List topics

~~~sh
curl http://localhost:9090/api/topics | jq
~~~

<details>
<summary>Response</summary>

~~~json
[
  {
    "id": 1,
    "title": "RecallForge",
    "memoryScore": 0.5,
    "nextReviewAt": "2026-05-22T18:29:01.553478"
  },
  {
    "id": 2,
    "title": "Pipeline Model",
    "memoryScore": 0.5,
    "nextReviewAt": "2026-05-22T18:29:01.595478"
  },
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
</details>


#### Start review

~~~sh
curl -X POST http://localhost:9090/api/reviews/start | jq
~~~

~~~json
{
  "topicId": 1,
  "topicTitle": "RecallForge",
  "question": "What is the primary function of RecallForge?"
}
~~~

#### Submit answer

~~~sh
curl -X POST http://localhost:9090/api/reviews/answer \
  -H "Content-Type: application/json" \
  -d '{
    "topicId": 1,
    "question": "What is the primary function of RecallForge?",
    "userAnswer": "spaced-repetition knowledgebase agent"
  }' | jq
~~~

~~~json
{
  "topicId": 1,
  "topicTitle": "RecallForge",
  "score": 0.7,
  "feedback": "The answer correctly identifies RecallForge as a spaced-repetition knowledgebase agent 
   but lacks detail about its primary functions such as importing notes, asking questions, 
   evaluating answers, and tracking weak topics.",
  "updateMemoryScore": 0.5599999999999999,
  "nextReviewAt": "2026-05-29T18:45:25.623704594"
}
~~~