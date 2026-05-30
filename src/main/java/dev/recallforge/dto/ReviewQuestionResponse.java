package dev.recallforge.dto;

public record ReviewQuestionResponse(
        Long topicId,
        String topicTitle,
        String question,
        String markdownContent,
        Long markdownFileId,
        boolean done,
        String message
) {
    public static ReviewQuestionResponse noReviewsDue() {
        return new ReviewQuestionResponse(
			null,
			null,
			null,
			null,
			null,
			true,
			"All reviews are done for now. Come back later."
        );
    }
}