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

    public List<Topic> getAllTopics() {
        return topicRepository.findAll()
            .stream()
            .sorted(Comparator.comparing(Topic::getMemoryScore))
            .toList();
    }

    public List<Topic> getDueTopics() {
        return topicRepository.findDueTopics(LocalDateTime.now());
    }

    public Topic getTopic(Long topicId) {
        return topicRepository.findById(topicId)
            .orElseThrow(() ->
                new IllegalArgumentException("Topic not found: " + topicId)
            );
    }

    public Optional<Topic> selectNextTopic() {
        return topicRepository.findDueTopics(LocalDateTime.now())
            .stream()
            .findFirst();
    }

    public Optional<Topic> selectNextTopicByMarkdownFileId(Long markdownFileId) {
        return topicRepository
            .findDueTopicsByMarkdownFileId(markdownFileId, LocalDateTime.now())
            .stream()
            .findFirst();
    }

    public long countDueTopics(LocalDateTime now) {
        return topicRepository.countDue(now);
    }

    public long countDueTopicsByMarkdownFileId(Long markdownFileId, LocalDateTime now) {
        return topicRepository.countByMarkdownFileIdAndNextReviewAtLessThanEqual(
                markdownFileId,
                now
        );
    }

    public Optional<Topic> findNextReviewAtAfter(LocalDateTime now) {
        return topicRepository.findNext(now, PageRequest.of(0, 1))
                .stream()
                .findFirst();
    }

    public Optional<Topic> findNextReviewAtAfterByMarkdownFileId(
        Long markdownFileId,
            LocalDateTime now
    ) {
        return topicRepository
                .findFirstByMarkdownFileIdAndNextReviewAtGreaterThanOrderByNextReviewAtAsc(
                        markdownFileId,
                        now
                );
    }

    public Topic save(Topic topic) {
        return topicRepository.save(topic);
    }
}
