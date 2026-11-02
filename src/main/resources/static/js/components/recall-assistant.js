// AI Recall Assistant: the "Insights" box plus the ask-a-question
// conversation. Registered in app.js as <recall-assistant>.
//
// Parent contract:
//   :category      - current category, sent with every request
//   @open-topic    - (topicId, markdownFileId) when the insight is clicked
//   reset()        - called via ref when the user picks an environment
//   onReviewSubmitted(isRetry) - called via ref after an answer is graded
const RecallAssistant = {
  props: {
    category: { type: String, default: null }
  },

  emits: ["open-topic"],

  data() {
    return {
      input: '',
      loading: false,
      // Hides the "?" hints bubble after a pick until the mouse leaves it
      hintsDismissed: false,
      // category -> "?" prompts already answered; cleared by a new review
      usedSuggestions: {},
      messages: [],

      insightData: null,
      insightLoading: false
    };
  },

  computed: {
    // Example prompts per assistant tool, shown in the input's "?" tooltip.
    toolHints() {
      // Category is sent as context with the request, so prompts stay generic.
      return [
        {
          title: 'Mistakes',
          prompts: [
            'What mistakes have I made recently?',
            'Which topics do I keep getting wrong?',
            'Show my lowest-scoring answers.',
            'What are my recurring errors?'
          ]
        },
        {
          title: 'History',
          prompts: [
            'Show my last 5 reviews.',
            'What did I review today?',
            'Show my last 10 reviews with feedback.',
            'How did I score on my recent reviews?'
          ]
        },
        {
          title: 'Progress',
          prompts: [
            'Am I improving compared with last month?', 
            'How is my progress?', 
            'What is my average review score this month?', 
            'Am I getting better, or just completing more reviews'
          ]
        },
        {
          title: 'Areas',
          prompts: [
            'What subjects do I already know?',
            'Which are my strongest areas?',
            'What knowledge areas should I improve?',
            "What are my weakest Java subcategories?"
          ]
        }
      ];
    },

    insight() {
      if (!this.category) {
        return 'Select a category to get personalized recall insights.';
      }

      if (this.insightLoading) {
        return 'Analyzing your knowledge…';
      }

      return this.insightData?.message
        || `Explore your ${this.category} knowledge and identify areas for improvement.`;
    },

    insightClickable() {
      return !this.insightLoading
        && !!this.insightData?.weakestTopicId
        && !!this.insightData?.weakestMarkdownFileId;
    }
  },

  watch: {
    // immediate: the component mounts after the user loads, by which time
    // a category may already be selected.
    category: {
      immediate: true,
      handler(newCategory, oldCategory) {
        if (newCategory === oldCategory) return;

        this.insightData = null;
        this.insightLoading = false;

        this.loadInsight();
      }
    }
  },

  methods: {
    // Called by the parent when the user picks an environment.
    reset() {
      this.messages = [];
      this.input = '';
    },

    // Called by the parent after an answer is graded.
    async onReviewSubmitted(isRetry) {
      // New review data - the "?" prompts are worth asking again
      delete this.usedSuggestions[this.category];

      // Retries don't move memoryScore; otherwise the reviewed topic is no
      // longer due, so pick the next weakest.
      if (!isRetry) {
        await this.loadInsight();
      }
    },

    // "?" tooltip pick: close the bubble and ask the prompt right away.
    async submitHint(prompt) {
      if (this.isHintUsed(prompt)) {
        return;
      }

      const category = this.category;

      this.input = prompt;
      this.hintsDismissed = true;
      // Drop focus from the clicked prompt, or :focus-within keeps the bubble open.
      document.activeElement?.blur();

      const answered = await this.sendMessage();

      // Only burn the prompt once it was actually answered.
      if (answered) {
        const used = this.usedSuggestions[category] || [];
        this.usedSuggestions[category] = [...used, prompt];
      }
    },

    // Used "?" prompts stay disabled; only a new review unlocks them.
    clearConversation() {
      this.messages = [];
      this.input = '';
    },

    isHintUsed(prompt) {
      return (this.usedSuggestions[this.category] || [])
        .includes(prompt);
    },

    async sendMessage() {
      const message = this.input.trim();

      if (!message || this.loading) {
          return false;
      }

      const category = this.category;

      this.loading = true;
      this.input = '';

      this.$refs.input?.blur();

      // Display the user's question immediately
      this.messages.push({
          role: 'user',
          content: message
      });

      this.scrollIntoView();

      try {
          const answer = await this.ask(
              message,
              category
          );

          this.messages.push({
              role: 'assistant',
              content: answer
          });

          return true;

      } catch (error) {
          console.error('Assistant error:', error);

          this.messages.push({
              role: 'assistant',
              content: error.limitMessage || 'Unable to analyze your mistakes right now.'
          });

          return false;

      } finally {
          this.loading = false;
          this.scrollIntoView();
      }
    },

    async ask(question, category) {
      const response = await fetch('/api/assistant/ask', {
          method: 'POST',
          headers: {
              'Content-Type': 'application/json'
          },
          body: JSON.stringify({
              question,
              category
          })
      });

      // Daily AI limit reached: show the server's message as the reply
      if (response.status === 429) {
          const error = new Error('Assistant daily AI limit reached');
          error.limitMessage = await response.text();
          throw error;
      }

      if (!response.ok) {
          throw new Error(`Assistant API error: ${response.status}`);
      }

      const data = await response.json();

      return data.answer;
    },

    scrollIntoView() {
      this.$nextTick(() => {
        this.$refs.bottom?.scrollIntoView({
          behavior: 'smooth',
          block: 'end'
        });
      });
    },

    openInsightTopic() {
      const topicId = this.insightData?.weakestTopicId;
      const markdownFileId = this.insightData?.weakestMarkdownFileId;

      if (!topicId || !markdownFileId || this.insightLoading) {
        return;
      }

      this.$emit('open-topic', topicId, markdownFileId);
    },

    async loadInsight() {
      const category = this.category;

      if (!category) {
        this.insightData = null;
        return;
      }

      this.insightLoading = true;

      try {
        const response = await fetch(
          `/api/assistant/insight?category=${encodeURIComponent(category)}`
        );

        if (!response.ok) {
          throw new Error(`HTTP ${response.status}`);
        }

        const data = await response.json();

        // Ignore stale responses when the user changes categories.
        if (this.category === category) {
          this.insightData = data;
        }

      } catch (error) {
        console.error('Failed to load assistant insight:', error);

        if (this.category === category) {
          this.insightData = null;
        }

      } finally {
        if (this.category === category) {
          this.insightLoading = false;
        }
      }
    },

    renderMessage(message) {
      if (message.role === 'assistant') {
          return Markdown.render(message.content);
      }

      // User messages remain plain text, safely escaped.
      const div = document.createElement('div');
      div.textContent = message.content;
      return div.innerHTML;
    }
  },

  template: `
    <div class="recall-assistant">

      <div class="assistant-title" title="AI Assistant">
        <h3>Insights</h3> <!-- ✨ -->
      </div>

      <div
        class="assistant-insight"
        :class="{ clickable: insightClickable }"
        :role="insightClickable ? 'button' : null"
        :tabindex="insightClickable ? 0 : null"
        @click="openInsightTopic"
        @keydown.enter="openInsightTopic"
      >
        {{ insight }}
      </div>

      <!-- Assistant conversation -->
      <div
        v-if="messages.length || loading"
        class="assistant-conversation"
      >
        <div
          v-for="(message, index) in messages"
          :key="index"
          class="assistant-message"
          :class="'assistant-message-' + message.role"
          v-html="renderMessage(message)"
        ></div>

        <!-- Loading status: sits where the reply will appear -->
        <div
          v-if="loading"
          class="assistant-message assistant-message-assistant assistant-pending"
        >
          <span class="assistant-pending-icon" aria-hidden="true">✨</span> Analyzing your knowledge…
        </div>

      </div>

      <!-- Input -->
      <div class="assistant-input-wrapper">

        <div class="assistant-input-row">

          <input
            ref="input"
            v-model="input"
            placeholder="Ask about your knowledge…"
            @keydown.enter.prevent="sendMessage"
          />

          <button
            v-if="messages.length"
            type="button"
            class="assistant-clear"
            :disabled="loading"
            title="Clear conversation"
            aria-label="Clear conversation"
            @click="clearConversation"
          >
            <icon name="trash"></icon>
          </button>

          <span
            class="assistant-hints"
            :class="{ dismissed: hintsDismissed }"
            tabindex="0"
            aria-label="What you can ask"
            @mouseleave="hintsDismissed = false"
          >
            <icon name="question-circle"></icon>
            <span class="assistant-hints-bubble" role="tooltip">
              <span class="assistant-hints-header">Suggested Questions</span>
              <span
                v-for="group in toolHints"
                :key="group.title"
                class="assistant-hints-group"
              >
                <strong>{{ group.title }}</strong>
                <ul>
                  <li v-for="prompt in group.prompts" :key="prompt">
                    <button
                      type="button"
                      :disabled="isHintUsed(prompt)"
                      :title="isHintUsed(prompt) ? 'Already asked — review more topics to refresh' : null"
                      @click="submitHint(prompt)"
                    >
                      {{ prompt }}
                    </button>
                  </li>
                </ul>
              </span>
            </span>
          </span>
        </div>

      </div>

      <div ref="bottom"></div>

    </div>
  `
};
