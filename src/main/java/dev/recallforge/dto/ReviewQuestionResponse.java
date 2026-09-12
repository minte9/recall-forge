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

        // Mirrors MarkdownFile's own optimized/aiGenerated/originalContent -
        // false/null when the topic has no backing MarkdownFile (an ad-hoc
        // topic) - so the AI badge in the markdown panel stays correct while
        // reviewing, not just when browsing the file directly.
        boolean optimized,
        boolean aiGenerated,
        String originalContent
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
			false,
			false,
			null
        );
    }
}
