package dev.recallforge.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public List<Topic> getDueTopics(Long userId, String category) {
        return topicRepository.findDueTopicsByCategory(userId, category, LocalDateTime.now());
    }

    public Topic getTopic(Long topicId, Long userId) {
        return topicRepository.findByIdAndUserId(topicId, userId)
            .orElseThrow(() ->
                new IllegalArgumentException("Topic not found: " + topicId)
            );
    }

    public Optional<Topic> selectNextTopic(Long userId, String category) {
        return topicRepository.findDueTopicsByCategory(userId, category, LocalDateTime.now())
            .stream()
            .sorted(
                // Topics tied on memory score (e.g. freshly imported, never
                // reviewed) would otherwise fall back to nextReviewAt, which
                // for bulk-imported content is an arbitrary microsecond
                // difference rather than a meaningful signal. Prefer the
                // topic that comes first in its file ("1." before "2.") for
                // those ties.
                Comparator.comparing(Topic::getMemoryScore)
                    .thenComparingLong(topic -> leadingTitleNumber(topic.getTitle()))
                    .thenComparing(Topic::getNextReviewAt, Comparator.nullsFirst(Comparator.naturalOrder()))
            )
            .findFirst();
    }

    public Optional<Topic> selectNextTopicByMarkdownFileId(Long markdownFileId, Long userId) {
        return topicRepository.findDueTopicsByMarkdownFileId(
                markdownFileId,
                userId,
                LocalDateTime.now()
            )
            .stream()
            .sorted(
                // Never-reviewed topics still go first, same as the repository
                // query's own ordering; within each group, prefer the topic
                // that comes first in the file ("1." before "2.") instead of
                // whichever happens to have the lower memory score, so a
                // weaker later topic doesn't jump the queue ahead of an
                // earlier one that's also due.
                Comparator.<Topic, Boolean>comparing(topic -> topic.getNextReviewAt() != null)
                    .thenComparingLong(topic -> leadingTitleNumber(topic.getTitle()))
                    .thenComparing(Topic::getMemoryScore)
                    .thenComparing(Topic::getNextReviewAt, Comparator.nullsFirst(Comparator.naturalOrder()))
            )
            .findFirst();
    }

    private static final Pattern LEADING_NUMBER = Pattern.compile("^\\s*(\\d+)");

    public List<Topic> getTopicsByMarkdownFileId(Long markdownFileId, Long userId) {
        return topicRepository.findByMarkdownFileIdAndUserIdOrderByIdAsc(markdownFileId, userId)
            .stream()
            .sorted(Comparator.comparingLong(topic -> leadingTitleNumber(topic.getTitle())))
            .toList();
    }

    // Renamed topics are deleted and recreated with a new id, which would
    // otherwise push them to the end of an id-ordered list. Sorting by the
    // "1. Title" numbering convention keeps the dropdown in document order
    // regardless of id, falling back to id order (via stable sort) for
    // topics without a leading number.
    private static long leadingTitleNumber(String title) {
        if (title == null) {
            return Long.MAX_VALUE;
        }

        Matcher matcher = LEADING_NUMBER.matcher(title);

        if (!matcher.find()) {
            return Long.MAX_VALUE;
        }

        try {
            return Long.parseLong(matcher.group(1));
        } catch (NumberFormatException e) {
            return Long.MAX_VALUE;
        }
    }

    public long countDueTopics(Long userId, String category, LocalDateTime now) {
        return topicRepository.countDueByUser(userId, category, now);
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

    public Optional<Topic> findNextReviewAtAfter(Long userId, String category, LocalDateTime now) {
        return topicRepository.findNextByUser(userId, category, now, PageRequest.of(0, 1))
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

    public void flush() {
        topicRepository.flush();
    }
}
