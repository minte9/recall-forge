package dev.recallforge.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import dev.recallforge.domain.MarkdownFile;
import dev.recallforge.domain.SubcategoryOrder;
import dev.recallforge.domain.Topic;
import dev.recallforge.repository.ReviewRepository;
import dev.recallforge.repository.SubcategoryOrderRepository;
import dev.recallforge.repository.TopicCodeChallengeCountProjection;
import dev.recallforge.repository.TopicRepository;

/**
 * Topic lookup and "what's next" selection. The two
 * {@code selectNextTopic*} methods each apply their own tie-breaking order
 * (subcategory/file/topic-number, or file/topic-number for a single-file
 * queue) so due topics come back in a stable, document-like order instead
 * of database id order. {@link #selectNextTopic} additionally prioritizes
 * topics with prior review history over never-reviewed ones, so an
 * ever-growing backlog of new material can't crowd out topics that are
 * actually due for spaced repetition. Used by {@link ReviewService} to pick
 * the topic for the next review.
 */
@Service
public class TopicService {

    private final TopicRepository topicRepository;
    private final SubcategoryOrderRepository subcategoryOrderRepository;
    private final ReviewRepository reviewRepository;

    public TopicService(
            TopicRepository topicRepository,
            SubcategoryOrderRepository subcategoryOrderRepository,
            ReviewRepository reviewRepository) {
        this.topicRepository = topicRepository;
        this.subcategoryOrderRepository = subcategoryOrderRepository;
        this.reviewRepository = reviewRepository;
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

    /**
     * Start Review flow — sorts due topics with this priority:
     *
     *  1. reviewed-before-unreviewed
     *  2. subcategory display order
     *  3. subcategory name
     *  4. markdown file displayOrder
     *  5. the leading number parsed out of the topic title (e.g. "1. Something" → 1)
     *  6. memoryScore (weakest first, only breaks ties left by document order)
     *  7. nextReviewAt
     */
    public Optional<Topic> selectNextTopic(Long userId, String category) {
        List<Topic> dueTopics = topicRepository.findDueTopicsByCategory(userId, category, LocalDateTime.now());

        if (dueTopics.isEmpty()) {
            return Optional.empty();
        }

        Map<String, Integer> subcategoryOrder = subcategoryOrderRepository.findByCategoryIgnoreCase(category)
            .stream()
            .collect(Collectors.toMap(SubcategoryOrder::getSubcategory, SubcategoryOrder::getDisplayOrder));

        // Every freshly imported topic starts at the same default memory
        // score and is due from the moment it's created, so letting
        // never-reviewed topics compete on equal footing with everything
        // else would let an ever-growing backlog of new material permanently
        // outrank topics that were already reviewed and are due again - the
        // opposite of what spaced repetition is for (a decaying memory is
        // more urgent than material that hasn't been seen yet, since the
        // latter isn't decaying). Topics with review history come first;
        // unreviewed topics only fill in once every reviewed-and-due topic
        // has been offered.
        Set<Long> reviewedTopicIds = new HashSet<>(
            reviewRepository.findReviewedTopicIds(dueTopics.stream().map(Topic::getId).toList(), userId));

        return dueTopics.stream()
            .sorted(
                // Within each of those two groups, present topics in the
                // same order they appear on the markdown page rather than
                // weakest-score-first - jumping from a hard topic in file 3
                // to a basic one in file 1 has no visible logic from the
                // user's side, and reviewing in document order lets the
                // user work through easy material and defer harder topics
                // rather than being forced to confront the weakest one
                // immediately. Prefer the subcategory order shown in the
                // markdown list (same subcategory_order table/alphabetical
                // fallback as DashboardService.getMarkdownSummaries), then
                // the file's own display_order within that subcategory
                // (every file's topic numbering restarts at "1.", so without
                // this, files tie and fall through), then the topic that
                // comes first in its file ("1." before "2."). Memory score
                // only breaks ties left over once document order is
                // exhausted (e.g. two topics with an identical position,
                // such as freshly imported ones before display_order is
                // set), instead of dominating the ordering.
                Comparator.<Topic, Boolean>comparing(topic -> !reviewedTopicIds.contains(topic.getId()))
                    .thenComparing(topic -> subcategoryDisplayOrder(topic, subcategoryOrder))
                    .thenComparing(Topic::getSubcategory, String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(TopicService::markdownFileDisplayOrder, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparingLong(topic -> leadingTitleNumber(topic.getTitle()))
                    .thenComparing(Topic::getMemoryScore)
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

    /**
     * Dashboard "Code Challenge" flow — picks the topic to challenge next
     * across the whole category, regardless of due date or review history:
     *
     *  1. fewest completed code challenges first (a never-challenged topic
     *     always outranks one that's been challenged, even in a later file)
     *  2. subcategory display order
     *  3. subcategory name
     *  4. markdown file displayOrder
     *  5. the leading number parsed out of the topic title ("1." before "2.")
     *  6. topic id (final stable tie-break)
     *
     * Keys 2-6 are the same document order the markdown list shows, so once
     * code-challenge counts tie, the earliest topic the user can see wins.
     * Empty only when the category has no topics at all.
     */
    public Optional<Topic> selectNextCodeChallengeTopic(Long userId, String category) {
        List<Topic> topics = topicRepository.findByUserIdAndCategoryIgnoreCase(userId, category);

        if (topics.isEmpty()) {
            return Optional.empty();
        }

        Map<String, Integer> subcategoryOrder = subcategoryOrderRepository.findByCategoryIgnoreCase(category)
            .stream()
            .collect(Collectors.toMap(SubcategoryOrder::getSubcategory, SubcategoryOrder::getDisplayOrder));

        Map<Long, Long> codeChallengeCounts = reviewRepository.countCodeChallengesByTopic(userId, category)
            .stream()
            .collect(Collectors.toMap(
                TopicCodeChallengeCountProjection::getTopicId,
                TopicCodeChallengeCountProjection::getCodeChallengeCount));

        return topics.stream()
            .sorted(
                Comparator.<Topic>comparingLong(topic -> codeChallengeCounts.getOrDefault(topic.getId(), 0L))
                    .thenComparing(topic -> subcategoryDisplayOrder(topic, subcategoryOrder))
                    .thenComparing(Topic::getSubcategory, String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(TopicService::markdownFileDisplayOrder, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparingLong(topic -> leadingTitleNumber(topic.getTitle()))
                    .thenComparing(Topic::getId)
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

    public long countDueTopicsBySubcategory(
        Long userId,
        String category,
        String subcategory,
        LocalDateTime now
    ) {
        return topicRepository.countDueByUserAndCategoryAndSubcategory(
            userId, category, subcategory, now);
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
