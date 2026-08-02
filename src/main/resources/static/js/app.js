const { createApp } = Vue;

const LAST_CATEGORY_STORAGE_KEY = "recallforge.lastCategory";

createApp({
  data() {
    return {
      loading: false,
      error: null,
      success: null,
      currentMarkdownFileId: null,
      currentMarkdownReviewCount: null,
      newMarkdownContent: "",
      currentCategory: null,
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
      reviewQuestionLoading: false,
      markdownEditing: false,
      optimizingMarkdown: false,
      optimizingSeconds: 0,
      optimizingIntervalId: null,
      optimizeFinalized: false,
      feedbackHistory: [],
      feedbackHistoryVisible: false,
      feedbackHistoryLoading: false,
      feedbackHistoryLoadedForTopicId: null,
      selectedEnvironment: null,
      environmentMenuOpen: false,
      adminUsers: [],
      categoryOptions: [],
      showDangerZone: false,
      topicsInFile: [],
      topicDropdownOpen: false,
      topicDropdownAlignRight: false,

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
      if (!this.dashboard || !this.dashboard.reward) {
        return 0;
      }

      return Math.max(
        0,
        Math.min(100, this.dashboard.reward.masteryPercent || 0)
      );
    },

    isDesktop() {
      return window.innerWidth > 900;
    },

    dailyLimitReached() {
      return !!this.dashboard && this.dashboard.reviewsToday >= this.dashboard.dailyReviewLimit;
    },

    mobileTitleDisplay() {
      if (this.mobilePageTitle === "Review" && this.dashboard) {
        return `${this.dashboard.reviewsToday}/${this.dashboard.todayReviewLimit}`; // Reviews: 
      }

      return this.mobilePageTitle;
    },

    isLibraryMode() {
      return this.leftPanel !== "markdown";
    },

    averageScore() {
      if (this.feedbackHistory.length === 0) {
        return null;
      }

      const total = this.feedbackHistory.reduce((sum, r) => sum + r.score, 0);
      return total / this.feedbackHistory.length;
    },

    environmentGroups() {
      const groups = {};

      this.categoryOptions.forEach(option => {
        if (!groups[option.environment]) {
          groups[option.environment] = [];
        }

        groups[option.environment].push(option.category);
      });

      const priorityOrder = ["JVM", "PYTHON", "DEVOPS", "DB"];

      const orderedNames = Object.keys(groups).sort((a, b) => {
        const priorityA = priorityOrder.indexOf(a.toUpperCase());
        const priorityB = priorityOrder.indexOf(b.toUpperCase());

        if (priorityA !== -1 && priorityB !== -1) {
          return priorityA - priorityB;
        }

        if (priorityA !== -1) {
          return -1;
        }

        if (priorityB !== -1) {
          return 1;
        }

        return a.localeCompare(b);
      });

      const orderedGroups = {};

      orderedNames.forEach(name => {
        orderedGroups[name] = groups[name];
      });

      return orderedGroups;
    },

    filteredMarkdownGroups() {
      const groups = {};

      this.markdownSummaries
        .filter(file => file.category === this.selectedEnvironment)
        .forEach(file => {
          const key = file.subcategory;

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
    document.addEventListener("keydown", this.handleGlobalKeydown);

    await this.loadCategoryOptions();
    await this.loadDashboard();
    await this.loadCurrentUser();
    await this.loadMarkdownSummaries(true);

    this.autoOpenSingleMarkdownGroup();

    if (this.categoryOptions.length === 0) {
      this.showMarkdownSummaries();
    }
  },

  beforeUnmount() {
    document.removeEventListener("click", this.handleBodyClick);
    document.removeEventListener("keydown", this.handleGlobalKeydown);
  },

  methods: {
    formatScore(score) {
      return Number(score).toFixed(2);
    },

    formatTokenCount(value) {
      const tokens = Number(value) || 0;

      if (tokens < 1000) {
        return String(tokens);
      }

      return (tokens / 1000).toFixed(1).replace(/\.0$/, "") + "k";
    },

    truncateTitle(title, maxLength = 30) {
      if (!title || title.length <= maxLength) {
        return title;
      }

      return title.slice(0, maxLength) + "...";
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

    formatSubcategory(name) {
      if (!name) return "";

      return name
        .toLowerCase()
        .replace(/\b\w/g, c => c.toUpperCase());
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

    handleGlobalKeydown(event) {
      if (!event.ctrlKey || !event.shiftKey || event.key !== "Enter") {
        return;
      }

      if (!this.review || !this.answerSubmitted || this.loading || this.dailyLimitReached) {
        return;
      }

      event.preventDefault();
      this.startReview();
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

    onMarkdownClick(event) {
      if (event.target.closest(".code-block")) {
        return;
      }

      if (this.currentMarkdownFileId) {
        this.editMarkdown();
      }
    },

    editMarkdown() {
      const scrollY = window.scrollY;

      this.leftPanel = "markdown";
      this.markdownEditing = true;
      this.optimizeFinalized = false;

      this.$nextTick(() => {
        this.resizeMarkdownEditor();
        this.$refs.markdownEditor?.focus({ preventScroll: true });
        window.scrollTo(0, scrollY);
      });
    },

    showMarkdown() {
      const scrollY = window.scrollY;

      this.markdownEditing = false;

      this.$nextTick(() => {
        window.scrollTo(0, scrollY);
      });
    },

    resizeMarkdownEditor(refName = "markdownEditor") {
      const el = this.$refs[refName];

      if (!el) {
        return;
      }

      const scrollY = window.scrollY;
      el.style.height = "auto";
      el.style.height = el.scrollHeight + "px";
      window.scrollTo(0, scrollY);
    },

    async submitMarkdownEdit() {
      if (!this.currentMarkdownFileId) {
        this.error = "This file can't be saved yet — upload or open a saved markdown file first.";
        return;
      }

      const scrollY = window.scrollY;

      this.loading = true;
      this.error = null;
      this.success = null;

      try {
        const updated = await Api.updateMarkdown(this.currentMarkdownFileId, this.markdown);

        this.markdown = updated.content;
        this.success = "Markdown saved.";
        this.markdownEditing = false;

        await this.loadTopicsInFile(this.currentMarkdownFileId);

        // A renamed topic is deleted and recreated with a new id server-side,
        // so the review panel may still be pointing at a topic that no
        // longer exists — reload it onto a valid topic in that case.
        const reviewedTopicGone =
          this.review &&
          this.review.markdownFileId === this.currentMarkdownFileId &&
          !this.topicsInFile.some(topic => topic.id === this.review.topicId);

        if (reviewedTopicGone) {
          await this.startReview();
        }

        this.$nextTick(() => {
          window.scrollTo(0, scrollY);
        });

      } catch (err) {
        this.error = err.message || "Could not save markdown file.";
      } finally {
        this.loading = false;
      }
    },

    // Shared by the edit panel and the add-file panel — both are plain
    // content-in/content-out textareas, so one method covers both. This
    // only previews the AI's suggestion in the textarea; nothing is saved
    // until the existing Submit button is clicked.
    async optimizeMarkdown() {
      const isAddFile = this.leftPanel === "add-file";
      const content = isAddFile ? this.newMarkdownContent : this.markdown;

      if (!content || !content.trim()) {
        return;
      }

      this.optimizingMarkdown = true;
      this.optimizingSeconds = 0;
      this.error = null;
      this.success = null;

      this.optimizingIntervalId = setInterval(() => {
        this.optimizingSeconds++;
      }, 1000);

      try {
        const optimized = await Api.optimizeMarkdown(content);

        if (isAddFile) {
          this.newMarkdownContent = optimized.content;
          this.$nextTick(() => this.resizeMarkdownEditor("addFileEditor"));
        } else {
          this.markdown = optimized.content;
          this.$nextTick(() => this.resizeMarkdownEditor());
        }

        this.optimizeFinalized = true;

      } catch (err) {
        this.error = err.message || "Could not optimize markdown content.";
      } finally {
        clearInterval(this.optimizingIntervalId);
        this.optimizingIntervalId = null;
        this.optimizingMarkdown = false;
      }
    },

    async selectEnvironment(environment) {
      this.selectedEnvironment = environment;
      this.currentCategory = environment;
      this.environmentMenuOpen = false;
      this.openMarkdownGroup = null;
      this.saveLastCategory(environment);

      await this.loadDashboard();
      await this.loadMarkdownSummaries(false);

      this.autoOpenSingleMarkdownGroup();
    },

    autoOpenSingleMarkdownGroup() {
      const groupNames = Object.keys(this.filteredMarkdownGroups);

      if (groupNames.length === 1) {
        this.openMarkdownGroup = groupNames[0];
      }
    },

    toggleEnvironmentMenu() {
      this.environmentMenuOpen = !this.environmentMenuOpen;
    },

    async startReview(topicId = null, allowBroaderFallback = !topicId) {
      await this.loadReviewQuestion(
        () => Api.startReview(this.currentMarkdownFileId, this.currentCategory, topicId),
        allowBroaderFallback
      );
    },

    async skipReview() {
      if (!this.review) {
        return;
      }

      const topicId = this.review.topicId;
      await this.loadReviewQuestion(
        () => Api.skipReview(topicId, this.currentMarkdownFileId, this.currentCategory),
        true
      );
    },

    async loadReviewQuestion(fetchQuestion, allowBroaderFallback) {
      // If a file's content is already showing (e.g. previewed before
      // clicking Start Review), don't flash a loading state — or, on
      // mobile, the dashboard — over it. Only show those once we know
      // there's nothing already on screen to preserve.
      const markdownAlreadyShown = !!this.currentMarkdownFileId && !!this.markdown;
      const startingFreshReview = markdownAlreadyShown && !this.review;

      this.loading = true;
      this.reviewQuestionLoading = true;
      if (!markdownAlreadyShown) {
        this.markdownLoading = true;
      }
      this.markdownEditing = false;
      this.mobilePageTitle = "Review";
      this.error = null;
      this.success = null;
      this.result = null;
      this.userAnswer = "";
      this.answerSubmitted = false;
      this.showAccount = false;
      this.leftPanel = "markdown";
      if (!startingFreshReview) {
        this.mobileView = "review";
      }
      this.feedbackHistory = [];
      this.feedbackHistoryVisible = false;
      this.feedbackHistoryLoadedForTopicId = null;
      this.topicDropdownOpen = false;

      try {
        let data = await fetchQuestion();

        if (data.done && this.currentMarkdownFileId && allowBroaderFallback) {
          this.currentMarkdownFileId = null;
          data = await Api.startReview(null, this.currentCategory);
        }

        if (data.done) {
          this.review = null;
          this.reviewMessage = data.message;
          this.topicsInFile = [];
          await this.loadDashboard();
          return;
        }

        this.review = data;
        this.currentMarkdownFileId = this.review.markdownFileId;
        this.mobileView = "review";

        if (this.review.markdownContent) {
          this.markdownLoading = false;
          this.markdown = this.review.markdownContent;
        }

        this.$nextTick(() => window.scrollTo(0, 0));

        await this.loadDashboard();

        await this.loadFeedbackHistory(this.review.topicId);
        await this.loadMarkdownReviewCount();
        await this.loadTopicsInFile(this.review.markdownFileId);

      } catch (err) {
        this.review = null;
        this.error = err.message || "Could not start review.";
        // Make sure the error is actually visible even if we deferred
        // switching mobileView while hoping for a quiet success above.
        this.mobileView = "review";
      } finally {
        this.loading = false;
        this.markdownLoading = false;
        this.reviewQuestionLoading = false;
      }
    },

    async startReviewForFile(markdownFileId) {
      this.currentMarkdownFileId = markdownFileId;

      // Disable broadening here so a miss never briefly renders a due
      // question from a different file — go straight to this file's own
      // first topic below instead.
      await this.startReview(null, false);

      // Nothing was due in this specific file (and no error, e.g. the
      // daily question limit, stopped us first). Since the user explicitly
      // chose this file, show them its first topic anyway instead of
      // silently redirecting away from it.
      if (!this.review && !this.error) {
        let topics = [];

        try {
          topics = await Api.loadTopicsByMarkdownFile(markdownFileId);
        } catch (err) {
          topics = [];
        }

        if (topics.length > 0) {
          this.currentMarkdownFileId = markdownFileId;
          await this.startReview(topics[0].id);
        }
      }
    },

    // Shows a file's markdown without generating a review question, so
    // browsing files doesn't spend the daily question limit.
    async previewMarkdownFile(markdownFileId) {
      this.currentMarkdownFileId = markdownFileId;
      this.review = null;
      this.result = null;
      this.error = null;
      this.success = null;
      this.markdownEditing = false;
      this.leftPanel = "markdown";
      this.mobilePageTitle = "Markdown";
      this.mobileView = "markdown";
      this.markdownLoading = true;

      try {
        const data = await Api.getMarkdown(markdownFileId);
        this.markdown = data.content;
      } catch (err) {
        this.error = err.message || "Could not load markdown file.";
      } finally {
        this.markdownLoading = false;
      }

      await this.loadTopicsInFile(markdownFileId);
      await this.loadMarkdownReviewCount();
    },

    async startReviewFromDashboard() {
      if (this.currentMarkdownFileId) {
        await this.startReviewForFile(this.currentMarkdownFileId);
      } else {
        await this.startReview();
      }
    },

    async loadTopicsInFile(markdownFileId) {
      if (!markdownFileId) {
        this.topicsInFile = [];
        return;
      }

      try {
        this.topicsInFile = await Api.loadTopicsByMarkdownFile(markdownFileId);
      } catch (err) {
        this.topicsInFile = [];
      }
    },

    openTopicDropdown(event) {
      const iconRect = event.currentTarget.getBoundingClientRect();
      const dropdownWidth = 240; // matches .topic-select-dropdown min-width
      const margin = 16;

      this.topicDropdownAlignRight = iconRect.left + dropdownWidth + margin > window.innerWidth;
      this.topicDropdownOpen = true;
    },

    closeTopicDropdown() {
      this.topicDropdownOpen = false;
    },

    toggleTopicDropdown(event) {
      if (this.topicDropdownOpen) {
        this.closeTopicDropdown();
      } else {
        this.openTopicDropdown(event);
      }
    },

    async selectTopic(topicId) {
      this.topicDropdownOpen = false;

      if (this.review && topicId === this.review.topicId) {
        return;
      }

      await this.startReview(topicId);
    },

    async uploadMarkdown(event) {
      const file = event.target.files[0];

      if (!file) {
        return;
      }

      this.mobileView = "dashboard";
      this.loading = true;
      this.error = null;
      this.success = null;
      this.result = null;

      try {
        this.markdown = await file.text();

        const uploaded = await Api.uploadMarkdown(file);

        this.currentMarkdownFileId = uploaded.markdownFileId;
        this.markdown = uploaded.content;
        this.leftPanel = "markdown";
        this.mobileView = "review"; // show markdown + right panel on mobile

        await this.startReview();
        await this.loadCategoryOptions();
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
      this.success = null;

      try {
        this.result = await Api.submitAnswer({
          topicId: this.review.topicId,
          question: this.review.question,
          userAnswer: this.userAnswer
        });

        this.feedbackHistoryVisible = false;

        await this.loadDashboard();
        await this.loadMarkdownSummaries(false);

        if (!this.review) {
          this.showMarkdownSummaries();
        }

      } catch (err) {
        this.error = err.message || "Could not submit answer.";
      } finally {
        this.loading = false;
      }
    },

    // Keeps the same question in place instead of fetching a new one,
    // so the user can attempt it again.
    retryQuestion() {
      this.answerSubmitted = false;
      this.result = null;
      this.error = null;
      this.success = null;
    },

    async loadCategoryOptions() {
      try {
        const options = await Api.loadCategoryOptions();
        this.categoryOptions = options || [];

        const hasCurrentCategory = this.categoryOptions.some(
          option => option.category === this.currentCategory
        );

        if (!hasCurrentCategory && this.categoryOptions.length > 0) {
          const lastCategory = this.readLastCategory();

          const hasLastCategory = lastCategory && this.categoryOptions.some(
            option => option.category === lastCategory
          );

          const fallbackCategory = hasLastCategory
            ? lastCategory
            : this.categoryOptions[0].category;

          this.selectedEnvironment = fallbackCategory;
          this.currentCategory = fallbackCategory;
        }

      } catch (err) {
        console.error("Could not load category options", err);
      }
    },

    readLastCategory() {
      try {
        return localStorage.getItem(LAST_CATEGORY_STORAGE_KEY);
      } catch (err) {
        return null;
      }
    },

    saveLastCategory(category) {
      try {
        localStorage.setItem(LAST_CATEGORY_STORAGE_KEY, category);
      } catch (err) {
        // localStorage unavailable (disabled/private mode) - preference just won't persist
      }
    },

    async loadDashboard() {
      try {
        const data = await Api.loadDashboard(this.currentCategory);

        if (data) {
          this.dashboard = data;
        }

      } catch (err) {
        console.error("Could not load dashboard", err);
      }
    },

    async loadMarkdownSummaries(autoShow = true) {
      try {
        const data = await Api.loadMarkdownSummaries(this.currentCategory);

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
      this.error = null;
      this.success = null;
      this.review = null;
      this.result = null;
      this.userAnswer = "";
      this.currentMarkdownFileId = null;
      this.showAccount = false;
      this.mobileView = "dashboard";
      this.leftExpanded = false;

      await this.loadDashboard();

      if (this.isDesktop) {
        this.showMarkdownSummaries();
      }
    },

    showMarkdownSummaries() {
      this.error = null;
      this.success = null;
      this.mobilePageTitle = "Markdowns";
      this.leftPanel = "markdowns";
      this.mobileView = "markdown";
      this.markdownEditing = false;
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
      this.error = null;
      this.success = null;
      this.mobilePageTitle = "Account Details";
      this.leftPanel = "account";
      this.mobileView = "markdown";
      this.markdownEditing = false;
    },

    showStarterPack() {
      this.error = null;
      this.success = null;
      this.mobilePageTitle = "Starter Pack";
      this.leftPanel = "starter-pack";
      this.mobileView = "markdown";
      this.markdownEditing = false;
    },

    async showDashboard() {
      this.error = null;
      this.success = null;
      this.review = null;
      this.result = null;
      this.userAnswer = "";
      this.currentMarkdownFileId = null;
      this.mobileView = "dashboard";
      this.leftExpanded = false;
      this.mobilePageTitle = "Dashboard";
      this.markdownEditing = false;

      await this.loadDashboard();

      if (this.isDesktop) {
        this.showMarkdownSummaries();
      }
    },

    closeLeftPanel() {
      this.error = null;
      this.success = null;
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
        //"#f45b00", // orange
        //"#8b5cf6", // purple
        "#3b82f6"  // blue
      ];

      return palette[index % palette.length];
    },

    softColor(hex) {
      return hex + "18";
    },

    toggleFeedbackHistory() {
      this.feedbackHistoryVisible = !this.feedbackHistoryVisible;
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

    openAdminUsers() {
      this.leftPanel = "admin-users";
      this.mobilePageTitle = "Admin Users";
      this.mobileView = "markdown";
      this.markdownEditing = false;
      this.loadAdminUsers();
    },

    async loadAdminUsers() {
      this.adminUsers = await Api.loadAdminUsers();
    },

    showDangerZonePage() {
      this.leftPanel = "danger-zone";
      this.leftExpanded = false;
      this.menuOpen = false;
      this.mobileMenuOpen = false;
      this.mobileView = "markdown";
      this.markdownEditing = false;
    },

    async resetLearningData() {
      const confirmed = confirm(
        "Delete all your learning data? This cannot be undone."
      );

      if (!confirmed) {
        return;
      }

      try {
        this.loading = true;
        this.error = null;
        this.success = null;
        await Api.resetLearningData();
        this.showDangerZone = false;

        this.review = null;
        this.result = null;
        this.markdown = "";
        this.selectedMarkdownFileId = null;

        this.markdownSummaries = [];
        this.categoryOptions = [];
        this.currentCategory = null;
        this.selectedEnvironment = null;
        this.openMarkdownGroup = null;
        this.environmentMenuOpen = false;

        this.markdown = this.initialMarkdown;
        this.leftPanel = "markdowns";
        this.mobileView = "markdown";

        await this.loadCategoryOptions();
        await this.loadDashboard();

        this.success = "Learning data reset successfully.";

      } catch (e) {
        this.error = e.message;
        this.success = null;
      } finally {
        this.loading = false;
      }
    },

    async resetReviewsData() {
      const confirmed = confirm(
        "Delete all your reviews data? This cannot be undone."
      );

      if (!confirmed) {
        return;
      }

      try {
        this.loading = true;
        this.error = null;
        this.success = null;
        await Api.resetReviewsData();
        this.showDangerZone = false;

        this.review = null;
        this.result = null;
        this.markdown = "";
        this.selectedMarkdownFileId = null;

        this.markdownSummaries = [];
        this.categoryOptions = [];
        this.currentCategory = null;
        this.selectedEnvironment = null;
        this.openMarkdownGroup = null;
        this.environmentMenuOpen = false;

        this.markdown = this.initialMarkdown;
        this.leftPanel = "markdowns";
        this.mobileView = "markdown";

        await this.loadCategoryOptions();
        await this.loadDashboard();

        this.success = "Reviews reset successfully.";

      } catch (e) {
        this.error = e.message;
        this.success = null;
      } finally {
        this.loading = false;
      }
    },

    async resetFileReviews() {
      if (!this.currentMarkdownFileId || this.currentMarkdownReviewCount === 0) {
        return;
      }

      const confirmed = confirm(
        "Delete all reviews and progress for this markdown file? This cannot be undone."
      );

      if (!confirmed) {
        return;
      }

      try {
        this.loading = true;
        this.error = null;
        this.success = null;

        await Api.resetMarkdownReviews(this.currentMarkdownFileId);

        this.review = null;
        this.result = null;
        this.feedbackHistory = [];
        this.feedbackHistoryVisible = false;
        this.feedbackHistoryLoadedForTopicId = null;

        await this.loadMarkdownSummaries(false);
        await this.loadDashboard();
        await this.loadMarkdownReviewCount();

        this.success = "Reviews reset for this file.";

      } catch (e) {
        this.error = e.message;
        this.success = null;
      } finally {
        this.loading = false;
      }
    },

    async loadMarkdownReviewCount() {
      if (!this.currentMarkdownFileId) {
        this.currentMarkdownReviewCount = null;
        return;
      }

      this.currentMarkdownReviewCount = null;

      try {
        this.currentMarkdownReviewCount = await Api.getMarkdownReviewCount(this.currentMarkdownFileId);
      } catch (e) {
        this.currentMarkdownReviewCount = null;
      }
    },

    async deleteMarkdownFile() {
      if (!this.currentMarkdownFileId) {
        return;
      }

      if (this.currentMarkdownReviewCount !== 0) {
        this.error = "This file has reviews — reset them before deleting it.";
        this.success = null;
        return;
      }

      const confirmed = confirm(
        "Delete this markdown file and all its topics? This cannot be undone."
      );

      if (!confirmed) {
        return;
      }

      try {
        this.loading = true;
        this.error = null;
        this.success = null;

        await Api.deleteMarkdown(this.currentMarkdownFileId);

        this.currentMarkdownFileId = null;
        this.currentMarkdownReviewCount = null;
        this.review = null;
        this.result = null;
        this.markdown = "";
        this.feedbackHistory = [];
        this.feedbackHistoryVisible = false;
        this.feedbackHistoryLoadedForTopicId = null;

        this.leftPanel = "markdowns";
        this.mobileView = "markdown";

        await this.loadMarkdownSummaries(false);
        await this.loadDashboard();

        this.success = "Markdown file deleted.";

      } catch (e) {
        this.error = e.message;
        this.success = null;
      } finally {
        this.loading = false;
      }
    },

    async importStarterPack(category) {
      try {
        this.loading = true;
        this.error = null;
        this.success = null;

        await Api.importStarterPack(category);

        await this.loadCategoryOptions();

        this.currentCategory = category;
        this.selectedEnvironment = category;

        await this.loadDashboard();
        await this.loadMarkdownSummaries(true);

        this.autoOpenSingleMarkdownGroup();

      } catch (e) {
        this.error = e.message;
      } finally {
        this.loading = false;
      }
    },

    showMarkdownExample() {
      this.error = null;
      this.success = null;
      this.mobilePageTitle = "Markdown Example";
      this.leftPanel = "markdown";
      this.showAccount = false;
      this.mobileView = "markdown";
      this.currentMarkdownFileId = null;
      this.markdownEditing = false;
      this.markdown = `
<h3>File Example Pattern</h3>

<pre>## Environment / Category / Subcategory / File Title

### 1. Topic
Content...

### 2. Topic
Content...</pre>

<h3>Example: threads_theory.md</h3>

<pre>## JVM / Java / Threads / Theory

### 1. Creating Threads
Thread creation basics...

### 2. Implementing Runnable
Java supports single inheritance...</pre>
`;
    },

    showAddFile() {
      this.error = null;
      this.success = null;
      this.mobilePageTitle = "Add File";
      this.leftPanel = "add-file";
      this.mobileView = "markdown";
      this.newMarkdownContent = "";
      this.optimizeFinalized = false;
    },

    async submitAddFile() {
      if (!this.newMarkdownContent.trim()) {
        return;
      }

      this.loading = true;
      this.error = null;
      this.success = null;
      this.result = null;

      try {
        const created = await Api.createMarkdown(this.newMarkdownContent);

        this.currentMarkdownFileId = created.markdownFileId;
        this.markdown = created.content;
        this.newMarkdownContent = "";
        this.leftPanel = "markdown";
        this.mobileView = "review"; // show markdown + right panel on mobile

        await this.startReview();
        await this.loadCategoryOptions();
        await this.loadDashboard();
        await this.loadMarkdownSummaries(false);

      } catch (err) {
        this.error = err.message || "Could not create markdown file.";
      } finally {
        this.loading = false;
      }
    }
  }
}).mount("#app");
