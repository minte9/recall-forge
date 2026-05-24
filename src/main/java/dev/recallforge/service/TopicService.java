package dev.recallforge.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

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

    public Topic selectNextTopic() {
        return topicRepository.findDueTopics(LocalDateTime.now())
            .stream()
            .findFirst()
            .orElseThrow(() ->
                    new IllegalStateException("No topics available. Upload a markdown file first.")
    );
}

    public Topic save(Topic topic) {
        return topicRepository.save(topic);
    }
}
