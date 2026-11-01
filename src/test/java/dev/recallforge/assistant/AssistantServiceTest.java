package dev.recallforge.assistant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;

import dev.recallforge.config.AppReviewLimitProperties;
import dev.recallforge.domain.QuestionGeneration;
import dev.recallforge.exception.DailyQuestionLimitExceededException;
import dev.recallforge.repository.QuestionGenerationRepository;
import dev.recallforge.security.CurrentUserService;

@ExtendWith(MockitoExtension.class)
class AssistantServiceTest {

    private static final Long USER_ID = 7L;
    private static final int DAILY_USER_LIMIT = 50;

    @Mock
    private ChatModel chatModel;

    @Mock
    private AssistantTools tools;

    @Mock
    private CurrentUserService currentUser;

    @Mock
    private QuestionGenerationRepository questionGenerationRepository;

    private AssistantService assistantService;

    @BeforeEach
    void setUp() {
        AppReviewLimitProperties limits = new AppReviewLimitProperties(10, 10, DAILY_USER_LIMIT, 100);
        assistantService = new AssistantService(
                chatModel, tools, currentUser, questionGenerationRepository, limits);

        when(currentUser.currentUserId()).thenReturn(USER_ID);
    }

    @Test
    void askRecordsAiCallUnderAssistantCategoryAndReturnsAnswer() {
        when(questionGenerationRepository.countByUserIdAndGeneratedAtAfter(eq(USER_ID), any(LocalDateTime.class)))
                .thenReturn((long) DAILY_USER_LIMIT - 1);
        when(chatModel.call(any(Prompt.class))).thenReturn(
                new ChatResponse(List.of(new Generation(new AssistantMessage("Focus on generics.")))));

        String answer = assistantService.ask("What are my weak spots?", "Java");

        assertThat(answer).isEqualTo("Focus on generics.");

        ArgumentCaptor<QuestionGeneration> saved = ArgumentCaptor.forClass(QuestionGeneration.class);
        verify(questionGenerationRepository).save(saved.capture());
        assertThat(saved.getValue().getUserId()).isEqualTo(USER_ID);
        assertThat(saved.getValue().getCategory()).isEqualTo(AssistantService.ASSISTANT_CATEGORY);
    }

    @Test
    void askThrowsWithoutCallingModelWhenDailyAiLimitReached() {
        when(questionGenerationRepository.countByUserIdAndGeneratedAtAfter(eq(USER_ID), any(LocalDateTime.class)))
                .thenReturn((long) DAILY_USER_LIMIT);

        assertThatThrownBy(() -> assistantService.ask("What are my weak spots?", "Java"))
                .isInstanceOf(DailyQuestionLimitExceededException.class)
                .hasMessageContaining("daily AI limit");

        verify(questionGenerationRepository, never()).save(any());
        verify(chatModel, never()).call(any(Prompt.class));
    }
}
