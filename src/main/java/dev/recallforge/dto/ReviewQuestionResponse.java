package dev.recallforge.dto;

public record ReviewQuestionResponse(
        Long topicId,
        String topicTitle,
        String question,
        String markdownContent,
        Long markdownFileId,
        boolean done,
        String message,
        // True when this topic (manually selected via the topic dropdown)
        // already has a real review from earlier today, or isn't due yet -
        // tells the frontend to force "Try Again" (practice) grading rather
        // than let an out-of-schedule answer move the spaced-repetition state.
        boolean forcePractice
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
			false
        );
    }
}