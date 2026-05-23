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

    public List<Topic> getDueTopics() {
        return repository.findDueTopics(LocalDateTime.now());
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
