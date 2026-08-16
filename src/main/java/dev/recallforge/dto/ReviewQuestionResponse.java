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
        boolean forcePractice,
        // True when this question is a Code Challenge (worded prompt asking
        // the user to write code, graded strictly) rather than a normal
        // review question - tells the frontend to submit the answer through
        // the strict grading path and to keep treating it as practice.
        boolean codeChallenge,
        SubcategoryMasteryResponse subcategoryMastery
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
			false,
			false,
			null
        );
    }
}