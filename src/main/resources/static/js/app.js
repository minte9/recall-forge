const { createApp } = Vue;

createApp({
  data() {
    return {
      loading: false,
      error: null,
      currentMarkdownFileId: null,
      showMetadataHelp: false,
      markdown: null,
      review: null,
      userAnswer: "",
      result: null,
      dashboard: null,
      markdownSummaries: [],
      leftExpanded: false,
      answerSubmitted: false,
      currentUser: null,
      menuOpen: false,
      leftPanel: "markdown",
      selectedGroup: null,
      mobileView: "dashboard",
      mobileMenuOpen: false,
      openMarkdownGroup: null,
      mobilePageTitle: "Dashboard",
      markdownLoading: false,
      feedbackHistory: [],
      feedbackHistoryVisible: false,
      feedbackHistoryLoading: false,
      feedbackHistoryLoadedForTopicId: null,

      initialMarkdown: `
<h3>Markdown File</h3>

Upload a markdown file to start.
The uploaded markdown will be saved in the database.
When you click <strong>Start Review</strong>, this panel will be updated.
`
    };
  },

  computed: {
    rewardProgress() {
      if (!this.dashboard || !this.dashboard.reward || !this.dashboard.reward.xpForNextLevel) {
        return 0;
      }

      const value = (this.dashboard.reward.xpInLevel / this.dashboard.reward.xpForNextLevel) * 100;
      return Math.max(0, Math.min(100, value));
    },

    isDesktop() {
      return window.innerWidth > 900;
    },

    isLibraryMode() {
      return this.leftPanel !== "markdown";
    },

    markdownGroups() {
      const groups = {};

      this.markdownSummaries.forEach(file => {
        const key = `${file.category}, ${file.subcategory}`;

        if (!groups[key]) {
          groups[key] = {
            environment: file.environment,
            files: []
          };
        }

        groups[key].files.push(file);
      });

      return groups;
    }
  },

  async mounted() {
    document.addEventListener("click", this.handleBodyClick);

    await this.loadDashboard();
    await this.loadCurrentUser();
    await this.loadMarkdownSummaries(true);
  },

  beforeUnmount() {
      document.removeEventListener("click", this.handleBodyClick);
  },

  methods: {
    formatScore(score) {
      return Number(score).toFixed(2);
    },

    formatDate(date) {
      return new Date(date).toLocaleString(undefined, {
        year: "numeric",
        month: "short",
        day: "numeric",
        hour: "2-digit",
        minute: "2-digit"
      });
    },

    formatDateTime(value) {
      if (!value) return "";
      return new Date(value).toLocaleString();
    },

    formatShortDate(value) {
      if (!value) return "";

      return new Date(value).toLocaleString(undefined, {
        month: "short",
        day: "numeric",
        hour: "2-digit",
        minute: "2-digit"
      });
    },

    handleBodyClick(event) {
      if (!this.mobileMenuOpen) {
          return;
      }

      const menu = this.$refs.mobileMenu;

      if (menu && !menu.contains(event.target)) {
          this.mobileMenuOpen = false;
      }
    },

    onTitleClick() {
      if (this.mobilePageTitle === "Dashboard") {
        this.showMarkdownSummaries();
      } else {
        this.showDashboard();
      }
    },

    renderMarkdown(md) {
      return Markdown.render(md);
    },

    async startReview() {
      this.loading = true;
      this.markdownLoading = true;
      this.mobilePageTitle = "Review";
      this.error = null;
      this.result = null;
      this.userAnswer = "";
      this.answerSubmitted = false;
      this.showAccount = false;
      this.leftPanel = "markdown";
      this.mobileView = "review";
      this.feedbackHistory = [];
      this.feedbackHistoryVisible = false;
      this.feedbackHistoryLoadedForTopicId = null;

      try {
        let data = await Api.startReview(this.currentMarkdownFileId);

        if (data.done && this.currentMarkdownFileId) {
          this.currentMarkdownFileId = null;
          data = await Api.startReview(null);
        }

        if (data.done) {
          this.review = null;
          this.reviewMessage = data.message;
          await this.loadDashboard();
          return;
        }

        this.review = data;
        this.currentMarkdownFileId = this.review.markdownFileId;

        if (this.review.markdownContent) {
          this.markdownLoading = false;
          this.markdown = this.review.markdownContent;
        }

        await this.loadDashboard();

      } catch (err) {
        this.review = null;
        this.error = err.message || "Could not start review.";
      } finally {
        this.loading = false;
        this.markdownLoading = false;
      }
    },

    async startReviewForFile(markdownFileId) {
        this.currentMarkdownFileId = markdownFileId;
        await this.startReview();
    },

    async uploadMarkdown(event) {
      const file = event.target.files[0];

      if (!file) {
        return;
      }

      this.mobileView = "dashboard";
      this.loading = true;
      this.error = null;
      this.result = null;

      try {
        this.markdown = await file.text();

        const uploaded = await Api.uploadMarkdown(file);

        this.currentMarkdownFileId = uploaded.markdownFileId;
        this.markdown = uploaded.content;
        this.leftPanel = "markdown";
        this.mobileView = "review"; // show markdown + right panel on mobile

        await this.startReview();
        await this.loadDashboard();
        await this.loadMarkdownSummaries(false);

      } catch (err) {
        this.error = err.message || "Could not upload markdown file.";
      } finally {
        this.loading = false;
        event.target.value = "";
      }
    },

    async submitAnswer() {
      if (this.answerSubmitted || this.loading) {
        return;
      }

      this.answerSubmitted = true;
      this.loading = true;
      this.error = null;

      try {
        this.result = await Api.submitAnswer({
          topicId: this.review.topicId,
          question: this.review.question,
          userAnswer: this.userAnswer
        });

        await this.loadDashboard();
        await this.loadMarkdownSummaries();

        if (!this.review) {
          this.showMarkdownSummaries();
        }

      } catch (err) {
        this.error = err.message || "Could not submit answer.";
      } finally {
        this.loading = false;
      }
    },

    async loadDashboard() {
      try {
        const data = await Api.loadDashboard();

        if (data) {
          this.dashboard = data;
        }

      } catch (err) {
        console.error("Could not load dashboard", err);
      }
    },

    async loadMarkdownSummaries(autoShow = true) {
      try {
        const data = await Api.loadMarkdownSummaries();

        if (!data) {
          return;
        }

        this.markdownSummaries = data;

        if (!autoShow) {
          return;
        }

        if (this.isDesktop) {
          this.showMarkdownSummaries();
        } else {
          this.markdown = "";
          this.leftPanel = "markdown";
          this.mobileView = "dashboard";
          this.mobilePageTitle = "Dashboard";
        }

        if (!this.isDesktop) { // On mobile, don't show the default text
          this.markdown = "";
          this.leftPanel = "markdown";
          this.mobileView = "dashboard";
          this.mobilePageTitle = "Dashboard";
        }

      } catch (err) {
        console.error("Could not load markdown summaries", err);
      }
    },

    async loadCurrentUser() {
      try {
        const data = await Api.loadCurrentUser();

        if (data) {
          this.currentUser = data;
        }

      } catch (err) {
        console.error("Could not load current user", err);
      }
    },

    async goHome() {
      this.review = null;
      this.result = null;
      this.userAnswer = "";
      this.currentMarkdownFileId = null;
      this.showAccount = false;
      this.mobileView = "dashboard";
      this.leftExpanded = false;

      await this.loadDashboard();

      if (this.isDesktop && this.markdownSummaries.length > 0) {
        this.showMarkdownSummaries();
      } else {
        this.markdown = this.initialMarkdown;
        this.leftPanel = "markdown";
      }
    },

    showMarkdownSummaries() {
      this.mobilePageTitle = "Markdowns";
      this.leftPanel = "markdowns";
      this.mobileView = "markdown";
    },

    renderTextTable(files) {
      const rows = files.map(file => [
        file.fileTitle,
        String(file.topicCont),
        String(file.dueCount),
        this.formatScore(file.averageMemoryScore)
      ]);

      const headers = ["File", "Topics", "Due today", "Avg memory"];

      const widths = headers.map((header, index) =>
        Math.max(
          header.length,
          ...rows.map(row => row[index].length)
        )
      );

      const line = (left, middle, right) =>
        left + widths.map(width => "─".repeat(width + 2)).join(middle) + right;

      const row = values =>
        "│ " +
        values.map((value, index) => value.padEnd(widths[index])).join(" │ ") +
        " │";

      return [
        line("┌", "┬", "┐"),
        row(headers),
        line("├", "┼", "┤"),
        ...rows.map(row),
        line("└", "┴", "┘")
      ].join("\n");
    },

    showAccountDetails() {
      this.mobilePageTitle = "Account Details";
      this.leftPanel = "account";
      this.mobileView = "markdown";
    },

    async showDashboard() {
      this.review = null;
      this.result = null;
      this.userAnswer = "";
      this.currentMarkdownFileId = null;
      this.mobileView = "dashboard";
      this.leftExpanded = false;
      this.mobilePageTitle = "Dashboard";

      await this.loadDashboard();

      if (this.isDesktop && this.markdownSummaries.length > 0) {
        this.showMarkdownSummaries();
      } else {
        this.markdown = this.initialMarkdown;
        this.leftPanel = "markdown";
      }
    },

    closeLeftPanel() {
        this.showAccount = false;
        this.mobileView = "dashboard";
    },

    stringHash(value) {
      let hash = 0;

      for (let i = 0; i < value.length; i++) {
        hash = value.charCodeAt(i) + ((hash << 5) - hash);
      }

      return Math.abs(hash);
    },

    environmentColor(index) {
      const palette = [
          "#f45b00", // orange
          "#8b5cf6", // purple
          "#3b82f6"  // blue
      ];

      return palette[index % palette.length];
    },

    softColor(hex) {
      return hex + "18";
    },

    async toggleFeedbackHistory() {
      this.feedbackHistoryVisible = !this.feedbackHistoryVisible;

      if (!this.feedbackHistoryVisible) return;

      const topicId = this.review?.topicId;

      if (!topicId) return;

      if (this.feedbackHistoryLoadedForTopicId === topicId) return;

      await this.loadFeedbackHistory(topicId);
    },

    async loadFeedbackHistory(topicId) {
      this.feedbackHistoryLoading = true;

      try {
        this.feedbackHistory = await Api.loadFeedbackHistory(topicId);
        this.feedbackHistoryLoadedForTopicId = topicId;
      } finally {
        this.feedbackHistoryLoading = false;
      }
    },

    showMarkdownExample() {
      this.mobilePageTitle = "Markdown Example";
      this.leftPanel = "markdown";
      this.showAccount = false;
      this.mobileView = "markdown";
      this.markdown = `
<h3>Pattern</h3>

<pre>## Environment / Category / Subcategory / File Title

### 1. Topic
Content...

### 2. Topic
Content...</pre>

<h3>Example</h3>

<pre>## JVM / Java / Threads / Theory

### 1. Creating Threads
Thread creation basics...

### 2. Implementing Runnable
Java supports single inheritance...</pre>
`;
    }
  }
}).mount("#app");
