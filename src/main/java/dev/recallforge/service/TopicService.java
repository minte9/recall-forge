package dev.recallforge.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import dev.recallforge.domain.MarkdownFile;
import dev.recallforge.domain.SubcategoryOrder;
import dev.recallforge.domain.Topic;
import dev.recallforge.repository.SubcategoryOrderRepository;
import dev.recallforge.repository.TopicRepository;

/**
 * Topic lookup and "what's next" selection. The two
 * {@code selectNextTopic*} methods each apply their own tie-breaking order
 * (subcategory/file/topic-number, or file/topic-number for a single-file
 * queue) so due topics come back in a stable, document-like order instead
 * of database id order. Used by {@link ReviewService} to pick the topic for
 * the next review.
 */
@Service
public class TopicService {

    private final TopicRepository topicRepository;
    private final SubcategoryOrderRepository subcategoryOrderRepository;

    public TopicService(TopicRepository topicRepository, SubcategoryOrderRepository subcategoryOrderRepository) {
        this.topicRepository = topicRepository;
        this.subcategoryOrderRepository = subcategoryOrderRepository;
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
        Map<String, Integer> subcategoryOrder = subcategoryOrderRepository.findByCategoryIgnoreCase(category)
            .stream()
            .collect(Collectors.toMap(SubcategoryOrder::getSubcategory, SubcategoryOrder::getDisplayOrder));

        return topicRepository.findDueTopicsByCategory(userId, category, LocalDateTime.now())
            .stream()
            .sorted(
                // Topics tied on memory score (e.g. freshly imported, never
                // reviewed) would otherwise fall back to nextReviewAt, which
                // for bulk-imported content is an arbitrary microsecond
                // difference rather than a meaningful signal. Prefer the
                // subcategory order shown in the markdown list (same
                // subcategory_order table/alphabetical fallback as
                // DashboardService.getMarkdownSummaries), then the file's own
                // display_order within that subcategory (every file's topic
                // numbering restarts at "1.", so without this, files tie and
                // fall through to nextReviewAt too), then the topic that
                // comes first in its file ("1." before "2.").
                Comparator.comparing(Topic::getMemoryScore)
                    .thenComparing(topic -> subcategoryDisplayOrder(topic, subcategoryOrder))
                    .thenComparing(Topic::getSubcategory, String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(TopicService::markdownFileDisplayOrder, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparingLong(topic -> leadingTitleNumber(topic.getTitle()))
                    .thenComparing(Topic::getNextReviewAt, Comparator.nullsFirst(Comparator.naturalOrder()))
            )
            .findFirst();
    }

    private static int subcategoryDisplayOrder(Topic topic, Map<String, Integer> subcategoryOrder) {
        return subcategoryOrder.getOrDefault(topic.getSubcategory(), Integer.MAX_VALUE);
    }

    private static Integer markdownFileDisplayOrder(Topic topic) {
        MarkdownFile markdownFile = topic.getMarkdownFile();
        return markdownFile != null ? markdownFile.getDisplayOrder() : null;
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
