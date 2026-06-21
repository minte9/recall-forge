package dev.recallforge.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import dev.recallforge.domain.Topic;
import dev.recallforge.repository.TopicRepository;

@Service
public class TopicService {
    
    private final TopicRepository topicRepository;

    public TopicService (TopicRepository topicRepository) {
        this.topicRepository = topicRepository;
    }

    public List<Topic> getAllTopics(Long userId) {
        return topicRepository.findByUserId(userId)
            .stream()
            .sorted(Comparator.comparing(Topic::getMemoryScore))
            .toList();
    }

    public List<Topic> getDueTopics(Long userId) {
        return topicRepository.findDueTopics(userId, LocalDateTime.now());
    }

    public Topic getTopic(Long topicId, Long userId) {
        return topicRepository.findByIdAndUserId(topicId, userId)
            .orElseThrow(() ->
                new IllegalArgumentException("Topic not found: " + topicId)
            );
    }

    public Optional<Topic> selectNextTopic(Long userId) {
        return topicRepository.findDueTopics(userId, LocalDateTime.now())
            .stream()
            .findFirst();
    }

    public Optional<Topic> selectNextTopicByMarkdownFileId(Long markdownFileId, Long userId) {
        return topicRepository
            .findDueTopicsByMarkdownFileId(
                markdownFileId, userId, LocalDateTime.now())
            .stream()
            .findFirst();
    }

    public long countDueTopics(Long userId, LocalDateTime now) {
        return topicRepository.countDueByUser(userId, now);
    }

    public long countDueTopicsByMarkdownFileId(
        Long markdownFileId,
        Long userId,
        LocalDateTime now
    ) {
        return topicRepository.countDueByMarkdownFileIdAndUser(
            markdownFileId,
            userId,
            now
        );
    }

    public Optional<Topic> findNextReviewAtAfter(Long userId, LocalDateTime now) {
        return topicRepository.findNextByUser(userId, now, PageRequest.of(0, 1))
            .stream()
            .findFirst();
    }

    public Optional<Topic> findNextReviewAtAfterByMarkdownFileId(
        Long markdownFileId,
        Long userId,
        LocalDateTime now
    ) {
        return topicRepository.findNextByMarkdownFileIdAndUser(
            markdownFileId,
            userId,
            now,
            PageRequest.of(0, 1)
        )
        .stream()
        .findFirst();
    }

    public Topic save(Topic topic) {
        return topicRepository.save(topic);
    }
}
