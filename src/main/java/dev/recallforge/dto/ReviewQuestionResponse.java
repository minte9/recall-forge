package dev.recallforge.dto;

public record ReviewQuestionResponse(
        Long topicId,
        String topicTitle,
        String question,
        String markdownContent,
        Long markdownFileId,
        boolean done,
        String message,
        // Only set for the practice-only "Code Challenge" flow, prefilled
        // into the answer box by the frontend. Null for a normal review.
        String codeSnippet,
        // True when this topic (manually selected via the topic dropdown)
        // already has a real review from earlier today - tells the frontend
        // to force "Try Again" (practice) grading rather than allow a
        // second full review to move the spaced-repetition state today.
        boolean alreadyReviewedToday
) {
    public static ReviewQuestionResponse noReviewsDue() {
        return new ReviewQuestionResponse(
			null,
			null,
			null,
			null,
			null,
			true,
			"All reviews are done for now. Come back later.",
			null,
			false
        );
    }
}